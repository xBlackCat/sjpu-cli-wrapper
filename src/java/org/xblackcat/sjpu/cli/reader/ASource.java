package org.xblackcat.sjpu.cli.reader;

import java.io.*;
import java.nio.charset.StandardCharsets;

public abstract class ASource implements ISource {
    private final CounterInputStream stream;

    protected ASource(InputStream in) {
        stream = new CounterInputStream(new BufferedInputStream(in, 1 << 25));
    }

    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    @Override
    public long getBytesRead() {
        return stream.getBytesRead();
    }

    private static class CounterInputStream extends InputStream {
        private final InputStream in;
        private long bytesRead = 0;

        public CounterInputStream(InputStream in) {
            this.in = in;
        }

        @Override
        public int read() throws IOException {
            int read = in.read();
            if (read != -1) {
                bytesRead++;
            }
            return read;
        }

        public long getBytesRead() {
            return bytesRead;
        }
    }
}
