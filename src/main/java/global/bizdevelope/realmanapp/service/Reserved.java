package global.bizdevelope.realmanapp.service;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
public class Reserved extends AbstractEvent {
    private Long id;
    private String concertName;
    private String customerName;
    private String reservationId;
    private String reservationStatus;
}
