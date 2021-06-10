package global.bizdevelope.realmapay.service;

import global.bizdevelope.realmapay.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApproveService {

    @Autowired
    ApprovedRepository approvedRepository;

    @Autowired
    PayRequestRepository payRequestRepository;

    @Autowired
    UserRepository userRepository;




    public List<Approved> findAll(){
        List<Approved> approvedList = new ArrayList<>();
        approvedRepository.findAll().forEach(e -> approvedList.add(e));
        return approvedList;
    }

    @Autowired
    private KafkaTemplate<String , String> kafkaTemplate;

    public void publish(String message) {
        this.kafkaTemplate.send("topic1", message);
    }

    @Transactional
    public boolean approveCheck(User user){
        PayRequest payRequest = new PayRequest();
        payRequest.setCustomerId(user.getUserId());
        publish(user.getUserId()+":PayRequest:"+payRequest.getId());

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

    private void saveApproveInfo(User user){
        Approved approved = new Approved();
        approved.setCustomerId(user.getUserId());
        approvedRepository.save(approved);
    }

    public String cancelProcedure(User user) {
        User savedUser=null;
        try{
            savedUser=userRepository.findByUserId(user.getUserId());
        }catch (Exception e){
            return "pending";
        }
        approvedRepository.deleteByCustomerId(savedUser.getUserId());
        return "cancelled";

    }
}
