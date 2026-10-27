
package com.cinematch.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.cinematch.service.RecommendationService;
import com.cinematch.service.RecommendationService.MovieRecommendation;
import com.cinematch.util.DatabaseConnection;

public class RecommendationsFrame extends JFrame {

    private final int userId;
    private final RecommendationService recommendationService;
    private final JPanel moviesPanel;

    public RecommendationsFrame(int userId) {
        this.userId = userId;
        this.recommendationService = new RecommendationService();

        setTitle("SoWhatsNext - Recommendations");
        setSize(850, 600);
        setMinimumSize(new Dimension(650, 400));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel heading = new JLabel("Recommended For You");
        heading.setFont(new Font("Arial", Font.BOLD, 24));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadRecommendations());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(heading, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        moviesPanel = new JPanel();
        moviesPanel.setLayout(new BoxLayout(moviesPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(moviesPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
        loadRecommendations();
    }

    private void loadRecommendations() {
        moviesPanel.removeAll();

        try {
            List<MovieRecommendation> recommendations =
                    recommendationService.getRecommendations(userId);

            if (recommendations.isEmpty()) {
                JLabel emptyLabel = new JLabel(
                        "<html><div style='text-align:center;'>"
                        + "No recommendations available yet.<br>"
                        + "Rate some movies and add watched movies to improve "
                        + "your recommendations.</div></html>",
                        SwingConstants.CENTER
                );
                emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                moviesPanel.add(Box.createVerticalStrut(30));
                moviesPanel.add(emptyLabel);
            } else {
                for (MovieRecommendation movie : recommendations) {
                    moviesPanel.add(createMovieCard(movie));
                    moviesPanel.add(Box.createVerticalStrut(10));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load recommendations:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        moviesPanel.revalidate();
        moviesPanel.repaint();
    }

    private JPanel createMovieCard(MovieRecommendation movie) {
        JPanel card = new JPanel(new BorderLayout(12, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(12, 12, 12, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JLabel posterLabel = new JLabel("No Poster", SwingConstants.CENTER);
        posterLabel.setPreferredSize(new Dimension(75, 110));

        String posterUrl = movie.getPosterUrl();

        if (posterUrl != null && !posterUrl.isBlank()) {
            try {
                ImageIcon originalIcon = new ImageIcon(new java.net.URL(posterUrl));
                Image scaledImage = originalIcon.getImage().getScaledInstance(
                        75, 110, Image.SCALE_SMOOTH
                );
                posterLabel.setText("");
                posterLabel.setIcon(new ImageIcon(scaledImage));
            } catch (Exception ignored) {
                posterLabel.setText("No Poster");
            }
        }

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(movie.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 17));

        String year = movie.getReleaseYear() == null
                ? "Year unknown"
                : movie.getReleaseYear().toString();

        JLabel infoLabel = new JLabel(
                "Year: " + year
                + "  |  Language: "
                + (movie.getLanguage() == null ? "N/A" : movie.getLanguage())
        );

        JLabel ratingLabel = new JLabel(String.format(
                "Average rating: %.1f/10  |  Match score: %.1f",
                movie.getAverageRating(),
                movie.getRecommendationScore()
        ));

        JButton watchedButton = new JButton("Mark as Watched");
        watchedButton.addActionListener(e -> markAsWatched(movie));

        detailsPanel.add(titleLabel);
        detailsPanel.add(Box.createVerticalStrut(8));
        detailsPanel.add(infoLabel);
        detailsPanel.add(Box.createVerticalStrut(5));
        detailsPanel.add(ratingLabel);
        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(watchedButton);

        card.add(posterLabel, BorderLayout.WEST);
        card.add(detailsPanel, BorderLayout.CENTER);

        return card;
    }

    private void markAsWatched(MovieRecommendation movie) {
        String sql = """
                INSERT INTO watch_history (user_id, movie_id)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE watched_at = CURRENT_TIMESTAMP
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setInt(2, movie.getMovieId());
            statement.executeUpdate();

            JOptionPane.showMessageDialog(
                    this,
                    movie.getTitle() + " marked as watched!"
            );

            loadRecommendations();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not update watch history:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
