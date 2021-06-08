package global.bizdevelope.realmanapp.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class Reserved {
    @Autowired
    private KafkaTemplate<String , String> kafkaTemplate;

    public void publish(String message) {
        this.kafkaTemplate.send("topic1", message);
    }
}
