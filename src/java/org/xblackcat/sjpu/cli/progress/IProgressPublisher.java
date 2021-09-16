package org.xblackcat.sjpu.cli.progress;

public interface IProgressPublisher {
    IProgressPublisher NO_RENDERER = new IProgressPublisher() {
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

    /**
     * Publish and render a new value for progress
     *
     * @param current new progress value
     * @return true if progress string has changed
     */
    boolean publish(long current);

    boolean isDone();

    void done();
}
