package io.github.mateuszuran.sisyphus_app.repository;

import io.github.mateuszuran.sisyphus_app.model.Specification;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpecificationRepository extends MongoRepository<Specification, String> {
}
