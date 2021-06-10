package global.bizdevelope.realmapay.domain;


import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
public class PayRequest{

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    Long Id;

    String customerId;
    String paymentStatus;
}
