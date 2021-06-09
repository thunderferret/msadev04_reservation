package global.bizdevelope.realmanapp.controller;


import global.bizdevelope.realmanapp.domain.Reservation;
import global.bizdevelope.realmanapp.domain.User;
import global.bizdevelope.realmanapp.service.ReservationService;
import global.bizdevelope.realmanapp.service.Reserved;
import org.hibernate.usertype.UserVersionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class ReservationController {

    private final Reserved reserved;

    @Autowired
    ReservationController(Reserved reserved){
        this.reserved = reserved;
    }

    @Autowired
    ReservationService service;

    @GetMapping("/reservation/customerlists")
    public List getAllReservationLists(HttpServletRequest req){
        return service.findAll();
    }

    @PostMapping("/reservationreq")
    public String reservation(@ModelAttribute User user){
        Reservation reservation = new Reservation();
        reservation.setCustomerName(user.getCustomerName());
        reservation.setCustomerId(user.getUserId());
        service.reserve(reservation);
        return "User Request Accepted";
    }



    @GetMapping("/reservation/test")
    public String reservationTest(){
        service.reservationTest();
        System.out.println("YEAHEYEHAEYEHAEYEAHE");
        return "OK OK";
    }

    @GetMapping("/reservation/kafkatest")
    public String kafkaTest(){
        this.reserved.publish("FUFUFUFUF DKDKDKDKKDKD");
        return "Kafka Published";
    }





}
