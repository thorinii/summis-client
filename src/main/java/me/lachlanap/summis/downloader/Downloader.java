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

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import me.lachlanap.summis.UpdateInformation;
import me.lachlanap.summis.UpdateInformation.FileInfo;
import me.lachlanap.summis.UpdateInformation.FileSet;

/**
 *
 * @author Lachlan Phillips
 */
public class Downloader {

    private static final String BINARY_DIRECTORY = "bin";

    private final ExecutorService EXECUTOR
            = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                                           new ThreadFactory() {

                                               @Override
                                               public Thread newThread(Runnable r) {
                                                   Thread thread = new Thread(r);
                                                   thread.setDaemon(true);
                                                   thread.setName("Summis Client: HTTP Download Executor");
                                                   return thread;
                                               }
                                           });

    private final Path installRoot;
    private final Path binaryRoot;
    private final Path tmpRoot;
    private final UpdateInformation versionInfo;
    private final DownloadListener downloadListener;
    private final boolean downloadFresh;

    public Downloader(Path installRoot,
                      UpdateInformation versionInfo,
                      DownloadListener downloadListener,
                      boolean downloadFresh) {
        this.installRoot = installRoot;
        this.binaryRoot = installRoot.resolve(BINARY_DIRECTORY);
        this.tmpRoot = installRoot.resolve("tmp");

        this.versionInfo = versionInfo;
        this.downloadListener = downloadListener;
        this.downloadFresh = downloadFresh;
    }


    public void go() throws InterruptedException {
        FileSet fileSet = getFileSet();

        int numberOfFiles = fileSet.getFileCount();
        MemoryUnit totalSize = fileSet.getTotalSize();

        if (numberOfFiles == 0)
            return;

        deleteTmpDirectory();

        downloadListener.startingDownload(numberOfFiles, totalSize);
        downloadFiles(fileSet);

        downloadListener.startingVerify(numberOfFiles);
        verifyFiles(fileSet);

        deleteTmpDirectory();
    }

    private FileSet getFileSet() {
        FileSet fileSet;
        if (downloadFresh)
            fileSet = versionInfo.getFullFileset();
        else
            fileSet = versionInfo.getDiffFileset();
        return fileSet;
    }

    private void deleteTmpDirectory() {
        try {
            Files.deleteIfExists(tmpRoot);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to delete tmp download directory", ex);
        }
    }

    private void downloadFiles(FileSet fileSet) throws InterruptedException {
        List<Callable<Void>> downloaders = new ArrayList<>();
        for (FileInfo info : fileSet.getFiles())
            downloaders.add(new DownloaderCallable(info));
        EXECUTOR.invokeAll(downloaders);
    }

    private void verifyFiles(FileSet fileSet) throws InterruptedException {
        List<Callable<Void>> verifiers = new ArrayList<>();
        for (FileInfo info : fileSet.getFiles())
            verifiers.add(new VerifierCallable(info));
        EXECUTOR.invokeAll(verifiers);
    }

    private class DownloaderCallable implements Callable<Void> {

        private final FileInfo info;

        public DownloaderCallable(FileInfo info) {
            this.info = info;
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

            downloadListener.completedADownload(info.getSize());
            return null;
        }

        private void checkIsRightSize(Path tmpDownloadTo) throws IOException, RuntimeException {
            MemoryUnit actualSize = new MemoryUnit(Files.size(tmpDownloadTo));
            if (!actualSize.equals(info.getSize()))
                throw new RuntimeException("Size of " + info.getName()
                                           + " does not match expected. "
                                           + "Expected: " + info.getSize()
                                           + " got: " + actualSize);
        }

        private void download(GenericUrl downloadUrl, Path tmpDownloadTo) throws IOException {
            HttpRequestFactory factory = new NetHttpTransport().createRequestFactory();
            HttpRequest request = factory.buildGetRequest(downloadUrl);
            request.setReadTimeout(3000);
            try (OutputStream os = Files.newOutputStream(tmpDownloadTo)) {
                HttpResponse response = request.execute();
                response.download(os);
                response.disconnect();
            }
        }
    }

    private class VerifierCallable implements Callable<Void> {

        private final FileInfo info;

        public VerifierCallable(FileInfo info) {
            this.info = info;
        }

        @Override
        public Void call() throws Exception {
            downloadListener.completedAVerify();
            throw new UnsupportedOperationException(".call not supported yet.");
        }
    }
}
