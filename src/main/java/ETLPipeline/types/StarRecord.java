package ETLPipeline.types;

import java.util.Objects;

public class StarRecord {
    private final String id;
    private final String name;
    private final Integer birthYear;

    public StarRecord(String id, String name, Integer birthYear) {
        this.id = id;
        this.name = name;
        this.birthYear = birthYear;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StarRecord)) {
            return false;
        }
        StarRecord that = (StarRecord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

