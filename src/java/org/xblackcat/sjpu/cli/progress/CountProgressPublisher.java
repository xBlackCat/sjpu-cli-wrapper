package org.xblackcat.sjpu.cli.progress;

import java.util.function.Consumer;

public class CountProgressPublisher extends AProgressPublisher {
    private final long total;

    public CountProgressPublisher(Consumer<String> output, long total) {
        super(output);
        this.total = total;
    }

    @Override
    public boolean publish(long current) {
        output.accept(String.format("\rProcessed %d of %d", current, total));
        return true;
    }
}
