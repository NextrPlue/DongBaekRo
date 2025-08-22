package com.redstonetorch.dongbaekro.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.redstonetorch.dongbaekro.auth.entity.RefreshToken;
import com.redstonetorch.dongbaekro.auth.entity.User;

@RepositoryRestResource(exported = false)
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	Optional<RefreshToken> findByRefreshTokenAndRevoked(String refreshToken, boolean revoked);

	List<RefreshToken> findByUserAndRevoked(User user, boolean revoked);
}
