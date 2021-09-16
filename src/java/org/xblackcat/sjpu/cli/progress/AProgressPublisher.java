package org.xblackcat.sjpu.cli.progress;

import java.util.function.Consumer;

public abstract class AProgressPublisher implements IProgressPublisher {
    protected final Consumer<String> output;
    private boolean done = false;

    protected AProgressPublisher(Consumer<String> output) {
        this.output = output;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public void done() {
        done = true;
        output.accept("\n");
    }
}
