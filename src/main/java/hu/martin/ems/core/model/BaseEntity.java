package hu.martin.ems.core.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@MappedSuperclass
@Slf4j
public abstract class BaseEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, columnDefinition = "SERIAL")
    @Getter
    @Id
    public Long id;

    @Getter
    @Setter
    private Long deleted;

    private static final ObjectMapper om = new ObjectMapper();

    static {
        om.registerModule(new JavaTimeModule());
    }

    @Override
    public String toString() {
        try {
            return om.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
