package org.xblackcat.sjpu.cli.progress;

public interface IMultiThreadProgressPublisher extends IProgressPublisher {
    IMultiThreadProgressPublisher NO_RENDERER = new IMultiThreadProgressPublisher() {
        @Override
        public IProgressPublisher newThread(String name, long total, IPublisherBuilder<? extends IProgressPublisher> builder) {
            return this;
        }

        @Override
        public boolean publish(long current) {
            return false;
        }

        @Override
        public void done() {
        }

        @Override
        public boolean isDone() {
            return false;
        }
    };

    IProgressPublisher newThread(String name, long total, IPublisherBuilder<? extends IProgressPublisher> builder);
}
