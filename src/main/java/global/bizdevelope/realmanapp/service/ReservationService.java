package global.bizdevelope.realmanapp.service;

import global.bizdevelope.realmanapp.domain.Reservation;
import global.bizdevelope.realmanapp.domain.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;


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


}
