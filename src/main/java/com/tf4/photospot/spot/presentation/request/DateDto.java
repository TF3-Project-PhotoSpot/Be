package com.tf4.photospot.spot.presentation.request;

import java.time.LocalDate;

import com.tf4.photospot.global.argument.ValidDate;

@ValidDate
public record DateDto(
	LocalDate start,
	LocalDate end
) {
}
