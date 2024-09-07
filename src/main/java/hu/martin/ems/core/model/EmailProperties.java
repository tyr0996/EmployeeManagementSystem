package hu.martin.ems.core.model;

import hu.martin.ems.NeedCleanCoding;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@NeedCleanCoding
public class EmailProperties {
    private String to;
    private String subject;
    private String htmlText;
    private List<EmailAttachment> attachments;
}
