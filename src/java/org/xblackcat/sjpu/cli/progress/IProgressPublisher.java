package org.xblackcat.sjpu.cli.progress;

public interface IProgressPublisher {
    IProgressPublisher NO_RENDERER = new IProgressPublisher() {
        @Override
        public void publish(long current) {
        }

        @Override
        public void done() {
        }
    };

    void publish(long current);

    void done();
}
