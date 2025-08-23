package com.redstonetorch.dongbaekro.emergencyReport.dto.request;

import jakarta.validation.constraints.NotNull;

public record EmergencyReportCreateRequest(
	@NotNull
	Double latitude,

	@NotNull
	Double longitude
) {
}
