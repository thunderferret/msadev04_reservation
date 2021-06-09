package global.bizdevelope.realmanapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

public class Cancelled {

    @Autowired
    private KafkaTemplate<String , String> kafkaTemplate;

    public Cancelled(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String message) {
        this.kafkaTemplate.send("topic1", message);
    }
}
