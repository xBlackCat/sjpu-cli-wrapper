package org.xblackcat.sjpu.cli;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

public class OptionUtilsTest {
    @Test
    public void testParseSize() throws Exception {
        Assert.assertEquals(10, OptionUtils.parseSize("10"));
        Assert.assertEquals(1024, OptionUtils.parseSize("1K"));
        Assert.assertEquals(1024, OptionUtils.parseSize("1k"));
        Assert.assertEquals(1024, OptionUtils.parseSize("1024"));
        Assert.assertEquals(1048576, OptionUtils.parseSize("1m"));
        Assert.assertEquals(1048576, OptionUtils.parseSize("1M"));
        Assert.assertEquals(1073741824, OptionUtils.parseSize("1073741824"));
        Assert.assertEquals(1073741824, OptionUtils.parseSize("1G"));
        Assert.assertEquals(1073741824, OptionUtils.parseSize("1g"));
        Assert.assertEquals(1099511627776L, OptionUtils.parseSize("1024g"));
        Assert.assertEquals(1099511627776L, OptionUtils.parseSize("1T"));
        Assert.assertEquals(1099511627776L, OptionUtils.parseSize("1t"));
    }

    @Test
    public void testParseDuration() {
        Duration.parse("P0D");
    }
}
