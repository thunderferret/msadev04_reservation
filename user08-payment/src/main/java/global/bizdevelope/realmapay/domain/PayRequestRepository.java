package global.bizdevelope.realmapay.domain;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayRequestRepository extends JpaRepository<PayRequest, Long>{
    public PayRequest findByCustomerId(String customerId);
}
