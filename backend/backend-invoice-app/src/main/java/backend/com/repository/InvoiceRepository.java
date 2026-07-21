package backend.com.repository;

import backend.com.entity.Invoice;
import backend.com.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
	Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

	// Used to work out the next running number within a company's financial year
	long countByCompany_IdAndFinancialYearLabel(Long companyId, String financialYearLabel);

	List<Invoice> findByCompany_IdAndStatusOrderByCreatedAtDesc(Long companyId, InvoiceStatus status);

	List<Invoice> findByCompany_IdAndCreatedBy_IdOrderByCreatedAtDesc(Long companyId, Long createdById);

	List<Invoice> findByCompany_IdOrderByCreatedAtDesc(Long companyId);
}
