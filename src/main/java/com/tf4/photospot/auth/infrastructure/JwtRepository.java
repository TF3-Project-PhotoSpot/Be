package com.tf4.photospot.auth.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tf4.photospot.auth.domain.jwt.RefreshToken;

@Repository
public interface JwtRepository extends JpaRepository<RefreshToken, Long> {

}
