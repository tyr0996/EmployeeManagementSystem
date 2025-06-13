package hu.martin.ems.base.mockito;

import hu.martin.ems.vaadin.api.base.WebClientProvider;
import lombok.experimental.UtilityClass;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;

@UtilityClass
public class MockingUtil {
    public void mockDatabaseNotAvailableOnlyOnce(DataSource spyDataSource, Integer preSuccess) throws SQLException {
        mockDatabaseNotAvailableWhen(spyDataSource, Arrays.asList(preSuccess));
    }

    public void mockDatabaseNotAvailableWhen(DataSource spyDataSource, List<Integer> failedCallIndexes) throws SQLException {
        AtomicInteger callCount = new AtomicInteger(0);

        doAnswer(invocation -> {
            int currentCall = callCount.incrementAndGet();
            if (failedCallIndexes.contains(currentCall - 1)) {
                throw new SQLException("Connection refused: getsockopt [Mocking exception]");
            } else {
                return invocation.callRealMethod();
            }
        }).when(spyDataSource).getConnection();
    }

    /**
     * This method is useful when you want to mock a REST call, which doesn't have @Controller or @RestController for it.
     * Use this if you create the WebClient with WebClientProvider.initBaseUrlWebClient.
     * For example for Actuator endpoints.
     */
    public <T, R> void mockBaseUrlWebClientResponse(WebClientProvider spyWebClientProvider,
                                                    String uri,
                                                    T mockResponse,
                                                    Class<T> responseType) {
        WebClient mockWebClient = prepareWebClientResponseMock(uri, mockResponse, responseType);
        doReturn(mockWebClient).when(spyWebClientProvider).initBaseUrlWebClient();
    }

    /**
     * This method is useful when you want to mock a REST call to throw an exception, which doesn't have @Controller
     * or @RestController for it. Use this if you create the WebClient with WebClientProvider.initBaseUrlWebClient.
     * For example for Actuator endpoints.
     */
    public <T, R> void mockBaseUrlWebClientException(WebClientProvider spyWebClientProvider,
                                                    String uri,
                                                    Throwable exception,
                                                    Class<T> responseType) {
        WebClient mockWebClient = prepareWebClientErrorMock(uri, responseType, exception);
        doReturn(mockWebClient).when(spyWebClientProvider).initBaseUrlWebClient();
    }

    /**
     * This method is useful when you want to mock a REST call, which doesn't have @Controller or @RestController for it.
     * Use this if you create the WebClient with WebClientProvider.initBaseUrlWebClient.
     * For example for Actuator endpoints.
     */
    public <T, R> void mockWebClientResponse(WebClientProvider spyWebClientProvider,
                                             String uri,
                                             T mockResponse,
                                             Class<T> responseType,
                                             String entityName) {
        WebClient mockWebClient = prepareWebClientResponseMock(uri, mockResponse, responseType);
        doReturn(mockWebClient).when(spyWebClientProvider).initWebClient(entityName);
    }

    /**
     * This method is useful when you want to mock a REST call, which doesn't have @Controller or @RestController for it.
     * Use this if you create the WebClient with WebClientProvider.initCsrfWebClient.
     * For example for Actuator endpoints.
     */
    public <T, R> void mockCsrfUrlWebClientResponse(WebClientProvider spyWebClientProvider,
                                                    String uri,
                                                    T mockResponse,
                                                    Class<T> responseType,
                                                    String entityName) {
        WebClient mockWebClient = prepareWebClientResponseMock(uri, mockResponse, responseType);
        doReturn(mockWebClient).when(spyWebClientProvider).initCsrfWebClient(entityName);
    }


    private <T> WebClient prepareWebClientResponseMock(String uri, T mockResponse, Class<T> responseType) {
        WebClient mockWebClient = spy(WebClient.class);
        WebClient.RequestHeadersUriSpec uriSpec = spy(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = spy(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = spy(WebClient.ResponseSpec.class);

        when(mockWebClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(uri)).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(responseType)).thenReturn(Mono.just(mockResponse));

        return mockWebClient;
    }

    private <T> WebClient prepareWebClientErrorMock(String requestUri, Class<T> responseType, Throwable exceptionToThrow) {
        WebClient mockWebClient = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(mockWebClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(eq(requestUri))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(eq(responseType))).thenReturn(Mono.error(exceptionToThrow));

        return mockWebClient;
    }
}
