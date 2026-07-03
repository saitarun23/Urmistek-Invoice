package com.example.backend_invoice_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "backend")
@EntityScan(basePackages = "backend.com.entity")
@EnableJpaRepositories(basePackages = "backend.com.repository")
public class BackendInvoiceAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendInvoiceAppApplication.class, args);
		System.err.println("backend microservice up!  8181");
	}

}
