package com.tf4.photospot.support;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.locationtech.jts.geom.Point;

import com.tf4.photospot.album.domain.Album;
import com.tf4.photospot.bookmark.domain.Bookmark;
import com.tf4.photospot.bookmark.domain.BookmarkFolder;
import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.global.util.PointConverter;
import com.tf4.photospot.photo.domain.Bubble;
import com.tf4.photospot.photo.domain.Photo;
import com.tf4.photospot.post.domain.Mention;
import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostLike;
import com.tf4.photospot.post.domain.PostTag;
import com.tf4.photospot.post.domain.Tag;
import com.tf4.photospot.post.domain.TagType;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.user.domain.User;

public class TestFixture {
	private static final AtomicLong COORD_UNIQUE_KEY = new AtomicLong(1L);
	private static final Double COORD_UNIT_VALUE = 0.0001;
	private static final Random RANDOM = new Random();
	private static final int LIKE_COUNT_RANGE = 100;

	public static Spot createSpot(String address, Point coord, Long postCount) {
		return Spot.builder()
			.address(address)
			.coord(coord)
			.postCount(postCount)
			.build();
	}

	public static Spot createSpot() {
		return createSpot("주소", createPoint(), 0L);
	}

	public static Spot createSpot(CoordinateDto coord) {
		return Spot.builder()
			.address("주소")
			.coord(coord.toCoord())
			.postCount(0L)
			.build();
	}

	public static Post createPost(Spot spot, User user) {
		return createPost(spot, user, 0);
	}

	public static Post createPost(Spot spot, User user, boolean isPrivate) {
		return createPost(spot, user, createPhoto(), 0, isPrivate);
	}

	public static Post createPost(Spot spot, User user, int likeCount) {
		return createPost(spot, user, createPhoto(), likeCount, false);
	}

	public static Post createPost(Spot spot, User user, Photo photo) {
		return createPost(spot, user, photo, 0, false);
	}

	public static Post createPost(Spot spot, User user, Photo photo, int likeCount, boolean isPrivate) {
		spot.incPostCount();
		return Post.builder()
			.spot(spot)
			.writer(user)
			.photo(photo)
			.detailAddress("디테일 주소")
			.likeCount(likeCount)
			.isPrivate(isPrivate)
			.build();
	}

	public static long createRandomLikeCount() {
		return RANDOM.nextInt(LIKE_COUNT_RANGE);
	}

	public static Photo createPhoto(String photoUrl, LocalDateTime takenAt) {
		return Photo.builder()
			.photoUrl(photoUrl)
			.coord(createPoint())
			.takenAt(takenAt)
			.build();
	}

	public static Photo createPhotoWithBubble(String photoUrl, String text, long posX, long posY) {
		return Photo.builder()
			.photoUrl(photoUrl)
			.bubble(createBubble(text, posX, posY))
			.coord(createPoint())
			.build();
	}

	public static Photo createPhoto(String photoUrl) {
		return createPhoto(photoUrl, LocalDateTime.now().minusDays(1));
	}

	public static Photo createPhoto() {
		return createPhoto("photoUrl");
	}

	public static Bubble createBubble(String text, long posX, long posY) {
		return Bubble.builder()
			.text(text)
			.posX(posX)
			.posY(posY)
			.build();
	}

	public static User createUser(String nickname, String account, String providerType) {
		return User.builder()
			.nickname(nickname)
			.account(account)
			.providerType(providerType)
			.build();
	}

	public static User createUser(String nickname) {
		return createUser(nickname, "123456", "kakao");
	}

	public static Bookmark createSpotBookmark(User user, Spot spot, BookmarkFolder bookmarkFolder) {
		return Bookmark.builder()
			.spot(spot)
			.bookmarkFolder(bookmarkFolder)
			.build();
	}

	public static <T> List<T> createList(Supplier<T> construct, int total) {
		return IntStream.range(0, total)
			.mapToObj(i -> construct.get())
			.toList();
	}

	public static Point createPoint() {
		final double key = COORD_UNIQUE_KEY.getAndIncrement();
		return PointConverter.convert(
			124.000 + (key * COORD_UNIT_VALUE),
			33.000 + (key * COORD_UNIT_VALUE)
		);
	}

	public static List<Tag> createTags(String... tagNames) {
		return Arrays.stream(tagNames).map(TestFixture::createTag).toList();
	}

	public static Tag createTag(String tagName) {
		return createTag(tagName, TagType.ETC);
	}

	public static Tag createTag(String tagName, TagType tagType) {
		return Tag.builder()
			.name(tagName)
			.tagType(tagType)
			.iconUrl("iconUrl")
			.build();
	}

	public static PostTag createPostTag(Spot spot, Post post, Tag tag) {
		return PostTag.builder()
			.spot(spot)
			.post(post)
			.tag(tag)
			.build();
	}

	public static List<PostTag> createPostTags(Spot spot, Post post, List<Tag> tags) {
		return tags.stream()
			.map(tag -> createPostTag(spot, post, tag))
			.toList();
	}

	public static PostLike createPostLike(Post post, User user) {
		return PostLike.builder()
			.post(post)
			.user(user)
			.build();
	}

	public static Mention createMention(Post post, User mentionedUser) {
		return new Mention(post, mentionedUser);
	}

	public static List<Mention> createMentions(Post post, List<User> mentionedUsers) {
		return mentionedUsers.stream()
			.map(user -> createMention(post, user))
			.toList();
	}

	public static Album createAlbum() {
		return new Album("album");
	}

	public static BookmarkFolder createBookmarkFolder(User user, String name) {
		return BookmarkFolder.builder()
			.user(user)
			.name(name)
			.description("description")
			.color("color")
			.build();
	}

	public static Bookmark createBookmark(BookmarkFolder bookmarkFolder, Spot spot) {
		return Bookmark.builder()
			.bookmarkFolder(bookmarkFolder)
			.spot(spot)
			.name("name")
			.build();
	}

	public static String createDummyStr(int length) {
		final StringBuilder dummyStr = new StringBuilder();
		IntStream.range(0, length).forEach(i -> dummyStr.append("a"));
		return dummyStr.toString();
	}
}
