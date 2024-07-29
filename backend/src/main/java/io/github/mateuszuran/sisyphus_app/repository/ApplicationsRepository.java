package io.github.mateuszuran.sisyphus_app.repository;

import io.github.mateuszuran.sisyphus_app.model.Applications;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApplicationsRepository extends MongoRepository<Applications, String> {
}
