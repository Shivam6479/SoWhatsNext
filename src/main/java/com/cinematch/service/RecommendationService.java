
package com.cinematch.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.cinematch.util.DatabaseConnection;

public class RecommendationService {

    public static class MovieRecommendation {
        private final int movieId;
        private final String title;
        private final Integer releaseYear;
        private final String language;
        private final double averageRating;
        private final String posterUrl;
        private final double recommendationScore;

        public MovieRecommendation(
                int movieId,
                String title,
                Integer releaseYear,
                String language,
                double averageRating,
                String posterUrl,
                double recommendationScore) {

            this.movieId = movieId;
            this.title = title;
            this.releaseYear = releaseYear;
            this.language = language;
            this.averageRating = averageRating;
            this.posterUrl = posterUrl;
            this.recommendationScore = recommendationScore;
        }

        public int getMovieId() {
            return movieId;
        }

        public String getTitle() {
            return title;
        }

        public Integer getReleaseYear() {
            return releaseYear;
        }

        public String getLanguage() {
            return language;
        }

        public double getAverageRating() {
            return averageRating;
        }

        public String getPosterUrl() {
            return posterUrl;
        }

        public double getRecommendationScore() {
            return recommendationScore;
        }
    }

    public List<MovieRecommendation> getRecommendations(int userId)
            throws SQLException {

        List<MovieRecommendation> recommendations =
                new ArrayList<>();

        String sql = """
            WITH user_genre_stats AS (
                SELECT
                    mg.genre_id,
                    AVG(r.rating) AS avg_user_rating
                FROM ratings r
                JOIN movie_genres mg
                    ON mg.movie_id = r.movie_id
                WHERE r.user_id = ?
                GROUP BY mg.genre_id
            ),
            watched_genres AS (
                SELECT DISTINCT mg.genre_id
                FROM watch_history wh
                JOIN movie_genres mg
                    ON mg.movie_id = wh.movie_id
                WHERE wh.user_id = ?
            ),
            candidate_scores AS (
                SELECT
                    m.movie_id,
                    m.title,
                    m.release_year,
                    m.language,
                    m.average_rating,
                    m.poster_url,

                    COUNT(DISTINCT mg.genre_id)
                        AS total_genres,

                    COUNT(DISTINCT CASE
                        WHEN ugs.avg_user_rating >= 4
                        THEN mg.genre_id
                    END) AS preferred_matches,

                    AVG(ugs.avg_user_rating)
                        AS personal_genre_rating,

                    COUNT(DISTINCT CASE
                        WHEN wg.genre_id IS NOT NULL
                        THEN mg.genre_id
                    END) AS watched_matches

                FROM movies m

                LEFT JOIN movie_genres mg
                    ON mg.movie_id = m.movie_id

                LEFT JOIN user_genre_stats ugs
                    ON ugs.genre_id = mg.genre_id

                LEFT JOIN watched_genres wg
                    ON wg.genre_id = mg.genre_id

                WHERE NOT EXISTS (
                    SELECT 1
                    FROM ratings r
                    WHERE r.user_id = ?
                      AND r.movie_id = m.movie_id
                )
                AND NOT EXISTS (
                    SELECT 1
                    FROM watch_history wh
                    WHERE wh.user_id = ?
                      AND wh.movie_id = m.movie_id
                )

                GROUP BY
                    m.movie_id,
                    m.title,
                    m.release_year,
                    m.language,
                    m.average_rating,
                    m.poster_url
            )

            SELECT
                movie_id,
                title,
                release_year,
                language,
                average_rating,
                poster_url,

                (
                    40.0 * preferred_matches
                        / GREATEST(total_genres, 1)

                    + 35.0 * COALESCE(
                        personal_genre_rating, 0
                    ) / 5.0

                    + 25.0 * watched_matches
                        / GREATEST(total_genres, 1)
                ) AS recommendation_score

            FROM candidate_scores

            ORDER BY
                recommendation_score DESC,
                average_rating DESC,
                title ASC

            LIMIT 30
            """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setInt(2, userId);
            statement.setInt(3, userId);
            statement.setInt(4, userId);

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {

                    int yearValue = rs.getInt("release_year");
                    Integer year = rs.wasNull()
                            ? null : yearValue;

                    double ratingValue =
                            rs.getDouble("average_rating");

                    if (rs.wasNull()) {
                        ratingValue = 0.0;
                    }

                    recommendations.add(
                        new MovieRecommendation(
                            rs.getInt("movie_id"),
                            rs.getString("title"),
                            year,
                            rs.getString("language"),
                            ratingValue,
                            rs.getString("poster_url"),
                            rs.getDouble("recommendation_score")
                        )
                    );
                }
            }
        }

        return recommendations;
    }
}
