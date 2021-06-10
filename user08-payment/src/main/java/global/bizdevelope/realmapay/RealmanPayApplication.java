package global.bizdevelope.realmapay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RealmanPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(RealmanPayApplication.class, args);
        System.out.println("Payment Part Loaded Successfully");

    }

}
