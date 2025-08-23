package com.redstonetorch.dongbaekro.emergencyReport.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.redstonetorch.dongbaekro.common.dto.response.ApiResponse;
import com.redstonetorch.dongbaekro.common.dto.response.PagedResponse;
import com.redstonetorch.dongbaekro.emergencyReport.Service.EmergencyReportService;
import com.redstonetorch.dongbaekro.emergencyReport.dto.request.EmergencyReportCreateRequest;
import com.redstonetorch.dongbaekro.emergencyReport.dto.response.EmergencyReportResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/emergency-reports")
@RequiredArgsConstructor
public class EmergencyReportController {

	private final EmergencyReportService emergencyReportService;

	@PostMapping
	public ResponseEntity<ApiResponse<EmergencyReportResponse>> createEmergencyReport(
		@Valid @RequestBody EmergencyReportCreateRequest request,
		Authentication authentication
	) {
		Long userId = Long.valueOf(authentication.getName());
		EmergencyReportResponse response = emergencyReportService.createEmergencyReport(userId, request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PutMapping("/{reportId}/status")
	public ResponseEntity<ApiResponse<EmergencyReportResponse>> updateEmergencyReportStatus(
		@PathVariable Long reportId
	) {
		EmergencyReportResponse response = emergencyReportService.updateEmergencyReportStatus(reportId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/{reportId}")
	public ResponseEntity<ApiResponse<EmergencyReportResponse>> getEmergencyReport(
		@PathVariable Long reportId
	) {
		EmergencyReportResponse response = emergencyReportService.getEmergencyReport(reportId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PagedResponse<EmergencyReportResponse>>> getAllEmergencyReports(
		@PageableDefault(size = 20) Pageable pageable
	) {
		Page<EmergencyReportResponse> reports = emergencyReportService.getAllEmergencyReports(pageable);
		PagedResponse<EmergencyReportResponse> response = PagedResponse.of(reports);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/my")
	public ResponseEntity<ApiResponse<PagedResponse<EmergencyReportResponse>>> getUserEmergencyReports(
		@PageableDefault(size = 20) Pageable pageable,
		Authentication authentication
	) {
		Long userId = Long.valueOf(authentication.getName());
		Page<EmergencyReportResponse> reports = emergencyReportService.getUserEmergencyReports(userId, pageable);
		PagedResponse<EmergencyReportResponse> response = PagedResponse.of(reports);
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}