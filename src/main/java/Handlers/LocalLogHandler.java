package Handlers;

import Events.LongEvent;
import com.lmax.disruptor.EventHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class LocalLogHandler implements EventHandler<LongEvent> {
    private final String localLogFile;

    public LocalLogHandler(String localLogFile){
        super();
        this.localLogFile = localLogFile;
    }
    @Override
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch) {
        try {
            Files.write(
                    Paths.get(localLogFile), (Long.toString(event.get())+'\n').getBytes(), StandardOpenOption.APPEND
            );
        } catch (IOException e) {}
    }
}
