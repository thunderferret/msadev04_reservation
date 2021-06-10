package global.bizdevelope.realmapay.controller;


import global.bizdevelope.realmapay.domain.PayRequest;
import global.bizdevelope.realmapay.domain.TicketUser;
import global.bizdevelope.realmapay.domain.User;
import global.bizdevelope.realmapay.service.ApproveService;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.sound.midi.SysexMessage;
import java.net.http.HttpResponse;
import java.util.List;

@RestController
public class PaymentController {


    private ApproveService approveService;

    @GetMapping("/payment/test")
    public String test(){
        return "Hello MSA";
    }

    @GetMapping("/customerlist/getall}")
    public List getAllCustomerList(){
        return approveService.findAll();
    }

    @PostMapping("/posttest")
    public void postTest(@RequestBody String incoming){

        System.out.println(incoming);
    }

    @PostMapping("/payrequest")
    public String payRequest(@RequestBody String incomingUser) {
        PayRequest payRequest = new PayRequest();
        User user = new User();
        user.setUserId("sdlfkjsklfd@naver.com");
        System.out.println(incomingUser);
        /*
        payRequest.setCustomerId(incomingUser.getUserId());
        user.setUserId(incomingUser.getUserId());
        user.setCustomerName(incomingUser.getCustomerName());
        System.out.println(incomingUser.toString());
        */
        if(approveService.approveCheck(user)){
            return "Accepted";
        }
        return "Refused";
    }

    @PostMapping("/cancelrequest")
    public  String cancelRequest(@ModelAttribute User user){
        String cancelStatus;
        cancelStatus=approveService.cancelProcedure(user);
        if(cancelStatus.equals("cancelled")){
            return "cancelled";
        }else{
            return "pending";
        }
    }


    //
    @Bean
    public NewTopic topic(){
        return TopicBuilder.name("topic1")
                .partitions(10)
                .replicas(1)
                .build();
    }

    @KafkaListener(id="rumyApp",topics="topic1")
    public void listen(String in){
        System.out.println(in);
    }

    /*
    @GetMapping("/payment/customerlist")
    public List getAllCustomerList(HttpServletRequest req){
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
*/
}
