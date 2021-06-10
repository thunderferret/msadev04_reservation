![](./images/01_msaez.png){:height="50%" width="50%"}

# 예제 -  콘서트 예매 어플리케이션

본 예제는 MSA/DDD/Event Storming/EDA 를 포괄하는 분석/설계/구현/운영 전단계를 커버하도록 구성한 예제입니다.
이는 클라우드 네이티브 애플리케이션의 모델, 구현, 운영을 포함합니다.



# Table of contents


# 체크포인트
Local : k8s 1.21 ,Docker 20, Gradle 7.2, Springboot 2.5.0
Assessment : k8s 1.17 ,Docker 19, Gradle 7.x, Springboot 2.2.x ~ 2.5.0

Local 환경에서 작동하나, Assessment 환경에서 Gradle 작동하지 않는 경우 다수 발견
-> https://acloudguru.com/blog/engineering/kubernetes-is-deprecating-docker-what-you-need-to-know
K8s 에서 Docker를 Deprecating하면서 Docker runtime 시 Docker20 이상 버전과 호환되는 설정이 다른 것으로 추정
-> Local Docker 에서는 정상적으로 작동되는 것 확인

따라서 로컬과 Assessment 환경에서 작동하는 것 병기

예시
Local : O
Assessment : X
(로컬에서는 작동하나, Asseesment 환경에서는 작동하지 않음)


1. Saga
Myapp 에서 구현
Local : 0
Assessment : X
(Gradle 기반 서버가 작동하지 않으므로)

1. CQRS
Event Kafka 로 발송하여, Python Consumer 에서 볼 수 있음
Local : 0
Assessment : X

1. Correlation
REST 를 이용한 Sync 호출 사용
Local : 0
Assessment : X


1. Req/Resp
REST 이용
Local : 0
Assessment : X


1. Gateway
Spring cloud Gateway 이용
Local : 0
Assessment : O

1. Deploy/ Pipeline
AWS Codebuild 사용
Local : 0
Assessment : O

1. Circuit Breaker
OpenFeign Client 사용
Local : 0
Assessment : 0

1. Autoscale (HPA)
Local : 해당사항 없음
Assessment : 0


1. Zero-downtime deploy (Readiness Probe)
Local : X
Assessment : X


1. Config Map/ Persistence Volume
Kafka PV 배포
Local : 해당사항 없음
Assessment : X

1. Polyglot
Python 으로 myapp 구현
Local : 0
Assessment : X

1. Self-healing (Liveness Probe)
Local : X
Assessment : X


# 서비스 시나리오

![](./images/01_msaez_team.png){:height="50%" width="50%"}

Team 프로젝트에서, 영화 예매 어플리케이션을 구현 하였다.
해당 어플리케이션에서 스프링 부트 버전이나, maven, Docker Version 등을 변경하고자
Zero - Base 에서 다시 어플리케이션을 구현하였다.

Gradle 7.x 과 SpringBoot 2.5.X 버전, K8S 1.9, Docker 20.0.4 를 사용한 환경에서 작동하나,

EKS 1.14, Docker19 등에서는 현재 설정 에러로 Gradle 기반 프로젝트가 작동하지 않는 상황.
(Docker 19 버전 이상을 지원하지 않는다)
jar 등으로 배포하는 것을 권장한다

기능적 요구사항
1. 고객이 예매를 한다.
1. 남은 좌석이 있으면 결제할 수 있다.
1. 결제가 되면, 좌석이 배정된다.
1. 좌석이 배정되면 고객에게 좌석과 고객 정보를 전달한다
1. 고객이 예매를 취소할 수 있다
1. 예매가 취소되면 결제도 취소된다
1. 고객이 예매 상태를 중간중간 조회할 수 있다

비기능적 요구사항
1. 트랜잭션
    1. 결제가 안된 예약건은 아예 좌석 배치가 성립되지 않아야 한다  Sync 호출 
1. 장애격리
    1. 예매 기능이 수행되지 않더라도 나의 페이지는 365일 24시간 확인 할 수 있어야 한다  Async (event-driven), Eventual Consistency
    1. 결제시스템이 과중되면 사용자를 잠시동안 받지 않고 결제를 잠시후에 하도록 유도한다  Circuit breaker, fallback
1. 성능
    1. 고객이 자주 예매관리에서 확인할 수 있는 잔여좌석 상태를 예약시스템(프론트엔드)에서 확인할 수 있어야 한다  CQRS
    1. 잔여좌석 상태가 바뀔때마다 카톡 등으로 알림을 줄 수 있어야 한다  Event driven


# 분석/설계


### 1차 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

![](./images/03_sc1.png){:height="50%" width="50%"}

    - 고객이 Ticket 을 신청한다 (ok)
    - 고객이 결제한다 (ok)
    - 결제가 되면, 좌석이 줄어든다 (ok)
    - MyPage 에서 해당 Event 를 수신한다 (ok)

![](./images/04_sc2.png){:height="50%" width="50%"}
    - 고객이 Ticket 을 취소한다 (Ok)
    - 결제 모듈에서 과거 결제 이력을 변경한다(Ok)
    - 취소가 되면 취소 메세지를 전송한다

### 비기능 요구사항에 대한 검증

![image](https://user-images.githubusercontent.com/487999/79684184-5c9a9400-826a-11ea-8d87-2ed1e44f4562.png)

    - 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
        - 고객 주문시 결제처리: 결제시 티켓 에서는 결제 회사에서는 결제 여부만을 전송받는다.
        - 결제 완료시 myApp, Publish: Cloud Stream 은
        - REST 로 구현 : Spring Cloud는 2020 초에 지원이 끝나, 현재 Gradle 이나 Springboot 버전에서 작동을 안하거나 K8s의 구 버전에서 에러
        - 나머지 모든 inter-microservice 트랜잭션




## 헥사고날 아키텍처 다이어그램 도출
    
![image](https://user-images.githubusercontent.com/487999/79684772-eba9ab00-826e-11ea-9405-17e2bf39ec76.png)


    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트와 파이선으로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)


```
티켓 예매
- 티켓을 예약받고, 결제에 User 정보를 보내 결제 확인만 한다
- 결제 확인이 되었을 경우, 예약 정보를 반영한다. 좌석 숫자를 줄인다
- 예약 취소시 DB에서 Reservation 정보를 삭제하고, Seat 숫자를 추가한다

티켓 결제
- 유저 정보를 받아, Balance 를 Check 한다
- Payrequest 는 기록된다.
- Approved 된 결제 이력을 확인하고, 취소 시 결제 이력을 삭제한다.

mypage로 구성하였다
- python + Redis 로 구현
- Kafka로 모든 정보를 Subscribe 하고, user id 로 검색하여 User 에게 정보를 전달한다.



```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (예시는 Reservation 마이크로 서비스). 이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력했다. 하지만, 일부 구현에 있어서 영문이 아닌 경우는 실행이 불가능한 경우가 있기 때문에 계속 사용할 방법은 아닌것 같다. (Maven pom.xml, Kafka의 topic id, FeignClient 의 서비스 id 등은 한글로 식별자를 사용하는 경우 오류가 발생하는 것을 확인하였다)

```
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity(name="reservations")
public class Reservation {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;


    Long ticketNumber;
    String customerName;
    String customerId;
    String reservationStatus;
}


```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```

package global.bizdevelope.realmanapp.domain;

import global.bizdevelope.realmanapp.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}


```
- 적용 후 REST API 의 테스트
```
# reservation 서비스의 예매처리
http post localhost:8081/resrvationreq userId="skccman@naver.com" userName="김인철"

# payment 서비스의 결제처리
http post localhost:8082/payrequest userId="skccman@naver.com" userName="김인철"
위 reservationreq 에서 User 객체를 @RequestBody 로 건네 받는다

Transcational 하게 결제를 처리하고, Accepted 와 Refused 여부만 전송한다

# 예매 상태 확인
http get localhost:8083/myticket{userId}
예매 상태를 조회한다 (CQRS 적용

```


## 폴리글랏 퍼시스턴스

Reservation 과 Payment 서버는 Jpa Repository 를 상속받아 객체 지향적으로
구성될 수 있게 했다. mysql 서버를 사용하는 것이 Default 이다.

Lombok @Data 와 @Entity 을 사용하여 가독성을 높였다.

```
#Reservation Domain Reservation

@Data
@NoArgsConstructor
@Entity(name="reservations")
public class Reservation {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;


    Long ticketNumber;
    String customerName;
    String customerId;
    String reservationStatus;
}


# ApprovedtRepository.java


import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovedRepository extends JpaRepository<Approved, Long> {
    public void deleteByCustomerId(String userId);
}


```

## 폴리글랏 프로그래밍

Python Flask, Redis 와 Kafka Consumer 로 구현 되었다.



```

# app.py

from kafka import KafkaConsumer
from json import loads

  # topic, broker list
consumer = KafkaConsumer( 'topic1',
bootstrap_servers=['localhost:9092'],
auto_offset_reset='earliest',
enable_auto_commit=True,
group_id='my-group',
consumer_timeout_ms=1000 ) # consumer list를 가져온다
print('[begin] get consumer list')

while True:
    for message in consumer:
        print("Topic: %s, Partition: %d, Offset: %d, Key: %s, Value: %s" % ( message.topic, message.partition, message.offset, message.key, message.value ))
        print('[end] get consumer list')



```

파이선 애플리케이션을 컴파일하고 실행하기 위한 도커파일은 아래와 같다 (운영단계에서 할일인가? 아니다 여기 까지가 개발자가 할일이다. Immutable Image):
```
FROM python:3.7
COPY index.html /app/
COPY requirements.txt /app/
COPY app.py /app/
WORKDIR /app
RUN pip install -r requirements.txt
CMD python -u app.py
```


## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 예약(app)->결제(pay) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다.
따라서 Service 의 @Transactional 을 이용하여, 일관성을 유지할 수 있도록 하였다.

- REST @Service 의 @Transactional 을 이용하여 동기식 서비스 구현

```
#public class ApproveService
    @Transactional
    public boolean approveCheck(User user){
        PayRequest payRequest = new PayRequest();
        payRequest.setCustomerId(user.getUserId());

        Boolean isApproved = false;
        try{
            isApproved=userRepository.findByUserId(user.getUserId()).isBalance();
        }catch (Exception e){
            payRequest.setPaymentStatus("Fail");
            isApproved=false;
        }
        payRequest.setPaymentStatus("Success");
        payRequestRepository.save(payRequest);
        saveApproveInfo(user);
        return isApproved;

    }
```

- 결제가 확인 된 이후, 처리
```
# ReservationService.java (Service)

    @Transactional
    public String reserve(@RequestBody Reservation reservation){
        if(approveCheck().equals("Accepted")){
            Reservation newReservation = reservationRepository.save(reservation);
            return reservation.
        }
        return "Reservation Error";
    }
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 결제 시스템이 장애가 나면 주문도 못받는다는 것을 확인:


```
# 결제 (pay) 서비스를 잠시 내려놓음 (ctrl+c)

#예매 처리
http post localhost:8081/reservationreq userId="test1@naver.com"   #Fail
http post localhost:8081/cancelreservation userId="test1@naver.com" #Fail

#결제서비스 재기동
cd 결제
gradle ./build

#예매 처리
http post localhost:8081/reservationreq userId="test1@naver.com"  #Success
http post localhost:8081/cancelreservation userId="test1@naver.com"   #Success
```

- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)




## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트


결제가 이루어진 후에 상점시스템으로 이를 알려주는 행위는 동기식이 아니라 비 동기식으로 처리하여 예매 시스템의 처리를 위하여 결제주문이 블로킹 되지 않아도록 처리한다.
 
- 이를 위하여 결제이력에 기록을 남긴 후에 곧바로 결제승인이 되었다는 도메인 이벤트를 카프카로 송출한다(Publish)
 
```
package fooddelivery;

@Entity
@Table(name="결제이력_table")
public class 결제이력 {

 ...
    @Transactional
    public String cancelReservation(User user){
        List<Reservation> reservationList;

        try{
        reservationList=reservationRepository.findByCustomerId(user.getUserId());
        if(reservationList==null) {
            return "Refused";
        }
        for (Reservation r:reservationList){
            cancelled.publish(user.getUserId()+":Cancelled:"+r.getId());
            reservationRepository.delete(r.getId());
        }
        }catch (Exception e){
            return "Error";
        }
        return "Cancelled";
    }
```
- Mypage 에서는 해당 Event 를 수신받아 처리한다

```
package fooddelivery;

...

@Service
public class PolicyHandler{

    @StreamListener(KafkaProcessor.INPUT)
    public void whenever결제승인됨_주문정보받음(@Payload 결제승인됨 결제승인됨){

        if(결제승인됨.isMe()){
            System.out.println("##### listener 주문정보받음 : " + 결제승인됨.toJson());
            // 주문 정보를 받았으니, 요리를 슬슬 시작해야지..
            
        }
    }

}

```
실제 구현을 하자면, 카톡 등으로 점주는 노티를 받고, 요리를 마친후, 주문 상태를 UI에 입력할테니, 우선 주문정보를 DB에 받아놓은 후, 이후 처리는 해당 Aggregate 내에서 하면 되겠다.:
  
```
  @Autowired 주문관리Repository 주문관리Repository;
  
  @StreamListener(KafkaProcessor.INPUT)
  public void whenever결제승인됨_주문정보받음(@Payload 결제승인됨 결제승인됨){

      if(결제승인됨.isMe()){
          카톡전송(" 주문이 왔어요! : " + 결제승인됨.toString(), 주문.getStoreId());

          주문관리 주문 = new 주문관리();
          주문.setId(결제승인됨.getOrderId());
          주문관리Repository.save(주문);
      }
  }

```

상점 시스템은 주문/결제와 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 상점시스템이 유지보수로 인해 잠시 내려간 상태라도 주문을 받는데 문제가 없다:
```
# 상점 서비스 (store) 를 잠시 내려놓음 (ctrl+c)

#주문처리
http localhost:8081/orders item=통닭 storeId=1   #Success
http localhost:8081/orders item=피자 storeId=2   #Success

#주문상태 확인
http localhost:8080/orders     # 주문상태 안바뀜 확인

#상점 서비스 기동
cd 상점
mvn spring-boot:run

#주문상태 확인
http localhost:8080/orders     # 모든 주문의 상태가 "배송됨"으로 확인
```


# 운영

## CI/CD 설정


각 구현체들은 각자의 source repository 에 구성되었고, 사용한 CI/CD 플랫폼은 GCP를 사용하였으며, pipeline build script 는 각 프로젝트 폴더 이하에 cloudbuild.yml 에 포함되었다.


## 동기식 호출 / 서킷 브레이킹 / 장애격리

TBD


```

TBD

```
# (pay) 결제이력.java (Entity)






```

* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 100명
- 60초 동안 실시

```
$ siege -c100 -t60S -r10 --content-type "application/json" 'http://localhost:8081/orders POST {"item": "chicken"}'

** SIEGE 4.0.5
** Preparing 100 concurrent users for battle.
The server is now under siege...

HTTP/1.1 201     0.68 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.68 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.70 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.70 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.73 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.75 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.77 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.97 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.81 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.87 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.12 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.16 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.17 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.26 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.25 secs:     207 bytes ==> POST http://localhost:8081/orders

* 요청이 과도하여 CB를 동작함 요청을 차단

HTTP/1.1 500     1.29 secs:     248 bytes ==> POST http://localhost:8081/orders   
HTTP/1.1 500     1.24 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     1.23 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     1.42 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     2.08 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.29 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     1.24 secs:     248 bytes ==> POST http://localhost:8081/orders

* 요청을 어느정도 돌려보내고나니, 기존에 밀린 일들이 처리되었고, 회로를 닫아 요청을 다시 받기 시작

HTTP/1.1 201     1.46 secs:     207 bytes ==> POST http://localhost:8081/orders  
HTTP/1.1 201     1.33 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.36 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.63 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.65 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.68 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.69 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.71 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.71 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.74 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.76 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.79 secs:     207 bytes ==> POST http://localhost:8081/orders

* 다시 요청이 쌓이기 시작하여 건당 처리시간이 610 밀리를 살짝 넘기기 시작 => 회로 열기 => 요청 실패처리

HTTP/1.1 500     1.93 secs:     248 bytes ==> POST http://localhost:8081/orders    
HTTP/1.1 500     1.92 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     1.93 secs:     248 bytes ==> POST http://localhost:8081/orders

* 생각보다 빨리 상태 호전됨 - (건당 (쓰레드당) 처리시간이 610 밀리 미만으로 회복) => 요청 수락

HTTP/1.1 201     2.24 secs:     207 bytes ==> POST http://localhost:8081/orders  
HTTP/1.1 201     2.32 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.16 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.19 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.19 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.19 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.21 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.29 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.30 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.38 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.59 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.61 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.62 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.64 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.01 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.27 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.33 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.45 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.52 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.57 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.69 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.70 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.69 secs:     207 bytes ==> POST http://localhost:8081/orders

* 이후 이러한 패턴이 계속 반복되면서 시스템은 도미노 현상이나 자원 소모의 폭주 없이 잘 운영됨


HTTP/1.1 500     4.76 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.23 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.76 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.74 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.82 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.82 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.84 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.66 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     5.03 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.22 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.19 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.18 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.69 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.65 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     5.13 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.84 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.25 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.25 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.80 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.87 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.33 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.86 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.96 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.34 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.04 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.50 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.95 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.54 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.65 secs:     207 bytes ==> POST http://localhost:8081/orders


:
:

Transactions:		        1025 hits
Availability:		       63.55 %
Elapsed time:		       59.78 secs
Data transferred:	        0.34 MB
Response time:		        5.60 secs
Transaction rate:	       17.15 trans/sec
Throughput:		        0.01 MB/sec
Concurrency:		       96.02
Successful transactions:        1025
Failed transactions:	         588
Longest transaction:	        9.20
Shortest transaction:	        0.00

```
- 운영시스템은 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 자원을 보호하고 있음을 보여줌. 하지만, 63.55% 가 성공하였고, 46%가 실패했다는 것은 고객 사용성에 있어 좋지 않기 때문에 Retry 설정과 동적 Scale out (replica의 자동적 추가,HPA) 을 통하여 시스템을 확장 해주는 후속처리가 필요.

- Retry 의 설정 (istio)
- Availability 가 높아진 것을 확인 (siege)

### 오토스케일 아웃
앞서 CB 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다. 


- 결제서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 10개까지 늘려준다:
```
kubectl autoscale deploy pay --min=1 --max=10 --cpu-percent=15
```
- CB 에서 했던 방식대로 워크로드를 2분 동안 걸어준다.
```
siege -c100 -t120S -r10 --content-type "application/json" 'http://localhost:8081/orders POST {"item": "chicken"}'
```
- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
kubectl get deploy pay -w
```
- 어느정도 시간이 흐른 후 (약 30초) 스케일 아웃이 벌어지는 것을 확인할 수 있다:
```
NAME    DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
pay     1         1         1            1           17s
pay     1         2         1            1           45s
pay     1         4         1            1           1m
:
```
- siege 의 로그를 보아도 전체적인 성공률이 높아진 것을 확인 할 수 있다. 
```
Transactions:		        5078 hits
Availability:		       92.45 %
Elapsed time:		       120 secs
Data transferred:	        0.34 MB
Response time:		        5.60 secs
Transaction rate:	       17.15 trans/sec
Throughput:		        0.01 MB/sec
Concurrency:		       96.02
```


## 무정지 재배포

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 CB 설정을 제거함

- seige 로 배포작업 직전에 워크로드를 모니터링 함.
```
siege -c100 -t120S -r10 --content-type "application/json" 'http://localhost:8081/orders POST {"item": "chicken"}'

** SIEGE 4.0.5
** Preparing 100 concurrent users for battle.
The server is now under siege...

HTTP/1.1 201     0.68 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.68 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.70 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.70 secs:     207 bytes ==> POST http://localhost:8081/orders
:

```

- 새버전으로의 배포 시작
```
kubectl set image ...
```

- seige 의 화면으로 넘어가서 Availability 가 100% 미만으로 떨어졌는지 확인
```
Transactions:		        3078 hits
Availability:		       70.45 %
Elapsed time:		       120 secs
Data transferred:	        0.34 MB
Response time:		        5.60 secs
Transaction rate:	       17.15 trans/sec
Throughput:		        0.01 MB/sec
Concurrency:		       96.02

```
배포기간중 Availability 가 평소 100%에서 70% 대로 떨어지는 것을 확인. 원인은 쿠버네티스가 성급하게 새로 올려진 서비스를 READY 상태로 인식하여 서비스 유입을 진행한 것이기 때문. 이를 막기위해 Readiness Probe 를 설정함:

```
# deployment.yaml 의 readiness probe 의 설정:


kubectl apply -f kubernetes/deployment.yaml
```

- 동일한 시나리오로 재배포 한 후 Availability 확인:
```
Transactions:		        3078 hits
Availability:		       100 %
Elapsed time:		       120 secs
Data transferred:	        0.34 MB
Response time:		        5.60 secs
Transaction rate:	       17.15 trans/sec
Throughput:		        0.01 MB/sec
Concurrency:		       96.02

```

배포기간 동안 Availability 가 변화없기 때문에 무정지 재배포가 성공한 것으로 확인됨.


# 신규 개발 조직의 추가

  ![image](https://user-images.githubusercontent.com/487999/79684133-1d6c4300-826a-11ea-94a2-602e61814ebf.png)


## 마케팅팀의 추가
    - KPI: 신규 고객의 유입률 증대와 기존 고객의 충성도 향상
    - 구현계획 마이크로 서비스: 기존 customer 마이크로 서비스를 인수하며, 고객에 음식 및 맛집 추천 서비스 등을 제공할 예정

## 이벤트 스토밍 
    ![image](https://user-images.githubusercontent.com/487999/79685356-2b729180-8273-11ea-9361-a434065f2249.png)


## 헥사고날 아키텍처 변화 

![image](https://user-images.githubusercontent.com/487999/79685243-1d704100-8272-11ea-8ef6-f4869c509996.png)

## 구현  

기존의 마이크로 서비스에 수정을 발생시키지 않도록 Inbund 요청을 REST 가 아닌 Event 를 Subscribe 하는 방식으로 구현. 기존 마이크로 서비스에 대하여 아키텍처나 기존 마이크로 서비스들의 데이터베이스 구조와 관계없이 추가됨. 

## 운영과 Retirement

Request/Response 방식으로 구현하지 않았기 때문에 서비스가 더이상 불필요해져도 Deployment 에서 제거되면 기존 마이크로 서비스에 어떤 영향도 주지 않음.

* [비교] 결제 (pay) 마이크로서비스의 경우 API 변화나 Retire 시에 app(주문) 마이크로 서비스의 변경을 초래함:

예) API 변화시
```
# Order.java (Entity)

    @PostPersist
    public void onPostPersist(){

        fooddelivery.external.결제이력 pay = new fooddelivery.external.결제이력();
        pay.setOrderId(getOrderId());
        
        Application.applicationContext.getBean(fooddelivery.external.결제이력Service.class)
                .결제(pay);

                --> 

        Application.applicationContext.getBean(fooddelivery.external.결제이력Service.class)
                .결제2(pay);

    }
```

예) Retire 시
```
# Order.java (Entity)

    @PostPersist
    public void onPostPersist(){

        /**
        fooddelivery.external.결제이력 pay = new fooddelivery.external.결제이력();
        pay.setOrderId(getOrderId());
        
        Application.applicationContext.getBean(fooddelivery.external.결제이력Service.class)
                .결제(pay);

        **/
    }
```


