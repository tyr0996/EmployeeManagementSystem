package hu.martin.ems.base.mockito;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.doAnswer;

public final class MockingUtil {
    public static void mockDatabaseNotAvailableOnlyOnce(DataSource spyDataSource, Integer preSuccess) throws SQLException {
        mockDatabaseNotAvailableWhen(spyDataSource, Arrays.asList(preSuccess));
    }

    public static void mockDatabaseNotAvailableAfter(DataSource spyDataSource, int preSuccess) throws SQLException {
        AtomicInteger callCount = new AtomicInteger(0);

        doAnswer(invocation -> {
            int currentCall = callCount.incrementAndGet();
            if (currentCall <= preSuccess) {
                return invocation.callRealMethod();
            }
            else if (currentCall == preSuccess + 1) {
                throw new SQLException("Connection refused: getsockopt");
            }
            else {
                throw new SQLException("Connection refused: getsockopt");
            }
        }).when(spyDataSource).getConnection();
    }

    public static void mockDatabaseNotAvailableWhen(DataSource spyDataSource, List<Integer> failedCallIndexes) throws SQLException {
        AtomicInteger callCount = new AtomicInteger(0);

        doAnswer(invocation -> {
            int currentCall = callCount.incrementAndGet();
            if(failedCallIndexes.contains(currentCall - 1)) {
                throw new SQLException("Connection refused: getsockopt");
            }
            else {
                return invocation.callRealMethod();
            }
        }).when(spyDataSource).getConnection();
    }
}
