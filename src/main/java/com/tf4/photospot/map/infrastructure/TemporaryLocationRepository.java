package com.tf4.photospot.map.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tf4.photospot.map.domain.TemporaryLocation;

public interface TemporaryLocationRepository extends JpaRepository<TemporaryLocation, Long> {
}
