package Handlers;

import Events.LongEvent;
import com.lmax.disruptor.EventHandler;

public class PrintToSTDOutHandler implements EventHandler<LongEvent> {
    @Override
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch) {
        String toPrint = "(Handlers.PrintToSTDOutHandler) -- Value: "+ event.get();
        System.out.println(toPrint);
    }
}