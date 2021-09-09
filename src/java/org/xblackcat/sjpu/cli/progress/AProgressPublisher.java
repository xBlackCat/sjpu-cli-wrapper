package org.xblackcat.sjpu.cli.progress;

import java.util.function.Consumer;

public abstract class AProgressPublisher implements IProgressPublisher {
    protected final Consumer<String> output;

    protected AProgressPublisher(Consumer<String> output) {
        this.output = output;
    }

    @Override
    public void done() {
        output.accept("\n");
    }
}
