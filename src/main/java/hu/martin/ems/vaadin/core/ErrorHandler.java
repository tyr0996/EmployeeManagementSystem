package hu.martin.ems.vaadin.core;

import org.hibernate.exception.GenericJDBCException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<EmsError> handleAllExceptions(Exception ex, WebRequest request) {
        String message = ex.getMessage() == null ? "Internal Server Error" : ex.getMessage();
        EmsError emsError = new EmsError(Instant.now().toEpochMilli(), 500, message, request.getContextPath());
        return new ResponseEntity<>(emsError, HttpStatus.INTERNAL_SERVER_ERROR);
    }



    @ExceptionHandler({TransactionException.class, JpaSystemException.class, GenericJDBCException.class})
    public final ResponseEntity<EmsError> handleSqlException(Exception ex, WebRequest request){
        EmsError emsError = new EmsError(Instant.now().toEpochMilli(), 500, "Database error", request.getContextPath());
        return new ResponseEntity<>(emsError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //TODO: megcsinálni az egyedi exception-ökre, és ne a kontrollerben legyenek ezek.
}
