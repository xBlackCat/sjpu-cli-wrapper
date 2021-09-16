package org.xblackcat.sjpu.cli.progress;

import java.util.function.Consumer;

public class PercentProgressPublisher extends AProgressPublisher {
    private final long total;
    private final long permille;
    private final String formatString;
    private long lastThreshold = -1;

    public PercentProgressPublisher(Consumer<String> output, long total) {
        this(output, total, "Processed %#2.1f%%");
    }

    /**
     * Build Percent progress builder with custom progress string
     *
     * @param output
     * @param total
     * @param formatString formatted progress string. Can accept two arguments: 1. double(percentage value) and 2. long (total elements amount).
     *                     See {@link java.util.Locale.Category#FORMAT FORMAT} fpr details
     */
    public PercentProgressPublisher(Consumer<String> output, long total, String formatString) {
        super(output);
        this.total = total;
        permille = total / 1000L;
        this.formatString = "\r" + formatString;
    }

    @Override
    public boolean publish(long current) {
        if (lastThreshold < 0 || current < lastThreshold || current > lastThreshold + permille) {
            lastThreshold = (current / permille) * permille;
            output.accept(String.format(formatString, ((double) current / total) * 100., total));
            return true;
        }
        return false;
    }
}
