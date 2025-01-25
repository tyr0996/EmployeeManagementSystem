package hu.martin.ems.exception;

public class ParsingCurrenciesException extends CurrencyException {
    public ParsingCurrenciesException(){
        super(CurrencyExceptionType.PARSING_EXCEPTION);
    }
}
