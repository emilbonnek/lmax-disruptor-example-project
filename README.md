# lmax-disruptor-example-project
This is a small example application, demonstrating how to have a multiple threads communicating with the disruptor ring buffer. It has a producer that produces consecutive long values every second and it writes them to a ring buffer. The ringbuffer has a sequencenumber that is being monitored by two consumers to perform logging. These consumers have their own sequencenumbers that are being monitored by a consumer that writes it the number out to stdout.

## Sketch of application structure
![Sketch of the application structure](/sketch.png)
