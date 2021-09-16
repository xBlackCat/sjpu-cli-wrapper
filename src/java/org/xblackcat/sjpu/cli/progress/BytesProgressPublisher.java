package org.xblackcat.sjpu.cli.progress;

import java.util.function.Consumer;

public class BytesProgressPublisher extends AProgressPublisher {
    public BytesProgressPublisher(Consumer<String> output) {
        super(output);
    }

    @Override
    public boolean publish(long current) {
        output.accept(String.format("\rProcessed %d bytes", current));
        return true;
    }
}
