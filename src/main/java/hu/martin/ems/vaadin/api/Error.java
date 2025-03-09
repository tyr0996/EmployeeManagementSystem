package hu.martin.ems.vaadin.api;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Error {

    @Expose
    private long timestamp;

    @Expose
    private int status;

    @Getter
    @Expose
    private String error;

    @Expose
    private String path;
}