package hu.martin.ems.vaadin.api;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Error{
    @Expose
    private long timestamp;
    @Expose
    private int status;
    @Expose
    private String error;
    @Expose
    private String path;
}