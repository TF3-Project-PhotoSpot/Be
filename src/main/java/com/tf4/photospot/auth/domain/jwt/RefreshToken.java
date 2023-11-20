package com.tf4.photospot.auth.domain.jwt;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "token")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {

	@Id
	private Long userId;

	private String refreshToken;

	public boolean isTokenMatching(String refreshToken) {
		return Objects.equals(this.refreshToken, refreshToken);
	}
}
