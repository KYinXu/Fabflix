CREATE TABLE movies (
    id varchar(10) PRIMARY KEY,
    title varchar(100) NOT NULL DEFAULT '',
    year integer NOT NULL,
    director varchar(100) NOT NULL DEFAULT ''
);

CREATE TABLE stars (
    id varchar(10) PRIMARY KEY,
    name varchar(100) NOT NULL DEFAULT '',
    birth_year integer<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>my-tomcat-project</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <dependencies>
        <!-- Example: MySQL connector -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.33</version>
            <scope>runtime</scope>
        </dependency>
    </dependencies>

</project>

);

CREATE TABLE stars_in_movies (
    star_id varchar(10) REFERENCES stars(id),
    movie_id varchar(10) REFERENCES movies(id),
    PRIMARY KEY (star_id, movie_id)
);

CREATE TABLE genres (
    id integer AUTO_INCREMENT PRIMARY KEY,
    name varchar(32) NOT NULL DEFAULT ''
);

CREATE TABLE genres_in_movies (
    genre_id integer REFERENCES genres(id),
    movie_id varchar(10) REFERENCES movies(id),
    PRIMARY KEY (genre_id, movie_id)
);

CREATE TABLE customers (
    id integer AUTO_INCREMENT PRIMARY KEY,
    first_name varchar(50) NOT NULL DEFAULT '',
    last_name varchar(50) NOT NULL DEFAULT '',
    credit_card_id varchar(20) REFERENCES credit_cards(id),
    address varchar(200) NOT NULL DEFAULT '',
    email varchar(50) NOT NULL DEFAULT '',
    password varchar(20) NOT NULL DEFAULT '' # alter later for security concerns
);

CREATE TABLE sales (
    id integer AUTO_INCREMENT PRIMARY KEY,
    customer_id integer REFERENCES customers(id) NOT NULL,
    movie_id varchar(10) REFERENCES movies(id),
    sale_date date NOT NULL
);

CREATE TABLE credit_cards(
    id varchar(20) PRIMARY KEY,
    first_name varchar(50) NOT NULL DEFAULT '',
    last_name varchar(50) NOT NULL DEFAULT '',
    expiration date NOT NULL
);

CREATE TABLE ratings(
    movie_id varchar(10) REFERENCES movies(id),
    ratings float NOT NULL,
    vote_count integer NOT NULL
);