package hu.martin.ems.core.model;

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
    private String to;
    private String subject;
    private String htmlText;
    private List<EmailAttachment> attachments;
}
