package com.redstonetorch.dongbaekro.location.entity;

import com.redstonetorch.dongbaekro.common.enums.SafetyFacilityType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
	private String address_name;

	@Column(nullable = false)
	private String region_1depth_name;

	@Column(nullable = false)
	private String region_2depth_name;

	@Column(nullable = false)
	private String region_3depth_name;

	@Column(nullable = false)
	private String mountain_yn;

	@Column(nullable = false)
	private String main_address_no;

	@Column(nullable = false)
	private String sub_address_no;

	@Builder
	public SafetyFacility(SafetyFacilityType type, String name, Double latitude, Double longitude, String address_name,
		String region_1depth_name, String region_2depth_name, String region_3depth_name, String mountain_yn,
		String main_address_no, String sub_address_no) {
		this.type = type;
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.address_name = address_name;
		this.region_1depth_name = region_1depth_name;
		this.region_2depth_name = region_2depth_name;
		this.region_3depth_name = region_3depth_name;
		this.mountain_yn = mountain_yn;
		this.main_address_no = main_address_no;
		this.sub_address_no = sub_address_no;
	}
}
