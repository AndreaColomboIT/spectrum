package io.github.giulong.spectrum.internals;

import io.github.giulong.spectrum.utils.ReflectionUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.event.Level;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BrowserLog")
class BrowserLogTest {

    private static final String LOG_MESSAGE = "log message";

    private MockedConstruction<StringBuffer> stringBufferMockedConstruction;

    @Mock
    @SuppressWarnings("unused")
    private Level level;

    @InjectMocks
    private BrowserLog browserLog;

    @BeforeEach
    public void beforeEach() {
        stringBufferMockedConstruction = mockConstruction(StringBuffer.class);
        ReflectionUtils.setField("mem", browserLog, new StringBuffer(LOG_MESSAGE));
    }

    @AfterEach
    public void afterEach() {
        stringBufferMockedConstruction.close();
    }

    @Test
    @DisplayName("write should append the char provided if it's not a line break")
    public void write() {
        final char c = 'a';
        final List<StringBuffer> stringBuffers = stringBufferMockedConstruction.constructed();

        browserLog.write(c);
        verify(stringBuffers.getFirst()).append(c);
    }

    @Test
    @DisplayName("write should flush the buffer when the char provided is a line break")
    public void writeFlush() {
        final char c = '\n';
        final List<StringBuffer> stringBuffers = stringBufferMockedConstruction.constructed();

        browserLog.write(c);
        verifyNoInteractions(stringBuffers.getFirst());
        assertEquals(stringBuffers.get(1), ReflectionUtils.getFieldValue("mem", browserLog));
    }

    @Test
    @DisplayName("flush should write the buffer's content at the provided level and re-initialise it")
    public void flush() {
        final List<StringBuffer> stringBuffers = stringBufferMockedConstruction.constructed();
        assertEquals(stringBuffers.getFirst(), ReflectionUtils.getFieldValue("mem", browserLog));

        browserLog.flush();
        assertEquals(stringBuffers.get(1), ReflectionUtils.getFieldValue("mem", browserLog));
    }
}
