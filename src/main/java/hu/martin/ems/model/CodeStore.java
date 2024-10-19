package hu.martin.ems.model;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@NeedCleanCoding
public class CodeStore extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "parentcodestore_codestore_id", columnDefinition = "BIGINT")
    private CodeStore parentCodeStore;

    @Column(nullable = false)
    private String linkName;

    @Column(nullable = false)
    private Boolean deletable;

    public CodeStore(String name, CodeStore parent) {
        this.name = name;
        this.linkName = name;
        this.parentCodeStore = parent;
        this.deletable = true;
    }

    public CodeStore(String name, CodeStore parent, Boolean deletable) {
        this.name = name;
        this.linkName = name;
        this.parentCodeStore = parent;
        this.deletable = deletable;
    }
}
