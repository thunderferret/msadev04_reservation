package global.bizdevelope.realmanapp.service;

import global.bizdevelope.realmanapp.domain.Reservation;
import global.bizdevelope.realmanapp.domain.ReservationRepository;
import global.bizdevelope.realmanapp.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.transaction.Transactional;
import java.awt.*;
import java.net.http.HttpHeaders;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private KafkaTemplate<String,String > template;

    @Autowired
    private Reserved reserved;

    @Autowired
    private Cancelled cancelled;

    private WebClient webclientbase= WebClient.create();
    private WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8082")
            .build();


    public String postTest(){
       return webClient.post()
                .uri("/posttest")
                .bodyValue("POST TEST")
                .retrieve()
                .bodyToMono(String.class)
        .block();
    }
    public List<Reservation> findAll(){
        List<Reservation> reservations = new ArrayList<>();
        reservationRepository.findAll().forEach(e -> reservations.add(e));
        return reservations;
    }

    @Transactional
    public Reservation reserve(User user){
        Reservation reservation = new Reservation();
        reservation.setCustomerName(user.getCustomerName());
        reservation.setCustomerId(user.getUserId());

        if(approveCheck(user).equals("Accepted")){
            reservation.setReservationStatus("Reserved");
            reservationRepository.save(reservation);
        }
        reservation.setReservationStatus("Pending");
        reservationRepository.save(reservation);
        reserved.publish(user.getUserId()+":Reserved:"+reservation.getId());
        return reservation;
    }


    @Transactional
    public String cancelReservation(User user){
        List<Reservation> reservationList;

        try{
        reservationList=reservationRepository.findByCustomerId(user.getUserId());
        if(reservationList==null) {
            return "Refused";
        }
        for (Reservation r:reservationList){
            cancelled.publish(user.getUserId()+":Cancelled:"+r.getId().toString());
            reservationRepository.deleteByCustomerId(r.getId().toString());
        }
        }catch (Exception e){
            return "Error";
        }
        return "Cancelled";
    }


    public String approveCheck(User user){
        return webClient.post()
                .uri("/payrequest")
                .bodyValue(user.getUserId())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }


    public String reservationTest(){
        return webClient.get()
                .uri("/payment/test")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }



    public ApplicationRunner publishMypage(){
        return args -> {
            template.send("topic1", "Publish My Page Yeahahah");
        };
    }


    @Bean
    public ApplicationRunner runner() {
        return args -> {
            template.send("topic1", "Kafka Producer is Ready");
        };
    }

}
