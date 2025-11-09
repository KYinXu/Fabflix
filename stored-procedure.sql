DELIMITER $$

CREATE PROCEDURE add_movie(
    IN p_title VARCHAR(100),
    IN p_year INT,
    IN p_director VARCHAR(100),
    IN p_star_name VARCHAR(100),
    IN p_genre_name VARCHAR(32)
)
BEGIN
    DECLARE v_movie_id VARCHAR(10);
    DECLARE v_star_id VARCHAR(10);
    DECLARE v_genre_id INT;

    SELECT id INTO v_movie_id
    FROM movies
    WHERE title = p_title AND year = p_year AND director = p_director
    LIMIT 1;

    IF v_movie_id IS NULL THEN
        SET v_movie_id = CONCAT('tt', LPAD((SELECT COUNT(*) + 1 FROM movies), 7, '0'));
        INSERT INTO movies(id, title, year, director)
        VALUES (v_movie_id, p_title, p_year, p_director);
    END IF;

    SELECT id INTO v_star_id
    FROM stars
    WHERE name = p_star_name
    LIMIT 1;

    IF v_star_id IS NULL THEN
        SET v_star_id = CONCAT('nm', LPAD((SELECT COUNT(*) + 1 FROM stars), 7, '0'));
        INSERT INTO stars(id, name)
        VALUES (v_star_id, p_star_name);
    END IF;

    SELECT id INTO v_genre_id
    FROM genres
    WHERE name = p_genre_name
    LIMIT 1;

    IF v_genre_id IS NULL THEN
        INSERT INTO genres(name) VALUES (p_genre_name);
        SET v_genre_id = LAST_INSERT_ID();
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM stars_in_movies
        WHERE star_id = v_star_id AND movie_id = v_movie_id
    ) THEN
        INSERT INTO stars_in_movies(star_id, movie_id)
        VALUES (v_star_id, v_movie_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM genres_in_movies
        WHERE genre_id = v_genre_id AND movie_id = v_movie_id
    ) THEN
        INSERT INTO genres_in_movies(genre_id, movie_id)
        VALUES (v_genre_id, v_movie_id);
    END IF;

END$$

DELIMITER ;
