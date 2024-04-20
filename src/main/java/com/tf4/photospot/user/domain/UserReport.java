package com.tf4.photospot.user.domain;

import com.tf4.photospot.global.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	indexes = @Index(name = "user_report_unique_inx", columnList = "reporter_id, offender_id", unique = true)
)
public class UserReport extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "reporter_id")
	private User reporter;

	@ManyToOne
	@JoinColumn(name = "offender_id")
	private User offender;

	@Builder
	public UserReport(User reporter, User offender) {
		this.reporter = reporter;
		this.offender = offender;
	}
}
