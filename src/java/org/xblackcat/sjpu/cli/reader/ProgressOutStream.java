package org.xblackcat.sjpu.cli.reader;

import org.xblackcat.sjpu.cli.progress.NullPrintStream;

import java.io.PrintStream;
import java.util.Arrays;

public enum ProgressOutStream {
    None(new NullPrintStream()),
    StdOut(System.out),
    StdErr(System.err),
//    ----
    ;

    public static final ProgressOutStream Default = StdErr;
    public static final String TARGET_LIST = Arrays.asList(values()).toString();
    private final PrintStream stream;

    ProgressOutStream(PrintStream stream) {
        this.stream = stream;
    }

    public static ProgressOutStream parseValue(String value) {
        return Arrays.stream(values()).filter(sd -> sd.name().equalsIgnoreCase(value)).findFirst().orElse(null);
    }

    public PrintStream getStream() {
        return stream;
    }
}
