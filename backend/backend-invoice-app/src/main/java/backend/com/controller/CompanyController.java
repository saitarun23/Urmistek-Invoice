package backend.com.controller;

import backend.com.dto.CompanyDetailDTO;
import backend.com.dto.CompanyOptionDTO;
import backend.com.entity.Company;
import backend.com.repository.CompanyRepository;
import backend.com.security.CompanyPrincipal;
import backend.com.service.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@CrossOrigin
public class CompanyController {

    private final AuthService authService;
    private final CompanyRepository companyRepository;

    public CompanyController(AuthService authService, CompanyRepository companyRepository) {
        this.authService = authService;
        this.companyRepository = companyRepository;
    }

    // Public - the login page calls this first to render the URMISTEK / UB Industries tiles
    @GetMapping
    public List<CompanyOptionDTO> listCompanies() {
        return authService.listCompanies();
    }

    // Authenticated - the fixed (read-only) branding shown on the invoice
    // preview: company name/address/GSTIN/HSN/tagline + bank details.
    // Never editable from the frontend; it's exactly what's already
    // printed on this company's own invoices.
    @GetMapping("/me")
    public CompanyDetailDTO myCompany(@AuthenticationPrincipal CompanyPrincipal principal) {
        Company c = companyRepository.findById(principal.companyId())
                .orElseThrow(() -> new IllegalStateException("Unknown company"));
        return new CompanyDetailDTO(
                c.getCode(), c.getName(), c.getAddress(), c.getGstin(), c.getHsn(),
                c.getTaglineTabs(), c.getCurrencyLabel(),
                c.getBankAccountName(), c.getBankName(), c.getBankAccountNumber(), c.getBankIfsc()
        );
    }
}
