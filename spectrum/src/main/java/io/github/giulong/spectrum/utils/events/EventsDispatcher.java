package io.github.giulong.spectrum.utils.events;

import io.github.giulong.spectrum.enums.Result;
import io.github.giulong.spectrum.interfaces.SessionHook;
import io.github.giulong.spectrum.utils.Configuration;
import io.github.giulong.spectrum.pojos.events.Event;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public class EventsDispatcher implements SessionHook {

    private static final EventsDispatcher INSTANCE = new EventsDispatcher();

    public static final String BEFORE = "before";
    public static final String AFTER = "after";
    public static final String TEST = "test";
    public static final String CLASS = "class";
    public static final String SUITE = "suite";

    private final Configuration configuration = Configuration.getInstance();

    public static EventsDispatcher getInstance() {
        return INSTANCE;
    }

    @Override
    public void sessionOpened() {
        log.debug("Session opened hook");
        fire(BEFORE, Set.of(SUITE));
    }

    @Override
    public void sessionClosed() {
        log.debug("Session closed hook");
        fire(AFTER, Set.of(SUITE), configuration.getSummary().toResult());
    }

    public void fire(final String reason, final Set<String> tags) {
        fire(null, null, reason, null, tags, null);
    }

    public void fire(final String reason, final Set<String> tags, final Result result) {
        fire(null, null, reason, result, tags, null);
    }

    public void fire(final String primaryId, final String reason) {
        fire(primaryId, null, reason, null, null, null);
    }

    public void fire(final String primaryId, final String secondaryId, final String reason) {
        fire(primaryId, secondaryId, reason, null, null, null);
    }

    public void fire(final String primaryId, final String secondaryId, final String reason, final Result result, final Set<String> tags, final ExtensionContext context) {
        final Event event = Event.builder()
                .primaryId(primaryId)
                .secondaryId(secondaryId)
                .reason(reason)
                .result(result)
                .tags(tags)
                .context(context)
                .build();

        log.debug("Dispatching event {}", event);
        configuration.getEventsConsumers().forEach(consumer -> consumer.match(event));
    }
}
