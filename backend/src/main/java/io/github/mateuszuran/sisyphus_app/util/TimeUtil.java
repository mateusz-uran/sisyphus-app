package io.github.mateuszuran.sisyphus_app.util;

import io.github.mateuszuran.sisyphus_app.dto.WorkGroupDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Component
@Slf4j
public class TimeUtil {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    public String formatCreationTime() {
        return dateFormat.format(new Date());
    }

    public Timestamp convertCreationTime(String date) throws ParseException {
        if (date == null || date.trim().isEmpty()) {
            log.info("Given date is null: {}", date);
            return null;
        }

        Date parseDate = dateFormat.parse(date);
        return new Timestamp(parseDate.getTime());
    }
}
