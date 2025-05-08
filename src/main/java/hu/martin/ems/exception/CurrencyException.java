package hu.martin.ems.exception;

import hu.martin.ems.core.model.EmsResponse;
import lombok.Getter;

public abstract class CurrencyException extends Exception {

    @Getter
    private CurrencyExceptionType type;


    public enum CurrencyExceptionType {
        FETCHING_EXCEPTION(EmsResponse.Description.FETCHING_CURRENCIES_FAILED),
        PARSING_EXCEPTION(EmsResponse.Description.PARSING_CURRENCIES_FAILED);

        @Getter
        private String text;

        CurrencyExceptionType(String text) {
            this.text = text;
        }
    }

    public CurrencyException(CurrencyExceptionType type) {
        super(switch (type) {
            case FETCHING_EXCEPTION -> EmsResponse.Description.FETCHING_CURRENCIES_FAILED;
            case PARSING_EXCEPTION -> EmsResponse.Description.PARSING_CURRENCIES_FAILED;
        });
        this.type = type;
    }
}
