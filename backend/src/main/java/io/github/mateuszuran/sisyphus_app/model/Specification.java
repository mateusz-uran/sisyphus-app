package io.github.mateuszuran.sisyphus_app.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "specifications")
@JsonIgnoreProperties(value = { "target", "source" })
public class Specification {
    @Id
    private String id;

    private List<String> technologies;
    private List<String> requirements;
    private String companyName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Specification that = (Specification) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(technologies, that.technologies) &&
                Objects.equals(requirements, that.requirements) &&
                Objects.equals(companyName, that.companyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, technologies, requirements, companyName);
    }
}
