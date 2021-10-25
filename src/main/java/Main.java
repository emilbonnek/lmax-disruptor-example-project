import Events.LongEvent;
import Events.LongEventProducer;
import Handlers.LocalLogHandler;
import Handlers.OnlineLogHandler;
import Handlers.PrintToSTDOutHandler;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Main {
    public static void main(String[] args) throws Exception {
        // Empty log file ahead of execution
        clearFile("log.txt");

        // Setup of disruptor
        int bufferSize = 1024;
        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize, DaemonThreadFactory.INSTANCE);
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        // Stage one - Two parallel threads for logging
        SequenceBarrier sequenceBarrier = ringBuffer.newBarrier(); // sB to ring buffer
        LocalLogHandler localLogHandler = new LocalLogHandler("log.txt");
        EventProcessor batchEventProcessorForLocalLogHandler = new BatchEventProcessor<>(ringBuffer, sequenceBarrier, localLogHandler);
        OnlineLogHandler onlineLogHandler = new OnlineLogHandler("http://localhost:8080");
        EventProcessor batchEventProcessorForOnlineLogHandler = new BatchEventProcessor<>(ringBuffer, sequenceBarrier, onlineLogHandler);

        // Stage two - A single thread that prints out to STD out
        SequenceBarrier sequenceBarrier2 = ringBuffer.newBarrier(
            batchEventProcessorForLocalLogHandler.getSequence(),
            batchEventProcessorForOnlineLogHandler.getSequence()
        ); // sB to log handlers
        PrintToSTDOutHandler printToSTDOutHandler = new PrintToSTDOutHandler();
        EventProcessor batchEventProcessorForPrintToSTDOutHandler = new BatchEventProcessor<>(ringBuffer, sequenceBarrier2, printToSTDOutHandler);

        // Prevent the producer from overrunning the consumers by restricting them to pass by the final stage's sequence.
        ringBuffer.addGatingSequences(
            batchEventProcessorForPrintToSTDOutHandler.getSequence()
        );
        disruptor.handleEventsWith(
                batchEventProcessorForLocalLogHandler,
                batchEventProcessorForOnlineLogHandler,
                batchEventProcessorForPrintToSTDOutHandler
        );
        disruptor.start();

        // Produce a new event every second with a consecutive number
        LongEventProducer producer = new LongEventProducer(ringBuffer);
        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long l = 0; true; l++) {
            bb.putLong(0, l);
            producer.onData(bb);
            Thread.sleep(1000);
        }
    }

    private static void clearFile(String localLogFile) {
        try {
            Files.write(
                Paths.get(localLogFile), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {}
    }
}