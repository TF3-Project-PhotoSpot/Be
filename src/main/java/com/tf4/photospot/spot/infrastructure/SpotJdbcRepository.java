package com.tf4.photospot.spot.infrastructure;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.tf4.photospot.post.domain.Post;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SpotJdbcRepository {
	private final NamedParameterJdbcTemplate jdbcTemplate;

	public SpotJdbcRepository(DataSource dataSource) {
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	public void decreasePostCountBy(List<Post> posts) {
		final List<Long> postIds = posts.stream().map(Post::getId).toList();
		jdbcTemplate.update("""
				update spot, (
					select spot_id, count(id) as decrease_count
					from post
					where id in (:postIds)
					group by spot_id
				) as decrease_spot
				set spot.post_count = spot.post_count - decrease_spot.decrease_count
				where spot.id = decrease_spot.spot_id
			""", new MapSqlParameterSource("postIds", postIds));
	}
}

