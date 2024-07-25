package io.github.mateuszuran.sisyphus_app.repository;

import io.github.mateuszuran.sisyphus_app.model.WorkSpecification;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WorkSpecificationRepository extends MongoRepository<WorkSpecification, String> {
}
