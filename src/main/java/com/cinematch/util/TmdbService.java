
package com.cinematch.util;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TmdbService {

    private static final String BASE_URL =
            "https://api.themoviedb.org/3";

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    private TmdbService() {
        // Utility class: objects are not required.
    }

    private static String getAccessToken() {
        String token = System.getenv("TMDB_ACCESS_TOKEN");

        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "TMDB_ACCESS_TOKEN is missing. Set it in the same "
                    + "terminal from which you start the application."
            );
        }

        return token.trim();
    }

    private static JsonArray executeRequest(String url)
            throws IOException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header(
                        "Authorization",
                        "Bearer " + getAccessToken()
                )
                .header("Accept", "application/json")
                .GET()
                .build();

        final HttpResponse<String> response;

        try {
            response = CLIENT.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();

            throw new IOException(
                    "TMDB request was interrupted. Please try again.",
                    ex
            );
        } catch (IOException ex) {
            throw new IOException(
                    "Could not connect to TMDB. Check your internet "
                    + "connection. Details: " + ex.getMessage(),
                    ex
            );
        }

        if (response.statusCode() != 200) {
            throw new IOException(
                    "TMDB API error: HTTP "
                    + response.statusCode()
                    + " - " + response.body()
            );
        }

        try {
            JsonObject json = JsonParser
                    .parseString(response.body())
                    .getAsJsonObject();

            if (!json.has("results")
                    || !json.get("results").isJsonArray()) {

                throw new IOException(
                        "TMDB response does not contain a valid "
                        + "results list."
                );
            }

            return json.getAsJsonArray("results");

        } catch (IllegalStateException | com.google.gson.JsonParseException ex) {
            throw new IOException(
                    "TMDB returned an invalid JSON response.",
                    ex
            );
        }
    }

    /**
     * Search for movies by title.
     */
    public static JsonArray searchMovies(String query)
            throws IOException {

        if (query == null || query.isBlank()) {
            return new JsonArray();
        }

        String encodedQuery = URLEncoder.encode(
                query.trim(),
                StandardCharsets.UTF_8
        );

        String url = BASE_URL
                + "/search/movie?query=" + encodedQuery
                + "&include_adult=false"
                + "&page=1";

        return executeRequest(url);
    }

    /**
     * Load popular movies from TMDB.
     */
    public static JsonArray getPopularMovies(int page)
            throws IOException {

        int safePage = Math.max(1, page);

        String url = BASE_URL
                + "/movie/popular"
                + "?language=en-US"
                + "&page=" + safePage;

        return executeRequest(url);
    }

    /**
     * Discover movies using language and region filters.
     */
    public static JsonArray discoverMovies(
            int page,
            String language,
            String region
    ) throws IOException {

        int safePage = Math.max(1, page);

        String safeLanguage =
                language == null || language.isBlank()
                        ? "en-US" : language.trim();

        String safeRegion =
                region == null || region.isBlank()
                        ? "US" : region.trim();

        String url = BASE_URL
                + "/discover/movie"
                + "?include_adult=false"
                + "&sort_by=popularity.desc"
                + "&language="
                + URLEncoder.encode(
                        safeLanguage,
                        StandardCharsets.UTF_8
                )
                + "&region="
                + URLEncoder.encode(
                        safeRegion,
                        StandardCharsets.UTF_8
                )
                + "&page=" + safePage;

        return executeRequest(url);
    }

    /**
     * Convert a TMDB poster path into a complete image URL.
     */
    public static String getPosterUrl(String posterPath) {

        if (posterPath == null
                || posterPath.isBlank()
                || posterPath.equals("null")) {
            return null;
        }

        if (posterPath.startsWith("http://")
                || posterPath.startsWith("https://")) {
            return posterPath;
        }

        if (!posterPath.startsWith("/")) {
            posterPath = "/" + posterPath;
        }

        return "https://image.tmdb.org/t/p/w500"
                + posterPath;
    }
}
