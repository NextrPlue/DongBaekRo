package com.redstonetorch.dongbaekro.emergencyReport.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.redstonetorch.dongbaekro.emergencyReport.entity.EmergencyReport;

public interface EmergencyReportRepository extends JpaRepository<EmergencyReport, Long> {
}
