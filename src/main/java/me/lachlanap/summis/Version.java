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

import java.util.regex.Pattern;

/**
 *
 * @author Lachlan Phillips
 */
public final class Version implements Comparable<Version> {

    public static final String DEVSTATUS_RELEASE = "";

    public static final int MAJOR = 0;
    public static final int MINOR = 1;
    public static final int PATCH = 2;

    private final int[] numbers;
    private final String devStatus;

    public Version(int... numbers) {
        this(numbers, DEVSTATUS_RELEASE);
    }

    public Version(int[] numbers, String devStatus) {
        this.numbers = new int[numbers.length];
        System.arraycopy(numbers, 0, this.numbers, 0, numbers.length);

        this.devStatus = devStatus;
    }

    public int getNumberCount() {
        return numbers.length;
    }

    public int get(int index) {
        return numbers[index];
    }

    public String getDevStatus() {
        return devStatus;
    }

    public boolean isDevRelease() {
        return !devStatus.equals(DEVSTATUS_RELEASE);
    }

    public boolean isGreaterThan(Version version) {
        return compareTo(version) > 0;
    }

    @Override
    public int compareTo(Version o) {
        // check if same
        for (int i = 0; i < Math.min(numbers.length, o.numbers.length); i++) {
            if (numbers[i] != o.numbers[i])
                return numbers[i] - o.numbers[i];
        }

        // If the same *up to the matching point* go with the longer one
        if (o.numbers.length != numbers.length)
            return o.numbers.length - numbers.length;

        if (isDevRelease() ^ o.isDevRelease())
            return isDevRelease() ? -1 : 1;

        return getDevStatus().compareTo(o.getDevStatus());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < numbers.length; i++) {
            if (i > 0)
                builder.append(".");
            builder.append(numbers[i]);
        }

        if (isDevRelease())
            builder.append('-').append(devStatus);

        return builder.toString();
    }

    public static Version parse(String string) {
        String[] split = string.split(Pattern.quote("."));
        int[] numbers = new int[split.length];

        for (int i = 0; i < split.length; i++) {
            String piece = split[i];

            if (i == split.length - 1) {
                // cut off any -dev bits

                int indexOfDash = piece.indexOf('-');
                if (indexOfDash > 0)
                    piece = piece.substring(0, indexOfDash);
            }

            numbers[i] = Integer.parseInt(piece);
        }

        String lastPiece = split[split.length - 1];
        if (lastPiece.contains("-")) {
            String devStatus = lastPiece.substring(lastPiece.indexOf('-') + 1);
            return new Version(numbers, devStatus);
        } else
            return new Version(numbers, DEVSTATUS_RELEASE);
    }
}
