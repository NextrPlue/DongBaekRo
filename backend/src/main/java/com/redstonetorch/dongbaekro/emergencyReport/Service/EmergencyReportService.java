package com.redstonetorch.dongbaekro.emergencyReport.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.redstonetorch.dongbaekro.auth.entity.User;
import com.redstonetorch.dongbaekro.auth.repository.UserRepository;
import com.redstonetorch.dongbaekro.common.exception.CustomException;
import com.redstonetorch.dongbaekro.common.exception.ErrorCode;
import com.redstonetorch.dongbaekro.emergencyReport.dto.request.EmergencyReportCreateRequest;
import com.redstonetorch.dongbaekro.emergencyReport.dto.response.EmergencyReportResponse;
import com.redstonetorch.dongbaekro.emergencyReport.entity.EmergencyReport;
import com.redstonetorch.dongbaekro.emergencyReport.repository.EmergencyReportRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmergencyReportService {

	private final EmergencyReportRepository emergencyReportRepository;
	private final UserRepository userRepository;

	@Transactional
	public EmergencyReportResponse createEmergencyReport(Long userId, EmergencyReportCreateRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.AUTH_USER_NOT_FOUND));

		EmergencyReport report = EmergencyReport.builder()
			.user(user)
			.latitude(request.latitude())
			.longitude(request.longitude())
			.build();

		EmergencyReport savedReport = emergencyReportRepository.save(report);
		return EmergencyReportResponse.from(savedReport);
	}

	@Transactional
	public EmergencyReportResponse updateEmergencyReportStatus(Long reportId) {
		EmergencyReport report = emergencyReportRepository.findById(reportId)
			.orElseThrow(() -> new CustomException(ErrorCode.EMERGENCY_REPORT_NOT_FOUND));

		report.updateStatus();
		return EmergencyReportResponse.from(report);
	}

	public EmergencyReportResponse getEmergencyReport(Long reportId) {
		EmergencyReport report = emergencyReportRepository.findById(reportId)
			.orElseThrow(() -> new CustomException(ErrorCode.EMERGENCY_REPORT_NOT_FOUND));

		return EmergencyReportResponse.from(report);
	}

	public Page<EmergencyReportResponse> getAllEmergencyReports(Pageable pageable) {
		Page<EmergencyReport> reports = emergencyReportRepository.findAll(pageable);
		return reports.map(EmergencyReportResponse::from);
	}

	public Page<EmergencyReportResponse> getUserEmergencyReports(Long userId, Pageable pageable) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.AUTH_USER_NOT_FOUND));

		Page<EmergencyReport> reports = emergencyReportRepository.findByUserOrderByCreatedAtDesc(user, pageable);
		return reports.map(EmergencyReportResponse::from);
	}
}
