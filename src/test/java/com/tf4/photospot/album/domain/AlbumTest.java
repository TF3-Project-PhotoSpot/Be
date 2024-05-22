package com.tf4.photospot.album.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tf4.photospot.global.exception.domain.AlbumErrorCode;
import com.tf4.photospot.user.domain.User;

class AlbumTest {
	@DisplayName("앨범 이름을 변경한다.")
	@Test
	void renameAlbum() {
		//given
		final User user = new User(1L, "nickname", "kakao", "asads");
		final Album album = new Album("name");
		final AlbumUser albumUser = new AlbumUser(user, album);
		//when
		album.rename(albumUser, "newName");
		//then
		assertThat(album.getName()).isEqualTo("newName");
	}

	@DisplayName("앨범 유저만 이름을 변경할 수 있다.")
	@Test
	void onlyMemberCanRenameAlbum() {
		//given
		final User user = new User(1L, "nickname", "kakao", "asads");
		final Album album = new Album("name");
		final Album otherAlbum = new Album("name");
		final AlbumUser otherAlbumUser = new AlbumUser(user, otherAlbum);
		//when
		assertThatThrownBy(() -> album.rename(otherAlbumUser, "newName"))
			.extracting("errorCode")
			.isEqualTo(AlbumErrorCode.NO_AUTHORITY_ALBUM);
	}
}