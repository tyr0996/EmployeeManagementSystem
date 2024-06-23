package hu.martin.ems.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class EmailAttachment {
    String contentType;
    byte[] data;
    String fileName;
}
