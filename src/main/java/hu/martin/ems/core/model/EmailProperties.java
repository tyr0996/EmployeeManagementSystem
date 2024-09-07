package hu.martin.ems.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmailProperties {
    private String to;
    private String subject;
    private String htmlText;
    private List<EmailAttachment> attachments;
}
