package org.xblackcat.sjpu.cli.progress;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class StepByStepProgressPublisher extends AProgressPublisher implements IMultiThreadProgressPublisher {
    private final Lock lock = new ReentrantLock();
    private IProgressPublisher currentPublisher;
    private String currentName;
    private String currentProgress;

    private int step;
    private int totalSteps;

    public StepByStepProgressPublisher(Consumer<String> output) {
        this(output, 0);
    }

    public StepByStepProgressPublisher(Consumer<String> output, int totalSteps) {
        super(output);
        this.totalSteps = totalSteps;
    }

    @Override
    public IProgressPublisher newThread(String name, long total, IPublisherBuilder<? extends IProgressPublisher> builder) {
        final Consumer<String> consumer = s -> {
            lock.lock();
            try {
                currentProgress = s;
            } finally {
                lock.unlock();
            }

            publishAll();
        };
        final IProgressPublisher publisher = builder.apply(consumer, total);

        lock.lock();
        try {
            if (currentPublisher != null && !currentPublisher.isDone()) {
                throw new IllegalStateException("Previous publisher " + currentName + " is not finished!");
            }
            step++;
            currentPublisher = publisher;
            currentName = name;
        } finally {
            lock.unlock();
        }

        return new IProgressPublisher() {
            @Override
            public boolean publish(long current) {
                if (publisher.publish(current)) {
                    publishAll();
                    return true;
                }
                return false;
            }

            @Override
            public void done() {
                publisher.done();
                publishAll();
            }

            @Override
            public boolean isDone() {
                return publisher.isDone();
            }
        };
    }

    @Override
    public boolean publish(long current) {
        publishAll();
        return true;
    }

    private void publishAll() {
        final StringBuilder progress = new StringBuilder("\rStep: ");
        lock.lock();
        try {
            progress.append(step);
            if (totalSteps > 0) {
                progress.append(" of ");
                progress.append(totalSteps);
            }
            if (currentName != null) {
                progress.append(": ");
                progress.append(currentName);
                progress.append(": ");
                progress.append(currentProgress, 1, currentProgress.length());
            }
            progress.append("\t\t\t\t\t\t\t");
        } finally {
            lock.unlock();
        }

        output.accept(progress.toString());
    }

    @Override
    public void done() {
        super.done();
    }
}
