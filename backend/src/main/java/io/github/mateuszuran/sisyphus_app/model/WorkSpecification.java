package io.github.mateuszuran.sisyphus_app.model;

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
@Document(collection = "work_specifications")
public class WorkSpecification {
    @Id
    private String id;

    private List<String> technologies;
    private List<String> requirements;
    private String companyName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkSpecification that = (WorkSpecification) o;
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
