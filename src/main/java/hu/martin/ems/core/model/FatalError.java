package hu.martin.ems.core.model;

import hu.martin.ems.annotations.NeedCleanCoding;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class FatalError {

    private Integer errorId;
    private String entityType;
    private String operation;
    private BaseEntity entity;
    private String description;
    private LocalDateTime dateTime;

    public FatalError(BaseEntity entity, String operation, String description){
        this.entity = entity;
        this.entityType = entity.getClass().getSimpleName();
        this.operation = operation;
        this.description = description;
        this.dateTime = LocalDateTime.now();
    }

    @PostConstruct
    public void init(){
        this.errorId = this.hashCode();
        write();
    }

    private void write(){
        String fileName = this.errorId.toString();
        //TODO
    }


}
