package global.bizdevelope.realmanapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;


@EnableAutoConfiguration(exclude= KafkaAutoConfiguration.class)
@SpringBootTest
class ReservationApplicationTests {

    @Test
    void contextLoads() {
    }

}
