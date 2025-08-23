package com.redstonetorch.dongbaekro.emergencyReport.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.redstonetorch.dongbaekro.emergencyReport.entity.EmergencyReport;

@RepositoryRestResource(exported = false)
public interface EmergencyReportRepository extends JpaRepository<EmergencyReport, Long> {
}
