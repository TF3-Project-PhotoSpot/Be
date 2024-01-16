package com.tf4.photospot.spot.domain;

import java.util.ArrayList;
import java.util.List;

import com.tf4.photospot.global.entity.BaseEntity;
import com.tf4.photospot.user.domain.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookmarkFolder extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToMany(mappedBy = "bookmarkFolder")
	private List<SpotBookmark> spotBookmarks = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	private String name;

	private String color;

	@Builder
	private BookmarkFolder(User user, String name, String color) {
		this.user = user;
		this.name = name;
		this.color = color;
	}

	public static BookmarkFolder createDefaultBookmark(User user) {
		BookmarkFolder bookmarkFolder = BookmarkFolder.builder()
			.user(user)
			.name("기본 폴더")
			.build();
		user.getBookmarkFolders().add(bookmarkFolder);
		return bookmarkFolder;
	}
}
