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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import me.lachlanap.summis.MemoryUnit;
import me.lachlanap.summis.MemoryUnit.Prefix;

/**
 *
 * @author Lachlan Phillips
 */
public class CountingFilterOutputStream extends FilterOutputStream {
    private static final MemoryUnit INTERVAL = new MemoryUnit(Prefix.Kilo, 2);
    private final DownloadListener listener;
    private long transferedSoFar;
    private long lastNotify;

    public CountingFilterOutputStream(OutputStream out, DownloadListener listener) {
        super(out);
        this.listener = listener;
        transferedSoFar = 0;
        lastNotify = 0;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        transferedSoFar += len;
        while ((transferedSoFar - lastNotify) > INTERVAL.inBytes()) {
            listener.downloadedSome(INTERVAL);
            lastNotify += INTERVAL.inBytes();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        listener.downloadedSome(new MemoryUnit(transferedSoFar - lastNotify));
    }

}
