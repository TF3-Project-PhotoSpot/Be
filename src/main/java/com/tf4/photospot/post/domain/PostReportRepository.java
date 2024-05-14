package com.tf4.photospot.post.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {
	Optional<PostReport> findByPostId(Long postId);
}
