package io.github.mateuszuran.sisyphus_app.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "applications")
public class Applications {
    @Id
    private String id;

    private String workUrl;
    private String appliedDate;
    private ApplicationStatus status;

    @DocumentReference(lazy = true)
    private Specification specification;
}
