package org.xblackcat.sjpu.cli.reader;

public class StdInSource extends ASource {
    public StdInSource() {
        super(System.in);
    }

    @Override
    public Long getSize() {
        return null;
    }
}
