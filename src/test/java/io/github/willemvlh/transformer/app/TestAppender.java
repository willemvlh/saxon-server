package io.github.willemvlh.transformer.app;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.ArrayList;

public class TestAppender extends AppenderBase<ILoggingEvent> {
    private final ArrayList<ILoggingEvent> events = new ArrayList<>();

    public ArrayList<ILoggingEvent> getEvents() {
        return events;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        events.add(eventObject);
    }
}
