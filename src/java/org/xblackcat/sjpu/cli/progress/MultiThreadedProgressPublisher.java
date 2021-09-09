package org.xblackcat.sjpu.cli.progress;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class MultiThreadedProgressPublisher extends AProgressPublisher implements IMultiThreadProgressPublisher {
    private final Map<String, String> publishedInfos = new LinkedHashMap<>();
    private final Lock lock = new ReentrantLock();

    public MultiThreadedProgressPublisher(Consumer<String> output) {
        super(output);
    }

    @Override
    public IProgressPublisher newThread(String name, long total, IPublisherBuilder<? extends IProgressPublisher> builder) {
        final Consumer<String> consumer = s -> {
            lock.lock();
            try {
                publishedInfos.put(name, s);
            } finally {
                lock.unlock();
            }

            publishAll();
        };
        final IProgressPublisher publisher = builder.apply(consumer, total);
        return new IProgressPublisher() {
            @Override
            public void publish(long current) {
                publisher.publish(current);
                publishAll();
            }

            @Override
            public void done() {
                lock.lock();
                try {
                    publishedInfos.remove(name);
                } finally {
                    lock.unlock();
                }

                publishAll();
            }
        };
    }

    @Override
    public void publish(long current) {
        publishAll();
    }

    private void publishAll() {
        final StringBuilder progress = new StringBuilder("\rTotal: ");
        lock.lock();
        try {
            progress.append(publishedInfos.size());
            progress.append(": [ ");
            for (Map.Entry<String, String> e : publishedInfos.entrySet()) {
                progress.append(e.getKey());
                progress.append(": ");
                progress.append(e.getValue(), 1, e.getValue().length());
                progress.append("; ");
            }
            progress.append("]");
        } finally {
            lock.unlock();
        }

        output.accept(progress.toString());
    }

    @Override
    public void done() {
        publishedInfos.clear();
        super.done();
    }
}
