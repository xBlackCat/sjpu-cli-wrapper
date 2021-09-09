package org.xblackcat.sjpu.cli.progress;

import java.io.PrintStream;

public class LogPrintStream {
    private final Verbosity verbosity;
    private final PrintStream printStream;

    public LogPrintStream(Verbosity verbosity, PrintStream printStream) {
        this.verbosity = verbosity;
        this.printStream = printStream;
    }

    private void log(Verbosity v, String str) {
        if (v.ordinal() <= verbosity.ordinal()) {
            printStream.print(str);
        }
    }

    private void logln(Verbosity v, String str) {
        if (v.ordinal() <= verbosity.ordinal()) {
            printStream.println(str);
        }
    }

    public void brief(String str) {
        log(Verbosity.Brief, str);
    }

    public void briefln(String str) {
        logln(Verbosity.Brief, str);
    }

    public void normal(String str) {
        log(Verbosity.Normal, str);
    }

    public void normalln(String str) {
        logln(Verbosity.Normal, str);
    }

    public void verbose(String str) {
        log(Verbosity.Verbose, str);
    }

    public void verboseln(String str) {
        logln(Verbosity.Verbose, str);
    }

    public void print(String str) {
        normal(str);
    }

    public void println(String str) {
        normalln(str);
    }
}
