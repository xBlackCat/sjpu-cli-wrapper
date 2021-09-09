package org.xblackcat.sjpu.cli.progress;

import java.util.function.Consumer;

public class PercentProgressPublisher extends AProgressPublisher {
    private final long total;

    private long lastThreshold = -1;
    private final long permille;

    public PercentProgressPublisher(Consumer<String> output, long total) {
        super(output);
        this.total = total;
        permille = total / 1000L;
    }

    @Override
    public void publish(long current) {
        if (lastThreshold < 0 || current < lastThreshold || current > lastThreshold + permille) {
            lastThreshold = (current / permille) * permille;
            output.accept(String.format("\rProcessed %#2.1f%% of %d bytes", ((double) current / total) * 100., total));
        }
    }
}
