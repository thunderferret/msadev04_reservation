package global.bizdevelope.realmanapp;

import global.bizdevelope.realmanapp.service.config.KafkaProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.ApplicationContext;

@EnableFeignClients
@EnableBinding(KafkaProcessor.class)
@SpringBootApplication
public class ReservationApplication {

    public static ApplicationContext applicationContext;

    public static void main(String[] args) {

        SpringApplication.run(ReservationApplication.class, args);
    }

}
