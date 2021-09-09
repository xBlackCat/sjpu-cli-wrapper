package org.xblackcat.sjpu.cli.reader;

import java.io.BufferedReader;

public interface ISource {
    Long getSize();

    BufferedReader getReader();

    long getBytesRead();
}
