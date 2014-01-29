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
package me.lachlanap.summis.downloader;

/**
 *
 * @author Lachlan Phillips
 */
public class MemoryUnit {

    public enum Prefix {

        Byte(1, "B"),
        Kilo(Byte.scale * 1024, "KB"),
        Mega(Kilo.scale * 1024, "MB"),
        Giga(Mega.scale * 1024, "GB"),
        Tera(Giga.scale * 1024, "TB");

        private Prefix(long scale, String abbreviation) {
            this.scale = scale;
            this.abbreviation = abbreviation;
        }

        public final long scale;
        public final String abbreviation;
    }

    public static final MemoryUnit ZERO = new MemoryUnit(0);

    private final long bytes;

    public MemoryUnit(long bytes) {
        if (bytes < 0)
            throw new IllegalArgumentException("Cannot have a negative memory space: " + bytes + "bytes");
        this.bytes = bytes;
    }

    public MemoryUnit(Prefix prefix, int count) {
        this.bytes = prefix.scale * count;
    }

    @Override
    public int hashCode() {
        return 237 + (int) (this.bytes ^ (this.bytes >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MemoryUnit other = (MemoryUnit) obj;
        return bytes == other.bytes;
    }

    public MemoryUnit plus(MemoryUnit unit) {
        return new MemoryUnit(bytes + unit.bytes);
    }

    public long inBytes() {
        return bytes;
    }

    public float in(Prefix prefix) {
        return (float) ((double) bytes / prefix.scale);
    }

    public Prefix bestFittingPrefix() {
        if (bytes / Prefix.Tera.scale >= 1)
            return Prefix.Tera;
        if (bytes / Prefix.Giga.scale >= 1)
            return Prefix.Giga;
        if (bytes / Prefix.Mega.scale >= 1)
            return Prefix.Mega;
        if (bytes / Prefix.Kilo.scale >= 1)
            return Prefix.Kilo;
        return Prefix.Byte;
    }

    @Override
    public String toString() {
        Prefix bestFitting = bestFittingPrefix();
        return String.format("%.2f%s", in(bestFitting), bestFitting.abbreviation);
    }
}
