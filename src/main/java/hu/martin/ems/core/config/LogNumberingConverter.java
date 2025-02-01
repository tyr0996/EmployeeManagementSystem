package hu.martin.ems.core.config;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.concurrent.atomic.AtomicInteger;

public class LogNumberingConverter extends ClassicConverter {

    private static AtomicInteger counter = new AtomicInteger(1);

    @Override
    public String convert(ILoggingEvent iLoggingEvent) {
        return counter.getAndIncrement() + ": ";
    }

    public static void resetCounter(){
        counter.set(1);
    }

}
