package repository.jpa;

import java.util.List;
import models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
  public List<Customer> getByGuardian(Customer guardian);
}
