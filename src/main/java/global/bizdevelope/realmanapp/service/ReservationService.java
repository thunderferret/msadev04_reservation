package global.bizdevelope.realmanapp.service;

import global.bizdevelope.realmanapp.ReservationApplication;
import global.bizdevelope.realmanapp.domain.Reservation;
import global.bizdevelope.realmanapp.domain.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private KafkaTemplate<String,String > template;

    private WebClient webClient = WebClient.builder().baseUrl("http://localhost:8081").build();

    public List<Reservation> findAll(){
        List<Reservation> reservations = new ArrayList<>();
        reservationRepository.findAll().forEach(e -> reservations.add(e));
        return reservations;
    }

    public void reserve(@RequestBody Reservation reservation){
        Reservation newReservation = reservationRepository.save(reservation);
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
