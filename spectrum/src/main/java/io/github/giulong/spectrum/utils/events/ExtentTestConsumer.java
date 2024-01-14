package io.github.giulong.spectrum.utils.events;

import com.aventstack.extentreports.Status;
import com.fasterxml.jackson.annotation.JsonView;
import io.github.giulong.spectrum.internals.jackson.views.Views.Internal;
import io.github.giulong.spectrum.pojos.events.Event;
import io.github.giulong.spectrum.utils.ExtentReporter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@JsonView(Internal.class)
public class ExtentTestConsumer extends EventsConsumer {

    private final ExtentReporter extentReporter = ExtentReporter.getInstance();

    public void consumes(final Event event) {
        final Status status = event.getResult().getStatus();

        extentReporter.logTestEnd(event.getContext(), status);
        log.info(String.format("END execution of '%s -> %s': %s", event.getPrimaryId(), event.getSecondaryId(), status.name()));
    }
}
