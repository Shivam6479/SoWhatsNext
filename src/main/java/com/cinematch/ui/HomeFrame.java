
package com.cinematch.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import com.cinematch.service.RecommendationService;
import com.cinematch.util.DatabaseConnection;
import com.cinematch.util.TmdbService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HomeFrame extends JFrame {

    private final String username;

    private JTextField searchField;
    private JComboBox<String> genreFilter;

    // Panels displayed on the homepage
    private JPanel movieCardsPanel;
    private JPanel recommendationCardsPanel;
    private JPanel contentPanel;

    private int selectedMovieId = -1;
    private int popularPage = 1;
    private String selectedMovieTitle = "";

    private final Color background = new Color(16, 17, 22);
    private final Color cardBackground = new Color(29, 30, 39);
    private final Color accent = new Color(229, 9, 20);
    private final Color white = Color.WHITE;
    private final Color muted = new Color(175, 177, 190);
    private final Color borderColor = new Color(55, 56, 70);

    public HomeFrame(String username) {
        this.username = username;

        setTitle("SoWhatsNext | Discover Movies");
        setSize(1250, 820);
        setMinimumSize(new Dimension(950, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        buildInterface();
        loadGenres();
        loadMovies("");
        loadRecommendations();
    }

    private void buildInterface() {

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(background);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
                22, 28, 18, 28));

        // ---------------- NAVIGATION ----------------

        JPanel navigation = new JPanel(new BorderLayout(15, 0));
        navigation.setBackground(background);

        JLabel logo = new JLabel("SoWhatsNext");
        logo.setForeground(accent);
        logo.setFont(new Font("Arial", Font.BOLD, 30));

        JPanel navigationButtons =
                new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        navigationButtons.setBackground(background);

        JButton recommendationsButton =
                createButton("For You", accent);

        JButton watchlistButton =
                createButton("My Watchlist", cardBackground);

        JButton logoutButton =
                createButton("Logout", cardBackground);

        navigationButtons.add(recommendationsButton);
        navigationButtons.add(watchlistButton);
        navigationButtons.add(logoutButton);

        navigation.add(logo, BorderLayout.WEST);
        navigation.add(navigationButtons, BorderLayout.EAST);

        // ---------------- WELCOME ----------------

        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(
                new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBackground(background);

        JLabel heading = new JLabel("What are we watching today?");
        heading.setForeground(white);
        heading.setFont(new Font("Arial", Font.BOLD, 27));

        JLabel subtitle = new JLabel(
                "Welcome, " + username
                + "! Discover your next favourite movie.");

        subtitle.setForeground(muted);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));

        welcomePanel.add(heading);
        welcomePanel.add(Box.createVerticalStrut(8));
        welcomePanel.add(subtitle);

        // ---------------- SEARCH ----------------

        searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 15));
        searchField.setBackground(cardBackground);
        searchField.setForeground(white);
        searchField.setCaretColor(white);
        searchField.setToolTipText("Search movie title");

        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor),
                BorderFactory.createEmptyBorder(11, 12, 11, 12)));

        genreFilter = new JComboBox<>();
        genreFilter.addItem("All Genres");
        genreFilter.setFont(new Font("Arial", Font.PLAIN, 13));
        genreFilter.setPreferredSize(new Dimension(150, 40));
        genreFilter.setBackground(cardBackground);
        genreFilter.setForeground(white);

        JButton searchButton = createButton("Search", accent);
        JButton showAllButton =
                createButton("Show All", cardBackground);
        JButton popularButton =
                createButton("Popular Movies", accent);
        JButton loadMoreButton =
                createButton("Load More", cardBackground);

        JPanel filterPanel =
                new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        filterPanel.setBackground(background);
        filterPanel.add(genreFilter);
        filterPanel.add(searchButton);
        filterPanel.add(showAllButton);
        filterPanel.add(popularButton);
        filterPanel.add(loadMoreButton);

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(background);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(filterPanel, BorderLayout.EAST);

        // ---------------- RECOMMENDATIONS SECTION ----------------

        JLabel recommendationTitle =
                new JLabel("Recommended For You");

        recommendationTitle.setForeground(white);
        recommendationTitle.setFont(
                new Font("Arial", Font.BOLD, 23));

        JLabel recommendationSubtitle = new JLabel(
                "Movies selected using your preferences and ratings.");

        recommendationSubtitle.setForeground(muted);
        recommendationSubtitle.setFont(
                new Font("Arial", Font.PLAIN, 13));

        recommendationCardsPanel =
                new JPanel(new GridLayout(0, 4, 18, 22));

        recommendationCardsPanel.setBackground(background);
        recommendationCardsPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 2, 20, 2));

        JPanel recommendationSection = new JPanel();
        recommendationSection.setLayout(
                new BoxLayout(recommendationSection, BoxLayout.Y_AXIS));

        recommendationSection.setBackground(background);

        recommendationTitle.setAlignmentX(LEFT_ALIGNMENT);
        recommendationSubtitle.setAlignmentX(LEFT_ALIGNMENT);
        recommendationCardsPanel.setAlignmentX(LEFT_ALIGNMENT);

        recommendationSection.add(recommendationTitle);
        recommendationSection.add(Box.createVerticalStrut(7));
        recommendationSection.add(recommendationSubtitle);
        recommendationSection.add(Box.createVerticalStrut(5));
        recommendationSection.add(recommendationCardsPanel);

        // ---------------- EXPLORE MOVIES SECTION ----------------

        JLabel exploreTitle = new JLabel("Explore Movies");
        exploreTitle.setForeground(white);
        exploreTitle.setFont(new Font("Arial", Font.BOLD, 23));
        exploreTitle.setAlignmentX(LEFT_ALIGNMENT);

        movieCardsPanel = new JPanel(
                new GridLayout(0, 4, 18, 22));

        movieCardsPanel.setBackground(background);
        movieCardsPanel.setBorder(BorderFactory.createEmptyBorder(
                10, 2, 20, 2));

        movieCardsPanel.setAlignmentX(LEFT_ALIGNMENT);

        // Both sections are placed inside one vertically scrollable panel.
        contentPanel = new JPanel();
        contentPanel.setLayout(
                new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(background);

        contentPanel.add(recommendationSection);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(exploreTitle);
        contentPanel.add(movieCardsPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(background);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(18);

        // ---------------- TOP PANEL ----------------

        JPanel topPanel = new JPanel();
        topPanel.setLayout(
                new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(background);

        topPanel.add(navigation);
        topPanel.add(Box.createVerticalStrut(28));
        topPanel.add(welcomePanel);
        topPanel.add(Box.createVerticalStrut(22));
        topPanel.add(searchPanel);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JLabel footer = new JLabel(
                "SoWhatsNext | Made for movie lovers");

        footer.setForeground(muted);
        mainPanel.add(footer, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // ---------------- BUTTON ACTIONS ----------------

        searchButton.addActionListener(e -> searchMovies());
        searchField.addActionListener(e -> searchMovies());

        genreFilter.addActionListener(
                e -> loadMovies(searchField.getText().trim()));

        showAllButton.addActionListener(e -> {
            searchField.setText("");
            genreFilter.setSelectedItem("All Genres");
            loadMovies("");
        });

        popularButton.addActionListener(e -> {
            popularPage = 1;
            loadPopularMovies(popularPage);
        });

        loadMoreButton.addActionListener(e -> {
            popularPage++;
            loadPopularMovies(popularPage);
        });

        // Refresh recommendations on the homepage.
        // No separate RecommendationsFrame is opened.
        recommendationsButton.addActionListener(e -> {
            loadRecommendations();

            recommendationTitle.scrollRectToVisible(
                    recommendationTitle.getBounds());
        });

        watchlistButton.addActionListener(
                e -> openMyWatchlist());

        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    // =========================================================
    // BUTTON HELPER
    // =========================================================

    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(white);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setBorder(BorderFactory.createEmptyBorder(
                10, 14, 10, 14));

        return button;
    }

    // =========================================================
    // LOAD GENRES
    // =========================================================

    private void loadGenres() {

        String sql =
                "SELECT genre_name FROM genres ORDER BY genre_name";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            genreFilter.removeAllItems();
            genreFilter.addItem("All Genres");

            while (result.next()) {
                genreFilter.addItem(result.getString("genre_name"));
            }

        } catch (SQLException ex) {
            showDatabaseError("Could not load genres.", ex);
        }
    }

    // =========================================================
    // HOMEPAGE RECOMMENDATIONS
    // =========================================================

    private void loadRecommendations() {

        recommendationCardsPanel.removeAll();
        recommendationCardsPanel.setLayout(
                new GridLayout(0, 4, 18, 22));

        JLabel loadingLabel = new JLabel(
                "Finding movies for you...",
                SwingConstants.LEFT);

        loadingLabel.setForeground(muted);
        recommendationCardsPanel.add(loadingLabel);

        recommendationCardsPanel.revalidate();
        recommendationCardsPanel.repaint();

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            @Override
            protected Boolean doInBackground() throws Exception {

                int userId = getLoggedInUserId();

                RecommendationService service =
                        new RecommendationService();

                var recommendations =
                        service.getRecommendations(userId);

                recommendationCardsPanel.removeAll();

                if (recommendations == null
                        || recommendations.isEmpty()) {
                    return false;
                }

                for (var movie : recommendations) {

                    int movieId = movie.getMovieId();

                    String title = movie.getTitle();

                    String year = movie.getReleaseYear() == null
                            ? "N/A"
                            : String.valueOf(movie.getReleaseYear());

                    String language = movie.getLanguage();

                    if (language == null || language.isBlank()) {
                        language = "N/A";
                    }

                    String rating = String.valueOf(
                            movie.getAverageRating());

                    String posterUrl = movie.getPosterUrl();

                    JPanel card = createMovieCard(
                            movieId,
                            title,
                            year,
                            language,
                            rating,
                            posterUrl);

                    recommendationCardsPanel.add(card);
                }

                return true;
            }

            @Override
            protected void done() {

                try {
                    boolean hasRecommendations = get();

                    if (!hasRecommendations) {

                        recommendationCardsPanel.removeAll();
                        recommendationCardsPanel.setLayout(
                                new BorderLayout());

                        JLabel emptyLabel = new JLabel(
                                "<html><div style='padding:8px'>"
                                + "No recommendations available yet."
                                + "<br>Explore movies and rate a few "
                                + "to personalise your suggestions."
                                + "</div></html>");

                        emptyLabel.setForeground(muted);
                        emptyLabel.setFont(
                                new Font("Arial", Font.PLAIN, 14));

                        recommendationCardsPanel.add(
                                emptyLabel, BorderLayout.NORTH);
                    }

                } catch (Exception ex) {

                    Throwable cause = ex.getCause() == null
                            ? ex
                            : ex.getCause();

                    recommendationCardsPanel.removeAll();
                    recommendationCardsPanel.setLayout(
                            new BorderLayout());

                    JLabel errorLabel = new JLabel(
                            "Could not load recommendations. "
                            + "Please try again.");

                    errorLabel.setForeground(muted);

                    recommendationCardsPanel.add(
                            errorLabel, BorderLayout.NORTH);

                    System.err.println(
                            "Recommendation loading failed: "
                            + cause.getMessage());

                    cause.printStackTrace();
                }

                recommendationCardsPanel.revalidate();
                recommendationCardsPanel.repaint();

                contentPanel.revalidate();
                contentPanel.repaint();
            }
        };

        worker.execute();
    }

    // =========================================================
    // SEARCH TMDB
    // =========================================================

    private void searchMovies() {

        String query = searchField.getText().trim();
        genreFilter.setSelectedItem("All Genres");

        if (query.isBlank()) {
            loadMovies("");
            return;
        }

        JDialog loadingDialog =
                new JDialog(this, "Searching TMDB", false);

        loadingDialog.add(new JLabel(
                "Searching TMDB for movies...",
                SwingConstants.CENTER));

        loadingDialog.setSize(300, 90);
        loadingDialog.setLocationRelativeTo(this);

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {

            @Override
            protected Integer doInBackground() throws Exception {

                JsonArray results = TmdbService.searchMovies(query);
                int savedCount = 0;

                for (JsonElement element : results) {
                    if (element != null
                            && element.isJsonObject()
                            && saveTmdbMovie(element.getAsJsonObject())) {
                        savedCount++;
                    }
                }

                return savedCount;
            }

            @Override
            protected void done() {

                loadingDialog.dispose();

                try {
                    int count = get();

                    loadMovies(query);
                    loadRecommendations();

                    if (count == 0) {
                        JOptionPane.showMessageDialog(
                                HomeFrame.this,
                                "TMDB returned no matching movies. "
                                + "Try another title.",
                                "No Results",
                                JOptionPane.INFORMATION_MESSAGE);
                    }

                } catch (Exception ex) {

                    Throwable cause = ex.getCause() == null
                            ? ex
                            : ex.getCause();

                    JOptionPane.showMessageDialog(
                            HomeFrame.this,
                            "TMDB search failed: "
                            + cause.getMessage()
                            + "\nCheck TMDB_ACCESS_TOKEN in the same "
                            + "terminal used to launch the app.",
                            "TMDB Error",
                            JOptionPane.ERROR_MESSAGE);

                    cause.printStackTrace();
                }
            }
        };

        worker.execute();
        loadingDialog.setVisible(true);
    }

    // =========================================================
    // POPULAR MOVIES
    // =========================================================

    private void loadPopularMovies(int page) {

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {

            @Override
            protected Integer doInBackground() throws Exception {

                JsonArray results =
                        TmdbService.getPopularMovies(page);

                int savedCount = 0;

                for (JsonElement element : results) {
                    if (element != null
                            && element.isJsonObject()
                            && saveTmdbMovie(element.getAsJsonObject())) {
                        savedCount++;
                    }
                }

                return savedCount;
            }

            @Override
            protected void done() {

                try {
                    int count = get();

                    searchField.setText("");
                    genreFilter.setSelectedItem("All Genres");

                    loadMovies("");
                    loadRecommendations();

                    if (count == 0) {
                        JOptionPane.showMessageDialog(
                                HomeFrame.this,
                                "No movies were returned for this page. "
                                + "Try another page.",
                                "No Results",
                                JOptionPane.INFORMATION_MESSAGE);
                    }

                } catch (Exception ex) {

                    Throwable cause = ex.getCause() == null
                            ? ex
                            : ex.getCause();

                    JOptionPane.showMessageDialog(
                            HomeFrame.this,
                            "Could not load popular movies: "
                            + cause.getMessage()
                            + "\nCheck TMDB_ACCESS_TOKEN in the same "
                            + "terminal used to launch the app.",
                            "TMDB Error",
                            JOptionPane.ERROR_MESSAGE);

                    cause.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    // =========================================================
    // SAVE TMDB MOVIE TO DATABASE
    // =========================================================

    private boolean saveTmdbMovie(JsonObject movie)
            throws SQLException {

        String title = getJsonString(movie, "title");

        if (title == null || title.isBlank()) {
            return false;
        }

        String releaseDate = getJsonString(movie, "release_date");
        Integer year = null;

        if (releaseDate != null && releaseDate.length() >= 4) {
            try {
                year = Integer.parseInt(releaseDate.substring(0, 4));
            } catch (NumberFormatException ignored) {
                year = null;
            }
        }

        String description = getJsonString(movie, "overview");
        String language =
                getJsonString(movie, "original_language");

        String posterUrl = TmdbService.getPosterUrl(
                getJsonString(movie, "poster_path"));

        Double rating = null;

        if (movie.has("vote_average")
                && !movie.get("vote_average").isJsonNull()) {

            try {
                rating = movie.get("vote_average").getAsDouble() / 2.0;
            } catch (RuntimeException ignored) {
                rating = null;
            }
        }

        String findSql = """
                SELECT movie_id
                FROM movies
                WHERE title = ?
                  AND (
                      release_year = ?
                      OR (release_year IS NULL AND ? IS NULL)
                  )
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement find =
                     connection.prepareStatement(findSql)) {

            find.setString(1, title);

            if (year == null) {
                find.setNull(2, Types.INTEGER);
                find.setNull(3, Types.INTEGER);
            } else {
                find.setInt(2, year);
                find.setInt(3, year);
            }

            Integer existingId = null;

            try (ResultSet result = find.executeQuery()) {
                if (result.next()) {
                    existingId = result.getInt("movie_id");
                }
            }

            if (existingId != null) {

                String updateSql = """
                        UPDATE movies
                        SET description = ?,
                            language = ?,
                            average_rating = ?,
                            poster_url = ?
                        WHERE movie_id = ?
                        """;

                try (PreparedStatement update =
                             connection.prepareStatement(updateSql)) {

                    update.setString(1, description);
                    update.setString(2, language);

                    if (rating == null) {
                        update.setNull(3, Types.DECIMAL);
                    } else {
                        update.setDouble(3, rating);
                    }

                    update.setString(4, posterUrl);
                    update.setInt(5, existingId);

                    update.executeUpdate();
                }

            } else {

                String insertSql = """
                        INSERT INTO movies
                            (title, release_year, description,
                             language, average_rating, poster_url)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """;

                try (PreparedStatement insert =
                             connection.prepareStatement(insertSql)) {

                    insert.setString(1, title);

                    if (year == null) {
                        insert.setNull(2, Types.INTEGER);
                    } else {
                        insert.setInt(2, year);
                    }

                    insert.setString(3, description);
                    insert.setString(4, language);

                    if (rating == null) {
                        insert.setNull(5, Types.DECIMAL);
                    } else {
                        insert.setDouble(5, rating);
                    }

                    insert.setString(6, posterUrl);
                    insert.executeUpdate();
                }
            }
        }

        return true;
    }

    private String getJsonString(JsonObject object, String key) {

        if (object == null
                || !object.has(key)
                || object.get(key).isJsonNull()) {
            return null;
        }

        String value = object.get(key).getAsString();

        return value == null || value.isBlank()
                ? null
                : value;
    }

    // =========================================================
    // LOAD MOVIES FROM DATABASE
    // =========================================================

    private void loadMovies(String searchText) {

        movieCardsPanel.removeAll();
        movieCardsPanel.setLayout(
                new GridLayout(0, 4, 18, 22));

        String selectedGenre =
                (String) genreFilter.getSelectedItem();

        if (selectedGenre == null) {
            selectedGenre = "All Genres";
        }

        String sql = """
                SELECT m.movie_id, m.title, m.release_year,
                       m.language, m.average_rating, m.poster_url
                FROM movies m
                WHERE m.title LIKE ?
                  AND (
                      ? = 'All Genres'
                      OR EXISTS (
                          SELECT 1
                          FROM movie_genres mg
                          JOIN genres g
                            ON mg.genre_id = g.genre_id
                          WHERE mg.movie_id = m.movie_id
                            AND g.genre_name = ?
                      )
                  )
                ORDER BY m.title
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, "%" + searchText + "%");
            statement.setString(2, selectedGenre);
            statement.setString(3, selectedGenre);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    int id = result.getInt("movie_id");
                    String title = result.getString("title");

                    Object yearObject =
                            result.getObject("release_year");

                    Object ratingObject =
                            result.getObject("average_rating");

                    String year = yearObject == null
                            ? "N/A"
                            : yearObject.toString();

                    String language =
                            result.getString("language");

                    if (language == null || language.isBlank()) {
                        language = "N/A";
                    }

                    String rating = ratingObject == null
                            ? "Not rated"
                            : ratingObject.toString();

                    String posterUrl =
                            result.getString("poster_url");

                    movieCardsPanel.add(createMovieCard(
                            id, title, year,
                            language, rating, posterUrl));
                }
            }

            if (movieCardsPanel.getComponentCount() == 0) {

                movieCardsPanel.setLayout(new BorderLayout());

                JLabel emptyLabel = new JLabel(
                        "No movies found. Try another search.",
                        SwingConstants.CENTER);

                emptyLabel.setForeground(muted);
                emptyLabel.setFont(
                        new Font("Arial", Font.PLAIN, 17));

                movieCardsPanel.add(
                        emptyLabel, BorderLayout.NORTH);
            }

        } catch (SQLException ex) {
            showDatabaseError("Could not load movies.", ex);
        }

        movieCardsPanel.revalidate();
        movieCardsPanel.repaint();

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // =========================================================
    // MOVIE CARD
    // =========================================================

    private JPanel createMovieCard(
            int id,
            String title,
            String year,
            String language,
            String rating,
            String posterUrl) {

        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(cardBackground);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel poster = new JLabel(
                "MOVIE", SwingConstants.CENTER);

        poster.setPreferredSize(new Dimension(190, 220));
        poster.setOpaque(true);
        poster.setBackground(new Color(41, 42, 54));
        poster.setForeground(muted);
        poster.setFont(new Font("Arial", Font.BOLD, 20));

        if (posterUrl != null && !posterUrl.isBlank()) {
            try {
                ImageIcon icon = new ImageIcon(new URL(posterUrl));

                if (icon.getIconWidth() > 0) {
                    Image image = icon.getImage().getScaledInstance(
                            190, 220, Image.SCALE_SMOOTH);

                    poster.setText("");
                    poster.setIcon(new ImageIcon(image));
                }
            } catch (Exception ignored) {
                poster.setText("MOVIE");
            }
        }

        JLabel titleLabel = new JLabel(
                "<html><div style='width:175px'><b>"
                + escapeHtml(title)
                + "</b></div></html>");

        titleLabel.setForeground(white);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));

        JLabel infoLabel = new JLabel(
                year + "  |  " + language);

        infoLabel.setForeground(muted);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        JLabel ratingLabel = new JLabel("★  " + rating);
        ratingLabel.setForeground(new Color(255, 195, 70));
        ratingLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JPanel detailsPanel = new JPanel();
        detailsPanel.setBackground(cardBackground);
        detailsPanel.setLayout(
                new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));

        detailsPanel.add(titleLabel);
        detailsPanel.add(Box.createVerticalStrut(7));
        detailsPanel.add(infoLabel);
        detailsPanel.add(Box.createVerticalStrut(5));
        detailsPanel.add(ratingLabel);

        JButton detailsButton = createButton("Details", accent);

        JButton rateButton = createButton(
                "Rate", new Color(120, 80, 25));

        JButton reviewsButton = createButton(
                "Reviews", new Color(65, 65, 85));

        JButton watchlistButton = createButton(
                "+ Watchlist", new Color(35, 105, 75));

        JPanel firstRow = new JPanel(
                new GridLayout(1, 2, 5, 5));

        firstRow.setBackground(cardBackground);
        firstRow.add(detailsButton);
        firstRow.add(rateButton);

        JPanel secondRow = new JPanel(
                new GridLayout(1, 2, 5, 5));

        secondRow.setBackground(cardBackground);
        secondRow.add(reviewsButton);
        secondRow.add(watchlistButton);

        JPanel actions = new JPanel(
                new GridLayout(2, 1, 0, 5));

        actions.setBackground(cardBackground);
        actions.add(firstRow);
        actions.add(secondRow);

        card.add(poster, BorderLayout.NORTH);
        card.add(detailsPanel, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);

        detailsButton.addActionListener(e -> {
            selectMovie(id, title);
            showMovieDetails();
        });

        rateButton.addActionListener(e -> {
            selectMovie(id, title);
            rateAndReviewMovie();
        });

        reviewsButton.addActionListener(e -> {
            selectMovie(id, title);
            viewMovieReviews();
        });

        watchlistButton.addActionListener(e -> {
            selectMovie(id, title);
            addToWatchlist();
        });

        return card;
    }

    private void selectMovie(int id, String title) {
        selectedMovieId = id;
        selectedMovieTitle = title;
    }

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    // =========================================================
    // MOVIE DETAILS
    // =========================================================

    private void showMovieDetails() {

        if (selectedMovieId == -1) {
            showNoMovieSelected();
            return;
        }

        String sql = """
                SELECT title, release_year, description,
                       language, average_rating
                FROM movies
                WHERE movie_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, selectedMovieId);

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    return;
                }

                String description =
                        result.getString("description");

                if (description == null || description.isBlank()) {
                    description = "No description available.";
                }

                String year =
                        result.getObject("release_year") == null
                        ? "N/A"
                        : result.getObject("release_year").toString();

                String language = result.getString("language");

                if (language == null || language.isBlank()) {
                    language = "N/A";
                }

                String rating =
                        result.getObject("average_rating") == null
                        ? "Not rated"
                        : result.getObject("average_rating").toString();

                String details = "Title: "
                        + result.getString("title")
                        + "\nRelease Year: " + year
                        + "\nLanguage: " + language
                        + "\nAverage Rating: " + rating
                        + "\n\nDescription:\n" + description;

                JTextArea area = new JTextArea(details, 12, 38);
                area.setEditable(false);
                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                area.setMargin(new Insets(10, 10, 10, 10));

                JOptionPane.showMessageDialog(
                        this,
                        new JScrollPane(area),
                        "Movie Details",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException ex) {
            showDatabaseError(
                    "Could not load movie details.", ex);
        }
    }

    // =========================================================
    // RATE AND REVIEW
    // =========================================================

    private void rateAndReviewMovie() {

        if (selectedMovieId == -1) {
            showNoMovieSelected();
            return;
        }

        JComboBox<String> ratingBox =
                new JComboBox<>(
                        new String[]{"5", "4", "3", "2", "1"});

        ratingBox.setSelectedItem(
                loadExistingRating(selectedMovieId));

        JTextArea reviewArea = new JTextArea(6, 30);
        reviewArea.setLineWrap(true);
        reviewArea.setWrapStyleWord(true);

        reviewArea.setText(
                loadExistingReview(selectedMovieId));

        JPanel form = new JPanel(new BorderLayout(8, 8));

        JPanel ratingPanel =
                new JPanel(new FlowLayout(FlowLayout.LEFT));

        ratingPanel.add(new JLabel("Rating (1-5):"));
        ratingPanel.add(ratingBox);

        form.add(ratingPanel, BorderLayout.NORTH);
        form.add(new JScrollPane(reviewArea), BorderLayout.CENTER);
        form.add(new JLabel("Write your review (optional):"),
                BorderLayout.SOUTH);

        int choice = JOptionPane.showConfirmDialog(
                this,
                form,
                "Rate & Review: " + selectedMovieTitle,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        double rating = Double.parseDouble(
                (String) ratingBox.getSelectedItem());

        String review = reviewArea.getText().trim();

        String sql = """
                INSERT INTO ratings
                    (user_id, movie_id, rating, review)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    rating = VALUES(rating),
                    review = VALUES(review),
                    rated_at = CURRENT_TIMESTAMP
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                int userId = getLoggedInUserId();

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    statement.setInt(1, userId);
                    statement.setInt(2, selectedMovieId);
                    statement.setDouble(3, rating);

                    if (review.isBlank()) {
                        statement.setNull(4, Types.VARCHAR);
                    } else {
                        statement.setString(4, review);
                    }

                    statement.executeUpdate();
                }

                String updateSql = """
                        UPDATE movies
                        SET average_rating = (
                            SELECT ROUND(AVG(rating), 1)
                            FROM ratings
                            WHERE movie_id = ?
                        )
                        WHERE movie_id = ?
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(updateSql)) {

                    statement.setInt(1, selectedMovieId);
                    statement.setInt(2, selectedMovieId);
                    statement.executeUpdate();
                }

                connection.commit();

            } catch (SQLException ex) {
                connection.rollback();
                throw ex;

            } finally {
                connection.setAutoCommit(true);
            }

            loadMovies(searchField.getText().trim());
            loadRecommendations();

            JOptionPane.showMessageDialog(
                    this,
                    "Your rating and review have been saved.");

        } catch (SQLException ex) {

            showDatabaseError(
                    "Could not save rating/review. Check the unique key "
                    + "on ratings(user_id, movie_id).",
                    ex);
        }
    }

    private String loadExistingRating(int movieId) {

        String sql = """
                SELECT rating
                FROM ratings
                WHERE user_id = ? AND movie_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, getLoggedInUserId());
            statement.setInt(2, movieId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getBigDecimal("rating")
                            .stripTrailingZeros()
                            .toPlainString();
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return "5";
    }

    private String loadExistingReview(int movieId) {

        String sql = """
                SELECT review
                FROM ratings
                WHERE user_id = ? AND movie_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, getLoggedInUserId());
            statement.setInt(2, movieId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    String review = result.getString("review");
                    return review == null ? "" : review;
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return "";
    }

    // =========================================================
    // VIEW REVIEWS
    // =========================================================

    private void viewMovieReviews() {

        if (selectedMovieId == -1) {
            showNoMovieSelected();
            return;
        }

        String sql = """
                SELECT u.username, r.rating, r.review, r.rated_at
                FROM ratings r
                JOIN users u ON u.user_id = r.user_id
                WHERE r.movie_id = ?
                ORDER BY r.rated_at DESC
                """;

        StringBuilder reviews = new StringBuilder();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, selectedMovieId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    String review = result.getString("review");

                    reviews.append(result.getString("username"))
                            .append(" | Rating: ")
                            .append(result.getBigDecimal("rating"))
                            .append("/5\n")
                            .append(review == null || review.isBlank()
                                    ? "No written review."
                                    : review)
                            .append("\nDate: ")
                            .append(result.getTimestamp("rated_at"))
                            .append("\n\n----------------------\n\n");
                }
            }

            if (reviews.length() == 0) {
                reviews.append(
                        "No reviews yet. Be the first to rate this movie!");
            }

            JTextArea area =
                    new JTextArea(reviews.toString(), 16, 45);

            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setCaretPosition(0);

            JOptionPane.showMessageDialog(
                    this,
                    new JScrollPane(area),
                    "Reviews: " + selectedMovieTitle,
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException ex) {
            showDatabaseError("Could not load reviews.", ex);
        }
    }

    // =========================================================
    // GET LOGGED-IN USER ID
    // =========================================================

    private int getLoggedInUserId() throws SQLException {

        String sql =
                "SELECT user_id FROM users WHERE username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("user_id");
                }
            }
        }

        throw new SQLException("Logged-in user was not found.");
    }

    // =========================================================
    // ADD TO WATCHLIST
    // =========================================================

    private void addToWatchlist() {

        if (selectedMovieId == -1) {
            showNoMovieSelected();
            return;
        }

        String sql = """
                INSERT INTO watchlist (user_id, movie_id)
                VALUES (?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, getLoggedInUserId());
            statement.setInt(2, selectedMovieId);
            statement.executeUpdate();

            JOptionPane.showMessageDialog(
                    this,
                    "Movie added to your watchlist!");

        } catch (SQLException ex) {

            if ("23000".equals(ex.getSQLState())) {
                JOptionPane.showMessageDialog(
                        this,
                        "This movie is already in your watchlist.");
            } else {
                showDatabaseError(
                        "Could not add movie to watchlist.", ex);
            }
        }
    }

    // =========================================================
    // MY WATCHLIST
    // =========================================================

    private void openMyWatchlist() {

        try {

            int userId = getLoggedInUserId();

            String sql = """
                    SELECT m.movie_id, m.title, m.release_year,
                           m.language, m.average_rating
                    FROM watchlist w
                    JOIN movies m ON w.movie_id = m.movie_id
                    WHERE w.user_id = ?
                    ORDER BY w.added_at DESC
                    """;

            javax.swing.table.DefaultTableModel model =
                    new javax.swing.table.DefaultTableModel(
                            new String[]{
                                "ID", "Movie Title", "Year",
                                "Language", "Rating"
                            }, 0) {

                        @Override
                        public boolean isCellEditable(
                                int row, int column) {
                            return false;
                        }
                    };

            try (Connection connection =
                         DatabaseConnection.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setInt(1, userId);

                try (ResultSet result = statement.executeQuery()) {

                    while (result.next()) {

                        model.addRow(new Object[]{
                                result.getInt("movie_id"),
                                result.getString("title"),

                                result.getObject("release_year") == null
                                        ? "N/A"
                                        : result.getObject("release_year"),

                                result.getString("language") == null
                                        ? "N/A"
                                        : result.getString("language"),

                                result.getObject("average_rating") == null
                                        ? "N/A"
                                        : result.getObject("average_rating")
                        });
                    }
                }
            }

            javax.swing.JTable table =
                    new javax.swing.JTable(model);

            table.setSelectionMode(
                    javax.swing.ListSelectionModel.SINGLE_SELECTION);

            table.setRowHeight(28);
            table.setAutoCreateRowSorter(true);

            JButton removeButton =
                    new JButton("Remove Selected");

            JButton detailsButton =
                    new JButton("View Details");

            JButton closeButton =
                    new JButton("Close");

            JPanel buttons =
                    new JPanel(new FlowLayout(FlowLayout.RIGHT));

            buttons.add(detailsButton);
            buttons.add(removeButton);
            buttons.add(closeButton);

            JPanel panel = new JPanel(new BorderLayout(8, 8));

            panel.setBorder(BorderFactory.createEmptyBorder(
                    10, 10, 10, 10));

            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            panel.add(buttons, BorderLayout.SOUTH);

            JDialog dialog =
                    new JDialog(this, "My Watchlist", true);

            dialog.setSize(750, 420);
            dialog.setLocationRelativeTo(this);
            dialog.setContentPane(panel);

            closeButton.addActionListener(e -> dialog.dispose());

            detailsButton.addActionListener(e -> {

                int row = table.getSelectedRow();

                if (row == -1) {
                    JOptionPane.showMessageDialog(
                            dialog, "Select a movie first.");
                    return;
                }

                int modelRow = table.convertRowIndexToModel(row);

                selectMovie(
                        (int) model.getValueAt(modelRow, 0),
                        String.valueOf(
                                model.getValueAt(modelRow, 1)));

                dialog.dispose();
                showMovieDetails();
            });

            removeButton.addActionListener(e -> {

                int row = table.getSelectedRow();

                if (row == -1) {
                    JOptionPane.showMessageDialog(
                            dialog, "Select a movie to remove.");
                    return;
                }

                int modelRow = table.convertRowIndexToModel(row);

                int movieId =
                        (int) model.getValueAt(modelRow, 0);

                int confirm = JOptionPane.showConfirmDialog(
                        dialog,
                        "Remove this movie from your watchlist?",
                        "Confirm Removal",
                        JOptionPane.YES_NO_OPTION);

                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }

                String deleteSql = """
                        DELETE FROM watchlist
                        WHERE user_id = ? AND movie_id = ?
                        """;

                try (Connection connection =
                             DatabaseConnection.getConnection();
                     PreparedStatement statement =
                             connection.prepareStatement(deleteSql)) {

                    statement.setInt(1, userId);
                    statement.setInt(2, movieId);

                    statement.executeUpdate();
                    model.removeRow(modelRow);

                } catch (SQLException ex) {
                    showDatabaseError(
                            "Could not remove movie.", ex);
                }
            });

            dialog.setVisible(true);

        } catch (SQLException ex) {
            showDatabaseError("Could not load watchlist.", ex);
        }
    }

    // =========================================================
    // ERROR HELPERS
    // =========================================================

    private void showNoMovieSelected() {

        JOptionPane.showMessageDialog(
                this,
                "Please select a movie first.",
                "No Movie Selected",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showDatabaseError(
            String message, SQLException ex) {

        JOptionPane.showMessageDialog(
                this,
                message + "\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);

        ex.printStackTrace();
    }
}
