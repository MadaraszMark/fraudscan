package fraudscan.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import fraudscan.main.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
	
}
