package io.github.giulong.spectrum.utils.events;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.giulong.spectrum.pojos.events.Event;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Consumer;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.WRAPPER_OBJECT;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;
import static java.util.stream.Collectors.toSet;

@JsonTypeInfo(use = NAME, include = WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SlackConsumer.class, name = "slack"),
        @JsonSubTypes.Type(value = TestBookConsumer.class, name = "testbook"),
        @JsonSubTypes.Type(value = ExtentTestConsumer.class, name = "extentTest"),
        @JsonSubTypes.Type(value = DriverConsumer.class, name = "driver"),
        @JsonSubTypes.Type(value = MailConsumer.class, name = "mail"),
        @JsonSubTypes.Type(value = VideoConsumer.class, name = "video"),
})
@Getter
@Slf4j
public abstract class EventsConsumer implements Consumer<Event> {

    @JsonPropertyDescription("List of events that will be consumed")
    protected List<Event> events;

    protected boolean tagsIntersect(final Event e1, final Event e2) {
        final boolean matches = e1.getTags() != null && e2.getTags() != null &&
                !e1
                        .getTags()
                        .stream()
                        .filter(e2.getTags()::contains)
                        .collect(toSet())
                        .isEmpty();

        log.trace("tagsIntersect: {}", matches);
        return matches;
    }

    protected boolean primaryAndSecondaryIdMatch(final Event e1, final Event e2) {
        final boolean matches = e1.getPrimaryId() != null && e2.getPrimaryId() != null && e1.getPrimaryId().matches(e2.getPrimaryId()) &&
                e1.getSecondaryId() != null && e2.getSecondaryId() != null && e1.getSecondaryId().matches(e2.getSecondaryId());

        log.trace("primaryAndSecondaryIdMatch: {}", matches);
        return matches;
    }

    protected boolean justPrimaryIdMatches(final Event e1, final Event e2) {
        final boolean matches = e1.getPrimaryId() != null && e2.getPrimaryId() != null && e1.getPrimaryId().matches(e2.getPrimaryId())
                && e1.getSecondaryId() == null;

        log.trace("justPrimaryIdMatches: {}", matches);
        return matches;
    }

    protected boolean reasonMatches(final Event e1, final Event e2) {
        final boolean matches = e1.getReason() != null && e2.getReason() != null && e1.getReason().matches(e2.getReason());

        log.trace("reasonMatches: {}", matches);
        return matches;
    }

    protected boolean resultMatches(final Event e1, final Event e2) {
        final boolean matches = e1.getResult() != null && e1.getResult().equals(e2.getResult());

        log.trace("resultMatches: {}", matches);
        return matches;
    }

    protected boolean findMatchFor(Event e1, Event e2) {
        return (reasonMatches(e1, e2) || resultMatches(e1, e2)) &&
                (primaryAndSecondaryIdMatch(e1, e2) || justPrimaryIdMatches(e1, e2) || tagsIntersect(e1, e2));
    }

    public void match(final Event event) {
        events
                .stream()
                .peek(h -> log.trace("{} matchers for {}", getClass().getSimpleName(), event))
                .filter(h -> findMatchFor(event, h))
                .peek(h -> log.debug("{} is consuming {}", getClass().getSimpleName(), event))
                .forEach(h -> acceptSilently(event));
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    protected void acceptSilently(final Event event) {
        try {
            accept(event);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
