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

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Callable;
import me.lachlanap.summis.MemoryUnit;
import me.lachlanap.summis.UpdateInformation.FileInfo;

/**
 *
 * @author Lachlan Phillips
 */
class DownloaderCallable implements Callable<Void> {
    private final FileInfo info;
    private final Path binaryRoot;
    private final Path tmpRoot;
    private final DownloadListener downloadListener;

    public DownloaderCallable(FileInfo info, Path binaryRoot, Path tmpRoot, DownloadListener downloadListener) {
        this.info = info;
        this.binaryRoot = binaryRoot;
        this.tmpRoot = tmpRoot;
        this.downloadListener = downloadListener;
    }

    @Override
    public Void call() throws Exception {
        String filename = info.getName();
        GenericUrl downloadUrl = new GenericUrl(info.getUrl());
        Path destination = binaryRoot.resolve(filename);
        Path tmpDownloadTo = tmpRoot.resolve(filename);
        download(downloadUrl, tmpDownloadTo);
        checkIsRightSize(tmpDownloadTo);
        Files.move(tmpDownloadTo, destination);
        downloadListener.completedADownload();
        return null;
    }

    private void checkIsRightSize(Path tmpDownloadTo) throws IOException, RuntimeException {
        MemoryUnit actualSize = new MemoryUnit(Files.size(tmpDownloadTo));
        if (!actualSize.equals(info.getSize()))
            throw new RuntimeException("Size of " + info.getName() + " does not match expected. " + "Expected: " + info.getSize() + " got: " + actualSize);
    }

    private void download(GenericUrl downloadUrl, Path tmpDownloadTo) throws IOException {
        HttpRequestFactory factory = new NetHttpTransport().createRequestFactory();
        HttpRequest request = factory.buildGetRequest(downloadUrl);
        request.setReadTimeout(3000);
        try (final OutputStream os = new CountingFilterOutputStream(Files.newOutputStream(tmpDownloadTo, StandardOpenOption.CREATE_NEW), downloadListener)) {
            HttpResponse response = request.execute();
            response.download(os);
            response.disconnect();
        }
    }

}
