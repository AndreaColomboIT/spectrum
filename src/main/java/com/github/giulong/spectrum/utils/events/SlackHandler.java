package com.github.giulong.spectrum.utils.events;

import com.github.giulong.spectrum.pojos.events.Event;
import com.github.giulong.spectrum.utils.FileUtils;
import com.github.giulong.spectrum.utils.FreeMarkerWrapper;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import lombok.Getter;

import java.io.IOException;
import java.util.Map;

@Getter
public class SlackHandler extends EventHandler {

    private static final FileUtils FILE_UTILS = FileUtils.getInstance();

    private static final FreeMarkerWrapper FREE_MARKER_WRAPPER = FreeMarkerWrapper.getInstance();

    @SuppressWarnings("FieldMayBeFinal")
    private String template = "/templates/slack.json";

    protected String channel;

    protected String token;

    public void handle(final Event event) throws SlackApiException, IOException {
        final Map<String, Object> vars = Map.of("event", event);
        final String interpolatedTemplate = FREE_MARKER_WRAPPER.interpolate("slack", FILE_UTILS.read(template), vars);

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
