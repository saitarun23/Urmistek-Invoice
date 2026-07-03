package backend.com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.com.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long>{

}
