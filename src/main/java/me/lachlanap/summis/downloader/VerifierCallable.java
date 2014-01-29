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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.concurrent.Callable;
import me.lachlanap.summis.UpdateInformation.FileInfo;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author Lachlan Phillips
 */
class VerifierCallable implements Callable<Void> {
    private final FileInfo info;
    private final Path binaryRoot;
    private final DownloadListener downloadListener;

    public VerifierCallable(FileInfo info, Path binaryRoot, DownloadListener downloadListener) {
        this.info = info;
        this.binaryRoot = binaryRoot;
        this.downloadListener = downloadListener;
    }

    @Override
    public Void call() throws Exception {
        Path file = binaryRoot.resolve(info.getName());
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] buffer = new byte[1024];
        try (final InputStream is = Files.newInputStream(file)) {
            DigestInputStream dis = new DigestInputStream(new DigestInputStream(is, md5), sha1);
            while (dis.read(buffer) != -1) {
                ;
            }
        }
        String md5Digest = new String(Hex.encodeHex(md5.digest()));
        String sha1Digest = new String(Hex.encodeHex(sha1.digest()));
        if (!md5Digest.equals(info.getMD5Digest()) || !sha1Digest.equals(info.getSHA1Digest()))
            throw new RuntimeException(info.getName() + " failed verification");
        downloadListener.completedAVerify();
        return null;
    }

}
