package hu.martin.ems.model;

import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CodeStore extends BaseEntity{
    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "parentcodestore_codestore_id", columnDefinition = "BIGINT")
    private CodeStore parentCodeStore;

    @Column(nullable = false)
    private String linkName;

    @Column(nullable = false)
    private Boolean deletable;

    public CodeStore(String name, CodeStore parent){
        this.name = name;
        this.linkName = name;
        this.parentCodeStore = parent;
        this.deletable = true;
    }
    public CodeStore(String name, CodeStore parent, Boolean deletable){
        this.name = name;
        this.linkName = name;
        this.parentCodeStore = parent;
        this.deletable = deletable;
    }
}
