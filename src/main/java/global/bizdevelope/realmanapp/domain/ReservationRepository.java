package global.bizdevelope.realmanapp.domain;

import global.bizdevelope.realmanapp.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    public List<Reservation> findByCustomerId(String customerId);

    public void deleteByCustomerId(String customerId);
}
