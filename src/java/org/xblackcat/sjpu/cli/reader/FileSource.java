package org.xblackcat.sjpu.cli.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class FileSource extends ASource {
    private long length;

    public FileSource(File file) throws FileNotFoundException {
        super(new FileInputStream(file));
        length = file.length();
    }

    @Override
    public Long getSize() {
        return length;
    }

}
