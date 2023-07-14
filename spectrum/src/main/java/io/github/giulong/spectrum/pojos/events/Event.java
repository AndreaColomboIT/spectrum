package io.github.giulong.spectrum.pojos.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.giulong.spectrum.enums.EventTag;
import io.github.giulong.spectrum.enums.Result;
import lombok.*;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class Event {

    private String primaryId;
    private String secondaryId;
    private Set<EventTag> tags;
    private String reason;
    private Result result;

    @JsonIgnore
    private ExtensionContext context;
}
