package io.github.mateuszuran.sisyphus_app.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SpecificationDTO(String company_name, List<String> technologies_expected, List<String> requirements_expected) {}
