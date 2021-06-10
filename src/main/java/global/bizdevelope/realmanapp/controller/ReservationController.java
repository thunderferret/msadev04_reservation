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
        service.reserve(user);
        return "User Request Accepted";
    }

    @PostMapping("/cancelreservation")
    public String cancelreservation(@ModelAttribute User user){
        service.cancelReservation(user);
        return "User cancel Accepted";
    }

    @GetMapping("/reservation/posttest")
    @ResponseBody
    public String postTest(){
        String t = service.postTest();
        System.out.println(t);

        return "post Test Succeeded";
    }

    @GetMapping("/reservation/test")
    public String reservationTest(){
        User user = new User();
        user.setUserId("vpflt992@gmail.com");
        user.setCustomerName("Kim In TAE");
        service.approveCheck(user);
        return "TEST PASSED";
    }

    @GetMapping("/reservation/kafkatest")
    public String kafkaTest(){
        this.reserved.publish("FUFUFUFUF DKDKDKDKKDKD");
        return "Kafka Published";
    }


}
