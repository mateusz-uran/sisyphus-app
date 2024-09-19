package io.github.mateuszuran.sisyphus_app.util;

import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class TimeUtil {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    public String formatCreationTime() {
        return dateFormat.format(new Date());
    }

    public Timestamp convertCreationTime(String date) throws ParseException {
        Date parseDate = dateFormat.parse(date);
        return new Timestamp(parseDate.getTime());
    }
}
