package io.github.mateuszuran.sisyphus_app.util;

import io.github.mateuszuran.sisyphus_app.model.WorkGroup;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Comparator;

@Slf4j
public class WorkGroupComparator implements Comparator<WorkGroup> {
    private final TimeUtil util;

    public WorkGroupComparator(TimeUtil util) {
        this.util = util;
    }

    @Override
    public int compare(WorkGroup g1, WorkGroup g2) {
        var time1 = convert(g1.getCreationTime());
        var time2 = convert(g2.getCreationTime());


        if (time1 == null || time2 == null) {
            log.error("Null timestamp encountered for g1: {} and g2: {}", g1.getCreationTime(), g2.getCreationTime());
            return 0;
        }

        if (time1.before(time2)) {
            return 1;
        } else if (time1.after(time2)) {
            return -1;
        } else {
            return 0;
        }
    }

    public Timestamp convert(String time) {
        if (time == null || time.trim().isEmpty()) {
            log.warn("Received null or empty string");
            return null;
        }

        try {
            return util.convertCreationTime(time);
        } catch (ParseException e) {
            log.error("Failed to parse date: {}. Error: {}", time, e.getMessage());
            return null;
        }
    }
}
