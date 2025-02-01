package hu.martin.ems.core.model;

import com.google.gson.annotations.Expose;
import hu.martin.ems.annotations.NeedCleanCoding;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@NeedCleanCoding
@EqualsAndHashCode
public class EmailProperties {
    @Expose
    private String to;

    @Expose
    private String subject;

    @Expose
    private String htmlText;

    @Expose
    private List<EmailAttachment> attachments;
}
