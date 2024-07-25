package io.github.mateuszuran.sisyphus_app.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "work_applications")
public class WorkApplications {
    @Id
    private String id;

    private String workUrl;
    private String appliedDate;
    private ApplicationStatus status;

    @DocumentReference(lazy = true)
    WorkSpecification specification;
}
