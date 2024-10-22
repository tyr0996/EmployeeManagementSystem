package hu.martin.ems.core.model;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class EmsResponse {
    private int code;
    private Object responseData;
    private String description;

    public EmsResponse(int code, String description){
        this(code, null, description);
    }

//    @Override
//    public String toString(){
//        if(responseData == null){
//            return "{\"code\":" + code + ",\"description\":\"" + description + "\"}";
//        }
//        else{
//            try{
//                return "{\"code\":" + code + ",\"description\":\"" + description + "\",\"responseData\":\"" + new ObjectMapper().writeValueAsString(responseData) + "}";
//            }
//            catch (JsonProcessingException e){
//                return "Error when printing out the responseData from EmsResponse";
//            }
//        }
//    }

    public interface Description {
        String SFTP_SENDING_ERROR = "Error happened when sending with SFTP";
        String SFTP_SENDING_SUCCESS = "SFTP sending is done";
        String CLEAR_DATABASE_SUCCESS = "Clearing database was successful";
        String CLEAR_DATABASE_FAILED = "Clearing database failed for one or more table";
        String FETCHING_CURRENCIES_FAILED = "Error happened while fetching exchange rates";
        String PARSING_CURRENCIES_FAILED = "Currencies fetched successfully, but the currency server sent bad data";
    }
}
