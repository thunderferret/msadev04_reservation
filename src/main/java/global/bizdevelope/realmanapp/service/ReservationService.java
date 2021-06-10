package global.bizdevelope.realmanapp.service;

import global.bizdevelope.realmanapp.domain.Reservation;
import global.bizdevelope.realmanapp.domain.ReservationRepository;
import global.bizdevelope.realmanapp.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.transaction.Transactional;
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

    private WebClient webClient = WebClient.builder().baseUrl("http://localhost:8081").build();

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

        if(approveCheck().equals("Accepted")){
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


    public String approveCheck(){
        return webClient.get()
                .uri("/payment/payrequest")
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
