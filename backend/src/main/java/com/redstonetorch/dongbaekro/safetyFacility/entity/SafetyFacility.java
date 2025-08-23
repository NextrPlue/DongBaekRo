package com.redstonetorch.dongbaekro.safetyFacility.entity;

import java.time.LocalDateTime;

import com.redstonetorch.dongbaekro.common.enums.SafetyFacilityType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "safety_facilities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SafetyFacility {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private SafetyFacilityType type;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Double latitude;

	@Column(nullable = false)
	private Double longitude;

	@Column(nullable = false)
	private String address;

	@Builder
	public SafetyFacility(SafetyFacilityType type, String name, Double latitude, Double longitude, String address) {
		this.type = type;
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.address = address;
	}
}
