package hu.martin.ems.vaadin.core;

import hu.martin.ems.exception.FetchingCurrenciesException;
import hu.martin.ems.exception.ParsingCurrenciesException;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.GenericJDBCException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<EmsError> handleAllExceptions(Exception ex, HttpServletRequest request) {
        EmsError emsError = new EmsError(Instant.now().toEpochMilli(), 500, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(emsError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({TransactionException.class, JpaSystemException.class, GenericJDBCException.class})
    public final ResponseEntity<EmsError> handleSqlException(Exception ex, HttpServletRequest request) {
        EmsError emsError = new EmsError(Instant.now().toEpochMilli(), 500, "Database error", request.getRequestURI());
        return new ResponseEntity<>(emsError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FetchingCurrenciesException.class)
    public ResponseEntity<EmsError> handleFetchingCurrenciesException(FetchingCurrenciesException ex, HttpServletRequest request) {
        EmsError emsErrorResponse = new EmsError(Instant.now().toEpochMilli(), 502, ex.getType().getText(), request.getRequestURI());
        return new ResponseEntity<>(emsErrorResponse, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(ParsingCurrenciesException.class)
    public ResponseEntity<EmsError> handleParsingCurrenciesException(ParsingCurrenciesException ex, HttpServletRequest request) {
        EmsError emsErrorResponse = new EmsError(Instant.now().toEpochMilli(), 500, ex.getType().getText(), request.getRequestURI());
        return new ResponseEntity<>(emsErrorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
