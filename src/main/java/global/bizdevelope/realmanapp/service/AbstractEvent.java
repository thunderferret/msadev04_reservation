package global.bizdevelope.realmanapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import global.bizdevelope.realmanapp.ReservationApplication;
import global.bizdevelope.realmanapp.service.config.KafkaProcessor;
import lombok.Data;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.MimeTypeUtils;

import java.text.SimpleDateFormat;
import java.util.Date;


@Data
public class AbstractEvent {
    String eventType;
    String timestamp;

    public AbstractEvent(){
        this.setEventType(this.getClass().getSimpleName());
        SimpleDateFormat defaultSimpleDateFormat = new SimpleDateFormat("YYYYMMddHHmmss");
        this.timestamp = defaultSimpleDateFormat.format(new Date());
    }
    public String toJson(){
        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;

        try {
            json = objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON format exception", e);
        }
        return json;
    }


    public void publish(String json){
        if( json != null ){
            KafkaProcessor processor = ReservationApplication.applicationContext.getBean(KafkaProcessor.class);
            MessageChannel outputChannel = processor.outboundTopic();

            outputChannel.send(MessageBuilder
                    .withPayload(json)
                    .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                    .build());
        }
    }

    public void publish(){
        this.publish(this.toJson());
    }



}
