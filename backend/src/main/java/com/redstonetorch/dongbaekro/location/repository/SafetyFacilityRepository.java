package com.redstonetorch.dongbaekro.location.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.redstonetorch.dongbaekro.common.enums.SafetyFacilityType;
import com.redstonetorch.dongbaekro.location.entity.SafetyFacility;

@RepositoryRestResource(exported = false)
public interface SafetyFacilityRepository extends JpaRepository<SafetyFacility, Long> {
	List<SafetyFacility> findByCode(String code);
	List<SafetyFacility> findByCodeAndType(String code, SafetyFacilityType type);

	@Query("""
		SELECT sf FROM SafetyFacility sf 
		WHERE (6371000 * acos(
			cos(radians(:latitude)) * cos(radians(sf.latitude)) * 
			cos(radians(sf.longitude) - radians(:longitude)) + 
			sin(radians(:latitude)) * sin(radians(sf.latitude))
		)) <= :radiusMeters
		ORDER BY (6371000 * acos(
			cos(radians(:latitude)) * cos(radians(sf.latitude)) * 
			cos(radians(sf.longitude) - radians(:longitude)) + 
			sin(radians(:latitude)) * sin(radians(sf.latitude))
		))
		""")
	List<SafetyFacility> findFacilitiesWithinRadius(
		@Param("latitude") double latitude,
		@Param("longitude") double longitude,
		@Param("radiusMeters") double radiusMeters
	);
}
