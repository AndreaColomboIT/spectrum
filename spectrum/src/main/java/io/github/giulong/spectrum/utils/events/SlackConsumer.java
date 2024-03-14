package io.github.giulong.spectrum.utils.events;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.slack.api.Slack;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import io.github.giulong.spectrum.pojos.events.Event;
import io.github.giulong.spectrum.utils.FileUtils;
import io.github.giulong.spectrum.utils.FreeMarkerWrapper;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Map;

@Getter
public class SlackConsumer extends EventsConsumer {

    private static final FileUtils FILE_UTILS = FileUtils.getInstance();

    private static final FreeMarkerWrapper FREE_MARKER_WRAPPER = FreeMarkerWrapper.getInstance();

    @JsonPropertyDescription("Template to be used when creating the message")
    private final String template = "slack.json";

    @JsonPropertyDescription("Target channel where to send the message")
    protected String channel;

    @JsonPropertyDescription("Bot User OAuth Token")
    protected String token;

    @Override
    @SneakyThrows
    public void accept(final Event event) {
        final Map<String, Object> vars = Map.of("event", event);
        final String interpolatedTemplate = FREE_MARKER_WRAPPER.interpolate(FILE_UTILS.readTemplate(template), vars);

        Slack
                .getInstance()
                .methods(token)
                .chatPostMessage(ChatPostMessageRequest.builder()
                        .channel(channel)
                        .text("Spectrum notification")
                        .blocksAsString(interpolatedTemplate)
                        .build());
    }
}
