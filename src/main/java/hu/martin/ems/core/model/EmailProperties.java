package hu.martin.ems.core.model;

import com.google.gson.annotations.Expose;
import hu.martin.ems.annotations.NeedCleanCoding;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@NeedCleanCoding
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
