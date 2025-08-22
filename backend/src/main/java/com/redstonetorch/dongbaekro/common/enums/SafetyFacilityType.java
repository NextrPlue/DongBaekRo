package com.redstonetorch.dongbaekro.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SafetyFacilityType {
	CCTV("CCTV"),
	STREETLAMP("가로등"),
	POLICE_SUBSTATION("파출소"),
	INFORMATION_BOARD("안내판"),
	EMERGENCY_BELL("비상벨"),
	SAFE_DELIVERY_BOX("안심 택배함");

	private final String description;
}
