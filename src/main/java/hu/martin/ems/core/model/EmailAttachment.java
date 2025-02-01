package hu.martin.ems.core.model;

import com.google.gson.annotations.Expose;
import hu.martin.ems.annotations.NeedCleanCoding;
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
    @Expose
    String contentType;

    @Expose
    byte[] data;

    @Expose
    String fileName;
}
