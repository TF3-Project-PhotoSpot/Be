package com.tf4.photospot.bookmark.domain;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tf4.photospot.bookmark.application.request.CreateBookmark;
import com.tf4.photospot.global.exception.domain.BookmarkErrorCode;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.user.domain.User;

class BookmarkFolderTest {
	@DisplayName("폴더에 북마크를 추가한다.")
	@Test
	void addBookmark() {
		//given
		final Spot spot = Spot.builder().build();
		var user = User.builder().id(1L).nickname("nickname").build();
		var bookmarkFolder = BookmarkFolder.builder().user(user).name("name").build();
		var createBookmark = CreateBookmark.builder().userId(user.getId()).spotId(1L).name("name").build();
		//when then
		assertDoesNotThrow(() -> bookmarkFolder.add(createBookmark, spot));
	}

	@DisplayName("폴더에 북마크를 추가하면 count가 1 증가한다.")
	@Test
	void addBookmarkCount() {
		//given
		final Spot spot = Spot.builder().build();
		var user = User.builder().id(1L).nickname("nickname").build();
		var bookmarkFolder = BookmarkFolder.builder().user(user).name("name").build();
		var createBookmark = CreateBookmark.builder().userId(user.getId()).spotId(1L).name("name").build();
		final int beforeTotalCount = bookmarkFolder.getTotalCount();
		//when
		bookmarkFolder.add(createBookmark, spot);
		//then
		final int afterTotalCount = bookmarkFolder.getTotalCount();
		assertThat(beforeTotalCount + 1).isEqualTo(afterTotalCount);
	}

	@DisplayName("폴더에 최대 북마크 개수를 초과해서 추가할 수 없다.")
	@Test
	void canNotBookmarkOverMaxBookmarkCount() {
		//given
		final int maxBookmarked = 200;
		final Spot spot = Spot.builder().build();
		var user = User.builder().id(1L).nickname("nickname").build();
		var bookmarkFolder = BookmarkFolder.builder().user(user).name("name").totalCount(maxBookmarked).build();
		var createBookmark = CreateBookmark.builder().userId(user.getId()).spotId(1L).name("name").build();
		//when then
		assertThatThrownBy(() -> bookmarkFolder.add(createBookmark, spot))
			.extracting("errorCode")
			.isEqualTo(BookmarkErrorCode.MAX_BOOKMARKED);
	}

	@DisplayName("다른 유저의 폴더에 북마크를 추가할 수 없다.")
	@Test
	void canNotBookmarkToOtherUserFolder() {
		//given
		final Spot spot = Spot.builder().build();
		var user = User.builder().id(1L).nickname("nickname").build();
		var ohterUser = User.builder().id(2L).nickname("nickname").build();
		var bookmarkFolder = BookmarkFolder.builder().user(ohterUser).name("name").build();
		var createBookmark = CreateBookmark.builder().userId(user.getId()).spotId(1L).name("name").build();
		//when then
		assertThatThrownBy(() -> bookmarkFolder.add(createBookmark, spot))
			.extracting("errorCode")
			.isEqualTo(BookmarkErrorCode.NO_AUTHORITY_BOOKMARK_FOLDER);
	}
}