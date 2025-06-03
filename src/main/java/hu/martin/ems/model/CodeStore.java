package hu.martin.ems.model;

import com.google.gson.annotations.Expose;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@NeedCleanCoding
public class CodeStore extends BaseEntity {
    @Column(nullable = false)
    @Expose
    private String name;

    @ManyToOne
    @JoinColumn(name = "parentcodestore_codestore_id", columnDefinition = "BIGINT")
    @Expose
    private CodeStore parentCodeStore;

    @Column(nullable = false)
    @Expose
    private String linkName;

    @Column(nullable = false)
    @Expose
    private Boolean deletable;
}
