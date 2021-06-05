package global.bizdevelope.realmanapp.controller;


import global.bizdevelope.realmanapp.domain.Reservation;
import global.bizdevelope.realmanapp.domain.User;
import global.bizdevelope.realmanapp.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class ReservationController {


    @Autowired ReservationService service;

    @GetMapping("/reservation/customerlists")
    public List getAllReservationLists(HttpServletRequest req){
        return service.findAll();
    }

    @PostMapping("/reservationreq")
    public String reservation(@ModelAttribute User user){


        System.out.println(user);


        Reservation reservation = new Reservation();
        reservation.setCustomerName(user.getCustomerName());
        reservation.setCustomerId(user.getUserId());
        service.reserve(reservation);
        return "User Request Accepted";
    }

}
