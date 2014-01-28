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

import me.lachlanap.summis.downloader.MemoryUnit;

/**
 *
 * @author Lachlan Phillips
 */
public class UpdateInformation {

    public Version getLatest() {
        //throw new UnsupportedOperationException("UpdateInformation.getLatest not supported yet.");
        return new Version(1, 2, 4);
    }

    public FileSet getDiffFileset() {
        throw new UnsupportedOperationException("UpdateInformation.getDiffFileset not supported yet.");
    }

    public FileSet getFullFileset() {
        throw new UnsupportedOperationException("UpdateInformation.getFullFileset not supported yet.");
    }

    public static class FileSet {

        public Iterable<FileInfo> getFiles() {
            throw new UnsupportedOperationException("FileSet.getFiles not supported yet.");
        }

    }

    public static class FileInfo {

        public MemoryUnit getSize() {
            throw new UnsupportedOperationException("FileInfo.getSize not supported yet.");
        }
    }
}
