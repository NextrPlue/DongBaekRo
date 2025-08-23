package com.redstonetorch.dongbaekro.location.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoRegionResponse(
	Meta meta,
	List<Document> documents
) {
	public record Meta(
		@JsonProperty("total_count")
		int totalCount
	) {
	}

	public record Document(
		@JsonProperty("region_type")
		String regionType,
		@JsonProperty("address_name")
		String addressName,
		@JsonProperty("region_1depth_name")
		String region1DepthName,
		@JsonProperty("region_2depth_name")
		String region2DepthName,
		@JsonProperty("region_3depth_name")
		String region3DepthName,
		@JsonProperty("region_4depth_name")
		String region4DepthName,
		String code,
		double x,
		double y
	) {
	}
}