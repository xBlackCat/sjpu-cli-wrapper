package org.xblackcat.sjpu.cli.progress;

public interface IMultiThreadProgressPublisher extends IProgressPublisher {
    IMultiThreadProgressPublisher NO_RENDERER = new IMultiThreadProgressPublisher() {
        @Override
        public IProgressPublisher newThread(String name, long total, IPublisherBuilder<? extends IProgressPublisher> builder) {
            return this;
        }

        @Override
        public void publish(long current) {
        }

        @Override
        public void done() {
        }
    };

    IProgressPublisher newThread(String name, long total, IPublisherBuilder<? extends IProgressPublisher> builder);
}
