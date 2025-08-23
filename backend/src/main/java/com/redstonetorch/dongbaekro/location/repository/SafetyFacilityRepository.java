package com.redstonetorch.dongbaekro.location.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.redstonetorch.dongbaekro.location.entity.SafetyFacility;

@RepositoryRestResource(exported = false)
public interface SafetyFacilityRepository extends JpaRepository<SafetyFacility, Long> {
	List<SafetyFacility> findByCode(String code);
}
