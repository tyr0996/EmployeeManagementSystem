package hu.martin.ems.core.model;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EmsResponse {
    @Expose
    private int code;

    @Expose
    private Object responseData;

    @Expose
    private String description;

    public EmsResponse(int code, String description){
        this(code, null, description);
    }

    public interface Description {
        String SFTP_SENDING_ERROR = "Error happened when sending with SFTP";
        String SFTP_SENDING_SUCCESS = "SFTP sending is done";
        String CLEAR_DATABASE_SUCCESS = "Clearing database was successful";
        String CLEAR_DATABASE_FAILED = "Clearing database failed for one or more table";
        String FETCHING_CURRENCIES_FAILED = "Error happened while fetching exchange rates";
        String PARSING_CURRENCIES_FAILED = "Currencies fetched successfully, but the currency server sent bad data";
        String CURRENCIES_ALREADY_FETCHED = "Currencies already fetched";
        String DOCUMENT_GENERATION_FAILED_MISSING_TEMPLATE = "Document generation failed. Missing template file.";
        String DOCUMENT_GENERATION_FAILED_NOT_SUPPORTED_FILE_TYPE = "Document generation failed. Not supported file type";
    }
}
