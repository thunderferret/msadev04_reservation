package global.bizdevelope.realmapay.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovedRepository extends JpaRepository<Approved, Long> {
    public void deleteByCustomerId(String userId);
}
