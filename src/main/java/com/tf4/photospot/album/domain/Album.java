package com.tf4.photospot.album.domain;

import com.tf4.photospot.global.entity.BaseEntity;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AlbumErrorCode;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Album extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	public Album(String name) {
		this.name = name;
	}

	public void rename(AlbumUser albumUser, String newName) {
		if (albumUser.getAlbum() != this) {
			throw new ApiException(AlbumErrorCode.NO_AUTHORITY_ALBUM);
		}
		name = newName;
	}
}
