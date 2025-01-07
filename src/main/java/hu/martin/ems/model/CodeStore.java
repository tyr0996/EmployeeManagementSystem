package hu.martin.ems.model;

import com.google.gson.annotations.Expose;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

//    public CodeStore(String name, CodeStore parent) {
//        this.name = name;
//        this.linkName = name;
//        this.parentCodeStore = parent;
//        this.deletable = true;
//    }
//
//    public CodeStore(String name, CodeStore parent, Boolean deletable) {
//        this.name = name;
//        this.linkName = name;
//        this.parentCodeStore = parent;
//        this.deletable = deletable;
//    }
}
