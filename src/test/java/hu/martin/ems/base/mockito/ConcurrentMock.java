package hu.martin.ems.base.mockito;

import org.mockito.MockSettings;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.internal.util.reflection.LenientCopyTool;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.InvocationContainer;
import org.mockito.invocation.MockHandler;
import org.mockito.mock.MockCreationSettings;
import org.mockito.plugins.MockMaker;

import java.util.Map;
import java.util.WeakHashMap;

import static org.mockito.Mockito.withSettings;
import static org.mockito.internal.handler.MockHandlerFactory.createMockHandler;
import static org.mockito.internal.progress.ThreadSafeMockingProgress.mockingProgress;

/**
* Create shared mocks that can be stubbed and verified in concurrent threads
 */
public class ConcurrentMock<T> implements MockHandler<T> {
    private static final MockMaker mockMaker = Plugins.getMockMaker();
    private static final ThreadLocal<Map<ConcurrentMock, MockHandler>> handlerMap = ThreadLocal.withInitial(WeakHashMap::new);
    private MockCreationSettings<T> settings;

    private ConcurrentMock(MockCreationSettings<T> settings){
        this.settings = settings;
    }

    public Object handle(Invocation invocation) throws Throwable {
        return getHandler().handle(invocation);
    }

    public MockCreationSettings<T> getMockSettings() {
        return settings;
    }

    public InvocationContainer getInvocationContainer() {
        return getHandler().getInvocationContainer();
    }

    public static <T> T mock(Class<T> typeToMock){
        return mock(typeToMock, withSettings());
    }

    /**
     * Create a shared concurrent mock
     * @param typeToMock
     * @param settings
     * @return
     * @param <T>
     */
    public static <T> T mock(Class<T> typeToMock, MockSettings settings){
        MockCreationSettings<T> creationSettings = settings.build(typeToMock);
        MockHandler mockHandler = new ConcurrentMock<>(creationSettings);
        T mock = mockMaker.createMock(creationSettings, mockHandler);
        Object spiedInstance = creationSettings.getSpiedInstance();
        if (spiedInstance != null){
            new LenientCopyTool().copyToMock(spiedInstance, mock);
        }

        mockingProgress().mockingStarted(mock, creationSettings);
        return mock;
    }

    /**
     * Reset everything for the current thread in case
     * this is a recycled thread from a thread pool
     */
    public static void threadReset() {
        mockingProgress().clearListeners();
        mockingProgress().reset();
        Map<ConcurrentMock, MockHandler> hm = handlerMap.get();
        hm.clear();
    }

    private MockHandler getHandler() {
        Map<ConcurrentMock, MockHandler> hm = handlerMap.get();
        MockHandler h = hm.get(this);
        if(h == null){
            h = createMockHandler(settings);
            hm.put(this, h);
        }
        return h;
    }
}
