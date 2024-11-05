package io.github.mateuszuran.sisyphus_app.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Slf4j
@Getter
@Setter
@Builder
@Document(collection = "work_group")
public class WorkGroup {
    @Id
    private String id;

    private Binary cvData;
    private String cvFileName;
    private String creationTime;
    private int sent;
    private int rejected;
    private int inProgress;
    private boolean isHired;

    @DocumentReference(lazy = true)
    private List<Applications> applications;

    public void incrementCounter(String status) {
        switch (status.toUpperCase()) {
            case "SENT":
                setSent(this.sent + 1);
                break;
            case "REJECTED":
                setRejected(this.rejected + 1);
                break;
            case "IN_PROGRESS":
                setInProgress(this.inProgress + 1);
                break;
            case "HIRED":
                setHired(!this.isHired);
                break;
        }
    }

    public void decrementCounter(String status) {
        switch (status.toUpperCase()) {
            case "SENT":
                setSent(this.sent - 1);
                break;
            case "REJECTED":
                setRejected(this.rejected - 1);
                break;
            case "IN_PROGRESS":
                setInProgress(this.inProgress - 1);
                break;
            case "HIRED":
                setHired(!this.isHired);
                break;
        }
    }
}
