package hu.martin.ems.core.model;

import hu.martin.ems.NeedCleanCoding;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@NeedCleanCoding
public class EmailAttachment {
    String contentType;
    byte[] data;
    String fileName;
}
