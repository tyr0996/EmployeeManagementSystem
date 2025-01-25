package hu.martin.ems.exception;

public class FetchingCurrenciesException extends CurrencyException {
    public FetchingCurrenciesException() {
        super(CurrencyExceptionType.FETCHING_EXCEPTION);
    }
}
