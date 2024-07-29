package io.github.mateuszuran.sisyphus_app.exception;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ScraperException extends RuntimeException {
    private final HttpStatus status;

    public ScraperException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR; // Default status
    }

    public ScraperException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}
