package ETLPipeline;

import ETLPipeline.types.ETLConfig;
import ETLPipeline.types.ETLException;
import ETLPipeline.types.GenreMovieRelationRecord;
import ETLPipeline.types.MovieRecord;
import ETLPipeline.types.StarMovieRelation;
import ETLPipeline.types.StarRecord;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseWriter {
    private ETLConfig config;
    private boolean driverLoaded = false;
    private final Map<String, Integer> genreCache = new HashMap<>();
    
    /**
     * Constructor - uses default ETLConfig from Parameters
     */
    public DatabaseWriter() {
        this(new ETLConfig());
    }
    
    /**
     * Constructor with custom ETLConfig
     * @param config ETL configuration
     */
    public DatabaseWriter(ETLConfig config) {
        this.config = config != null ? config : new ETLConfig();
        applyDefaultsFromParameters();
    }
    
    /**
     * Write genres to database
     * @param genres Collection of genre names
     */
    
    /**
     * Write stars to database
     * @param stars List of star data
     */
    public void writeStars(List<StarRecord> stars) {
        String sql = "INSERT INTO stars (id, name, birth_year) VALUES (?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE name=VALUES(name), birth_year=VALUES(birth_year)";
        executeRecords(stars, sql, (statement, star) -> {
            statement.setString(1, star.getId());
            statement.setString(2, safeString(star.getName()));
            if (star.getBirthYear() != null) {
                statement.setInt(3, star.getBirthYear());
            } else {
                statement.setNull(3, Types.INTEGER);
            }
        });
    }
    
    /**
     * Write movies to database
     * @param movies List of movie data
     */
    public void writeMovies(List<MovieRecord> movies) {
        if (movies == null || movies.isEmpty()) {
            return;
        }
        
        List<MovieRecord> eligible = new java.util.ArrayList<>(movies.size());
        for (MovieRecord movie : movies) {
            if (movie == null) {
                continue;
            }
            if (movie.getYear() == null) {
                System.err.println("Skipping movie '" + movie.getId()
                    + "' due to missing year (column requires a value).");
                continue;
            }
            eligible.add(movie);
        }
        
        String sql = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE title=VALUES(title), year=VALUES(year), director=VALUES(director)";
        executeRecords(eligible, sql, (statement, movie) -> {
            statement.setString(1, movie.getId());
            statement.setString(2, safeString(movie.getTitle()));
            statement.setInt(3, movie.getYear());
            statement.setString(4, safeString(movie.getDirector()));
        });
    }
    
    /**
     * Write star-movie relationships to database
     * @param starMovieRelations List of star-movie relationship data
     */
    public void writeStarMovieRelations(List<StarMovieRelation> starMovieRelations) {
        String sql = "INSERT INTO stars_in_movies (star_id, movie_id) VALUES (?, ?) "
                   + "ON DUPLICATE KEY UPDATE star_id=VALUES(star_id)";
        executeRecords(starMovieRelations, sql, (statement, relation) -> {
            statement.setString(1, relation.getStarId());
            statement.setString(2, relation.getMovieId());
        });
    }
    
    /**
     * Write genre-movie relationships to database
     * @param genreMovieRelations List of genre-movie relationship data
     */
    public void writeGenreMovieRelations(List<GenreMovieRelationRecord> genreMovieRelations) {
        if (genreMovieRelations == null || genreMovieRelations.isEmpty()) {
            return;
        }
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "INSERT INTO genres_in_movies (genre_id, movie_id) VALUES (?, ?)")) {
            ensureGenreCache(connection);
            for (GenreMovieRelationRecord relation : genreMovieRelations) {
                if (relation == null) {
                    continue;
                }
                String normalizedGenre = normalizeGenreName(relation.getGenreName());
                if (normalizedGenre == null) {
                    continue;
                }
                Integer genreId = genreCache.get(normalizedGenre);
                if (genreId == null) {
                    genreId = insertGenre(connection, normalizedGenre);
                }
                if (genreId == null) {
                    System.err.println("Skipping genre-movie relation for movie '" + relation.getMovieId()
                        + "' because genre '" + normalizedGenre + "' could not be resolved.");
                    continue;
                }
                try {
                    statement.setInt(1, genreId);
                    statement.setString(2, relation.getMovieId());
                    statement.executeUpdate();
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.err.println("Skipping genre-movie relation due to constraint violation: " + e.getMessage());
                } catch (SQLException e) {
                    System.err.println("Skipping genre-movie relation because of SQL error: " + e.getMessage());
                } finally {
                    statement.clearParameters();
                }
            }
        } catch (SQLException e) {
            throw new ETLException("Failed to write genre-movie relations", e);
        }
    }
    
    /**
     * Write ratings to database
     * @param ratings List of rating data
     */
    public void writeRatings(List<?> ratings) {
        // Not yet implemented - ratings are outside current ETL scope
    }
    
    private Connection getConnection() {
        ensureConfig();
        try {
            loadDriver();
            return DriverManager.getConnection(config.dbUrl, config.dbUser, config.dbPassword);
        } catch (SQLException e) {
            throw new ETLException("Failed to obtain database connection", e);
        }
    }
    
    private void loadDriver() {
        if (driverLoaded) {
            return;
        }
        try {
            Class.forName(config.dbDriver);
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            throw new ETLException("Database driver not found: " + config.dbDriver, e);
        }
    }
    
    private void ensureConfig() {
        applyDefaultsFromParameters();
        if (config.dbUrl == null || config.dbUser == null || config.dbPassword == null) {
            throw new ETLException("Database configuration is incomplete. Ensure dbUrl, dbUser, and dbPassword are set.");
        }
    }
    
    private String safeString(String value) {
        return value != null ? value : "";
    }
    
    private <T> void executeRecords(List<T> records,
                                    String sql,
                                    SqlBinder<T> binder) {
        if (records == null || records.isEmpty()) {
            return;
        }
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (T record : records) {
                if (record == null) {
                    continue;
                }
                try {
                    binder.bind(statement, record);
                    statement.executeUpdate();
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.err.println("Skipping record due to constraint violation: " + e.getMessage());
                } catch (SQLException e) {
                    System.err.println("Skipping record due to SQL error: " + e.getMessage());
                } finally {
                    statement.clearParameters();
                }
            }
        } catch (SQLException e) {
            throw new ETLException("Failed to execute SQL operation", e);
        }
    }
    
    @FunctionalInterface
    private interface SqlBinder<T> {
        void bind(PreparedStatement statement, T record) throws SQLException;
    }
    
    private void applyDefaultsFromParameters() {
        if (config == null) {
            config = new ETLConfig();
        }
        
        String dbType = null;
        String dbName = null;
        String user = null;
        String password = null;
        
        try {
            Class<?> parametersClass = Class.forName("Parameters");
            dbType = readStaticString(parametersClass, "dbtype");
            dbName = readStaticString(parametersClass, "dbname");
            user = readStaticString(parametersClass, "username");
            password = readStaticString(parametersClass, "password");
        } catch (ClassNotFoundException ignored) {
            // Parameters.java not available; rely on existing config values.
        }
        
        if ((config.dbUrl == null || config.dbUrl.isBlank()) && dbType != null && dbName != null) {
            config.dbUrl = "jdbc:" + dbType + ":///" + dbName
                + "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true";
        }
        if ((config.dbUser == null || config.dbUser.isBlank()) && user != null) {
            config.dbUser = user;
        }
        if (config.dbPassword == null && password != null) {
            config.dbPassword = password;
        }
    }
    
    private String readStaticString(Class<?> parametersClass, String fieldName) {
        try {
            Object value = parametersClass.getField(fieldName).get(null);
            return value instanceof String ? (String) value : null;
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            return null;
        }
    }
    
    public void writeGenres(Collection<String> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        try (Connection connection = getConnection();
             PreparedStatement insert = connection.prepareStatement(
                 "INSERT INTO genres (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ensureGenreCache(connection);
            for (String genre : genres) {
                String normalized = normalizeGenreName(genre);
                if (normalized == null || genreCache.containsKey(normalized)) {
                    continue;
                }
                try {
                    insert.setString(1, normalized);
                    insert.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        if (keys.next()) {
                            genreCache.put(normalized, keys.getInt(1));
                        }
                    }
                } catch (SQLIntegrityConstraintViolationException e) {
                    Integer id = fetchGenreId(connection, normalized);
                    if (id != null) {
                        genreCache.put(normalized, id);
                    }
                } catch (SQLException e) {
                    System.err.println("Skipping genre '" + normalized + "' due to SQL error: " + e.getMessage());
                } finally {
                    insert.clearParameters();
                }
            }
        } catch (SQLException e) {
            throw new ETLException("Failed to write genres", e);
        }
    }
    
    private void ensureGenreCache(Connection connection) throws SQLException {
        if (!genreCache.isEmpty()) {
            return;
        }
        try (PreparedStatement statement = connection.prepareStatement("SELECT id, name FROM genres");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                genreCache.putIfAbsent(resultSet.getString("name"), resultSet.getInt("id"));
            }
        }
    }
    
    private Integer insertGenre(Connection connection, String genreName) {
        try (PreparedStatement insert = connection.prepareStatement(
                 "INSERT INTO genres (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, genreName);
            insert.executeUpdate();
            try (ResultSet keys = insert.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    genreCache.put(genreName, id);
                    return id;
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            try {
                return fetchGenreId(connection, genreName);
            } catch (SQLException ex) {
                System.err.println("Failed to resolve genre '" + genreName + "': " + ex.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Failed to insert genre '" + genreName + "': " + e.getMessage());
        }
        return null;
    }
    
    private Integer fetchGenreId(Connection connection, String genreName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                 "SELECT id FROM genres WHERE name = ?")) {
            statement.setString(1, genreName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt(1);
                    genreCache.put(genreName, id);
                    return id;
                }
            }
        }
        return null;
    }
    
    private String normalizeGenreName(String genre) {
        if (genre == null) {
            return null;
        }
        String trimmed = genre.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
