package global.bizdevelope.realmanapp.domain;


import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity(name="reservations")
public class Reservation {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;


    Long ticketNumber;
    Date concertDate;
    String customerName;
    String customerId;
    String reservationStatus;
}
