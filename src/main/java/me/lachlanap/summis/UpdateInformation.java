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

import java.util.Collections;
import java.util.List;
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
        return new FileSet(Collections.EMPTY_LIST);
    }

    public FileSet getFullFileset() {
        return new FileSet(Collections.EMPTY_LIST);
    }

    public static class FileSet {

        private final List<FileInfo> fileInfos;
        private final MemoryUnit totalSize;

        public FileSet(List<FileInfo> fileInfos) {
            this.fileInfos = fileInfos;

            MemoryUnit tmp = new MemoryUnit(0);
            for (FileInfo info : fileInfos) {
                tmp = tmp.plus(info.getSize());
            }
            totalSize = tmp;
        }

        public List<FileInfo> getFiles() {
            return Collections.unmodifiableList(fileInfos);
        }

        public int getFileCount() {
            return fileInfos.size();
        }

        public MemoryUnit getTotalSize() {
            return totalSize;
        }
    }

    public static class FileInfo {

        public String getName() {
            throw new UnsupportedOperationException("FileInfo.getName not supported yet.");
        }

        public MemoryUnit getSize() {
            throw new UnsupportedOperationException("FileInfo.getSize not supported yet.");
        }

        public String getUrl() {
            throw new UnsupportedOperationException("FileInfo.getUrl not supported yet.");
        }

        public String getMD5Digest() {
            throw new UnsupportedOperationException("FileInfo.getMD5Digest not supported yet.");
        }

        public String getSHA1Digest() {
            throw new UnsupportedOperationException("FileInfo.getSHA1Digest not supported yet.");
        }
    }
}
