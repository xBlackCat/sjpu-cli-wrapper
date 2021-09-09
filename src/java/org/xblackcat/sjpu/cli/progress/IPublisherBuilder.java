package org.xblackcat.sjpu.cli.progress;

import java.util.function.Consumer;

@FunctionalInterface
public interface IPublisherBuilder<P extends IProgressPublisher> {
    P apply(Consumer<String> consumer, long total);
}
