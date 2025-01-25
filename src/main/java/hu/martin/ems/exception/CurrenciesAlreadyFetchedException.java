package hu.martin.ems.exception;

public class CurrenciesAlreadyFetchedException extends CurrencyException {
    public CurrenciesAlreadyFetchedException(){
        super(CurrencyExceptionType.ALREADY_FETCHED);
    }
}
