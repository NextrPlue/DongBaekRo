package com.redstonetorch.dongbaekro.emergencyReport.dto.response;

import java.time.LocalDateTime;

import com.redstonetorch.dongbaekro.emergencyReport.entity.EmergencyReport;

public record EmergencyReportResponse(
	Long id,
	Double latitude,
	Double longitude,
	Boolean status,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static EmergencyReportResponse from(EmergencyReport report) {
		return new EmergencyReportResponse(
			report.getId(),
			report.getLatitude(),
			report.getLongitude(),
			report.getStatus(),
			report.getCreatedAt(),
			report.getUpdatedAt()
		);
	}
}