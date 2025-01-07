package hu.martin.ems.core.model;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import hu.martin.ems.core.config.BeanProvider;
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
    @Expose
    public Long id;

    @Getter
    @Setter
    @Expose
    private Long deleted;

    @Override
    public String toString() {
        return BeanProvider.getBean(Gson.class).toJson(this);
    }
}
