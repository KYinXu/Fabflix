package ETLPipeline.types;

import java.util.Objects;

public class MovieRecord {
    private final String id;
    private final String title;
    private final Integer year;
    private final String director;

    public MovieRecord(String id, String title, Integer year, String director) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Integer getYear() {
        return year;
    }

    public String getDirector() {
        return director;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MovieRecord)) {
            return false;
        }
        MovieRecord that = (MovieRecord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

