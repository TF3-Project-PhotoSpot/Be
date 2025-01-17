package com.tf4.photospot.bookmark.presentation;

import java.util.List;

import org.hibernate.validator.constraints.Range;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.bookmark.application.BookmarkService;
import com.tf4.photospot.bookmark.application.request.CreateBookmark;
import com.tf4.photospot.bookmark.application.request.CreateBookmarkFolder;
import com.tf4.photospot.bookmark.application.request.ReadBookmarkFolderList;
import com.tf4.photospot.bookmark.application.response.BookmarkFolderResponse;
import com.tf4.photospot.bookmark.application.response.BookmarkListResponse;
import com.tf4.photospot.bookmark.domain.BookmarkFolder;
import com.tf4.photospot.bookmark.presentation.request.AddBookmarkHttpRequest;
import com.tf4.photospot.bookmark.presentation.request.CreateBookmarkFolderHttpRequest;
import com.tf4.photospot.bookmark.presentation.request.ReadBookmarkRequest;
import com.tf4.photospot.bookmark.presentation.request.RemoveBookmarkHttpRequest;
import com.tf4.photospot.bookmark.presentation.request.UpdateBookmarkFolderHttpRequest;
import com.tf4.photospot.bookmark.presentation.response.AddBookmarkHttpResponse;
import com.tf4.photospot.bookmark.presentation.response.BookmarkCoordFolderHttpResponse;
import com.tf4.photospot.bookmark.presentation.response.BookmarkCoordFolderListHttpResponse;
import com.tf4.photospot.bookmark.presentation.response.BookmarkFolderListHttpResponse;
import com.tf4.photospot.bookmark.presentation.response.CreateBookmarkFolderResponse;
import com.tf4.photospot.global.argument.AuthUserId;
import com.tf4.photospot.global.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/v1/bookmarkFolders")
@RestController
@RequiredArgsConstructor
public class BookmarkController {
	private final BookmarkService bookmarkService;

	@PostMapping
	public CreateBookmarkFolderResponse createBookmarkFolder(
		@AuthUserId Long userId,
		@RequestBody @Valid CreateBookmarkFolderHttpRequest request
	) {
		final Long bookmarkFolderId = bookmarkService.createFolder(CreateBookmarkFolder.builder()
			.userId(userId)
			.name(request.name())
			.description(request.description())
			.color(request.color())
			.build());
		return new CreateBookmarkFolderResponse(bookmarkFolderId);
	}

	@PostMapping("/{bookmarkFolderId}/bookmarks")
	public AddBookmarkHttpResponse addBookmark(
		@PathVariable(name = "bookmarkFolderId") Long bookmarkFolderId,
		@AuthUserId Long userId,
		@RequestBody @Valid AddBookmarkHttpRequest request
	) {
		final Long bookmarkId = bookmarkService.addBookmark(CreateBookmark.builder()
			.bookmarkFolderId(bookmarkFolderId)
			.userId(userId)
			.spotId(request.spotId())
			.name(request.name())
			.build()
		);
		return new AddBookmarkHttpResponse(bookmarkId);
	}

	@GetMapping("/{bookmarkFolderId}/bookmarks")
	public BookmarkListResponse getBookmarks(
		@PathVariable(name = "bookmarkFolderId") Long bookmarkFolderId,
		@AuthUserId Long userId,
		@RequestParam(name = "postPreviewCount", defaultValue = "5")
		@Range(min = 1, max = 10, message = "미리보기 사진은 1~10개만 가능합니다.") Integer postPreviewCount,
		@PageableDefault Pageable pageable
	) {
		final PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
			Sort.by("id").descending());
		final ReadBookmarkRequest request = ReadBookmarkRequest.builder()
			.bookmarkFolderId(bookmarkFolderId)
			.userId(userId)
			.postPreviewCount(postPreviewCount)
			.pageable(pageRequest)
			.build();
		return bookmarkService.getBookmarks(request);
	}

	@GetMapping
	public BookmarkFolderListHttpResponse getBookmarkFolders(
		@AuthUserId Long userId,
		@RequestParam(name = "direction", defaultValue = "desc") String direction
	) {
		final List<BookmarkFolderResponse> bookmarkFolders = bookmarkService.getBookmarkFolders(
			ReadBookmarkFolderList.of(userId, direction));
		return BookmarkFolderListHttpResponse.builder()
			.bookmarkFolders(bookmarkFolders)
			.maxBookmarkCount(BookmarkFolder.MAX_BOOKMARKED)
			.build();
	}

	@DeleteMapping("/{bookmarkFolderId}/bookmarks")
	public ApiResponse deleteBookmarks(
		@PathVariable(name = "bookmarkFolderId") Long bookmarkFolderId,
		@AuthUserId Long userId,
		@RequestBody @Valid RemoveBookmarkHttpRequest request
	) {
		bookmarkService.removeBookmarks(bookmarkFolderId, userId, request.bookmarkIds());
		return ApiResponse.SUCCESS;
	}

	@DeleteMapping("/{bookmarkFolderId}")
	public ApiResponse deleteBookmarkFolder(
		@PathVariable(name = "bookmarkFolderId") Long bookmarkFolderId,
		@AuthUserId Long userId
	) {
		bookmarkService.deleteBookmarkFolder(bookmarkFolderId, userId);
		return ApiResponse.SUCCESS;
	}

	@GetMapping("/mine")
	public BookmarkCoordFolderListHttpResponse getAllMyBookmarkCoord(
		@AuthUserId Long userId
	) {
		final List<BookmarkCoordFolderHttpResponse> bookmarkFolders = BookmarkCoordFolderHttpResponse.from(
			bookmarkService.getAllMyBookmarkCoord(userId));
		return new BookmarkCoordFolderListHttpResponse(bookmarkFolders);
	}

	@PutMapping("/{bookmarkFolderId}")
	public ApiResponse updateBookmarkFolderName(
		@PathVariable(name = "bookmarkFolderId") Long bookmarkFolderId,
		@RequestBody @Valid UpdateBookmarkFolderHttpRequest request
	) {
		bookmarkService.updateBookmarkFolder(
			bookmarkFolderId,
			request.name(),
			request.description(),
			request.color());
		return ApiResponse.SUCCESS;
	}
}
