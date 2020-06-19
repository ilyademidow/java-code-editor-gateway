package ru.idemidov.interviewgateway.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.idemidov.interviewgateway.model.ApiKeySettings;

@Repository
public interface ApiKeySettingsRepository extends CrudRepository<ApiKeySettings, String> {
}
