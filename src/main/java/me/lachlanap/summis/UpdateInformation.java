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
import java.util.Objects;

/**
 *
 * @author Lachlan Phillips
 */
public class UpdateInformation {

    private final Version latest;
    private final Version current;
    private final FileSet diffSet;
    private final FileSet fullSet;

    public UpdateInformation(Version latest, Version current, FileSet diffSet, FileSet fullSet) {
        this.latest = latest;
        this.current = current;
        this.diffSet = diffSet;
        this.fullSet = fullSet;
    }

    public Version getLatest() {
        return latest;
    }

    public FileSet getDiffFileset() {
        return diffSet;
    }

    public FileSet getFullFileset() {
        return fullSet;
    }

    public boolean isNewUpdate() {
        return latest.isGreaterThan(current);
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

        @Override
        public String toString() {
            return "FileSet{fileInfos=" + fileInfos + ", totalSize=" + totalSize + '}';
        }
    }

    public static class FileInfo {

        private final String name;
        private final MemoryUnit size;
        private final String url;
        private final String md5;
        private final String sha1;

        public FileInfo(String name, MemoryUnit size, String url, String md5, String sha1) {
            this.name = name;
            this.size = size;
            this.url = url;
            this.md5 = md5;
            this.sha1 = sha1;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 19 * hash + Objects.hashCode(this.name);
            hash = 19 * hash + Objects.hashCode(this.size);
            hash = 19 * hash + Objects.hashCode(this.md5);
            hash = 19 * hash + Objects.hashCode(this.sha1);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final FileInfo other = (FileInfo) obj;
            if (!Objects.equals(this.name, other.name))
                return false;
            if (!Objects.equals(this.size, other.size))
                return false;
            if (!Objects.equals(this.md5, other.md5))
                return false;
            if (!Objects.equals(this.sha1, other.sha1))
                return false;
            return true;
        }

        public String getName() {
            return name;
        }

        public MemoryUnit getSize() {
            return size;
        }

        public String getUrl() {
            return url;
        }

        public String getMD5Digest() {
            return md5;
        }

        public String getSHA1Digest() {
            return sha1;
        }

        @Override
        public String toString() {
            return "FileInfo{name=" + name + ", size=" + size + '}';
        }
    }
}
