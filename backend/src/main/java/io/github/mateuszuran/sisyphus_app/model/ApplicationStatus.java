package io.github.mateuszuran.sisyphus_app.model;

public enum ApplicationStatus {
    IN_PROGRESS, REJECTED, SENT, HIRED;

    public static ApplicationStatus getByUpperCaseStatus(String status) {
        if (status == null || status.isEmpty()) {
            return null;
        }
        return ApplicationStatus.valueOf(status.toUpperCase());
    }
}
