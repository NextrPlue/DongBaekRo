package com.redstonetorch.dongbaekro.location.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.redstonetorch.dongbaekro.location.dto.response.SafetyFacilityResponse;
import com.redstonetorch.dongbaekro.location.repository.SafetyFacilityRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SafetyFacilityService {

	private final SafetyFacilityRepository safetyFacilityRepository;

	public List<SafetyFacilityResponse> findByCode(String code) {
		return safetyFacilityRepository.findByCode(code)
			.stream()
			.map(SafetyFacilityResponse::from)
			.toList();
	}
}