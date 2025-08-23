package com.redstonetorch.dongbaekro.emergencyReport.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.redstonetorch.dongbaekro.auth.entity.User;
import com.redstonetorch.dongbaekro.emergencyReport.entity.EmergencyReport;

@RepositoryRestResource(exported = false)
public interface EmergencyReportRepository extends JpaRepository<EmergencyReport, Long> {
	Page<EmergencyReport> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
