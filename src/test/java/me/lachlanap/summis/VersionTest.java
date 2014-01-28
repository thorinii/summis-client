/*
 * The MIT License
 *
 * Copyright 2014 Lachlan Phillips.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.lachlanap.summis;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 *
 * @author Lachlan Phillips
 */
public class VersionTest {

    @Test
    public void parseSingle() {
        String parse = "3";

        Version v = Version.parse(parse);

        assertThat(v.getNumberCount(), is(1));
        assertThat(v.get(Version.MAJOR), is(3));

        assertThat(v.isDevRelease(), is(false));
    }

    @Test
    public void parseMultiple() {
        String parse = "1.2.3";

        Version v = Version.parse(parse);

        assertThat(v.getNumberCount(), is(3));
        assertThat(v.get(Version.MAJOR), is(1));
        assertThat(v.get(Version.MINOR), is(2));
        assertThat(v.get(Version.PATCH), is(3));

        assertThat(v.isDevRelease(), is(false));
    }

    @Test
    public void parseDevStatus() {
        String parse = "1.2-dev";

        Version v = Version.parse(parse);

        assertThat(v.isDevRelease(), is(true));
        assertThat(v.getDevStatus(), is("dev"));
    }

    @Test
    public void toStringIsSameAsOriginal() {
        String parse = "1.2-dev";
        Version v = Version.parse(parse);

        assertThat(v.toString(), is(parse));
    }

    @Test
    public void isGreaterThanMajor() {
        Version v1 = Version.parse("1.2");
        Version v2 = Version.parse("2.1");

        assertThat(v2.isGreaterThan(v1), is(true));
        assertThat(v1.isGreaterThan(v2), is(false));
    }

    @Test
    public void isGreaterThanMinor() {
        Version v1 = Version.parse("1.2");
        Version v2 = Version.parse("1.3");

        assertThat(v2.isGreaterThan(v1), is(true));
        assertThat(v1.isGreaterThan(v2), is(false));
    }

    @Test
    public void isGreaterThanDifferentLengthVersion() {
        Version v1 = Version.parse("1.4.2");
        Version v2 = Version.parse("1.3");

        assertThat(v1.isGreaterThan(v2), is(true));
        assertThat(v2.isGreaterThan(v1), is(false));
    }

    @Test
    public void isGreater_devVersionIsLessThanIfSame() {
        Version v1 = Version.parse("1.4.2");
        Version v2 = Version.parse("1.4.2-b102");

        assertThat(v1.isGreaterThan(v2), is(true));
        assertThat(v2.isGreaterThan(v1), is(false));
    }

    @Test
    public void isGreater_devVersionByAlphabetical() {
        Version v1 = Version.parse("1.4.2-b103");
        Version v2 = Version.parse("1.4.2-b102");

        assertThat(v1.isGreaterThan(v2), is(true));
        assertThat(v2.isGreaterThan(v1), is(false));
    }
}
