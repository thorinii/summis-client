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

    private static final ExecutorService EXECUTOR
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

    private final UpdateInformation versionInfo;
    private final DownloadListener downloadListener;
    private final boolean useDiff;

    public Downloader(UpdateInformation versionInfo, DownloadListener downloadListener, boolean useDiff) {
        this.versionInfo = versionInfo;
        this.downloadListener = downloadListener;
        this.useDiff = useDiff;
    }


    public void go() throws InterruptedException {
        FileSet fileSet = getFileSet();

        int numberOfFiles = 1;
        MemoryUnit totalSize = null;

        downloadListener.startingDownload(numberOfFiles, totalSize);
        downloadFiles(fileSet);

        downloadListener.startingVerify(numberOfFiles);
        verifyFiles(numberOfFiles, fileSet);
    }

    private FileSet getFileSet() {
        FileSet fileSet;
        if (useDiff)
            fileSet = versionInfo.getDiffFileset();
        else
            fileSet = versionInfo.getFullFileset();
        return fileSet;
    }

    private void downloadFiles(FileSet fileSet) throws InterruptedException {
        List<Callable<Void>> downloaders = new ArrayList<>();
        for (FileInfo info : fileSet.getFiles())
            downloaders.add(new DownloaderCallable(info));
        EXECUTOR.invokeAll(downloaders);
    }

    private void verifyFiles(int numberOfFiles, FileSet fileSet) throws InterruptedException {
        List<Callable<Void>> verifiers = new ArrayList<>();
        for (FileInfo info : fileSet.getFiles())
            verifiers.add(new VerifierCallable(info));
        EXECUTOR.invokeAll(verifiers);
    }

    private class DownloaderCallable implements Callable<Void> {

        final FileInfo info;

        public DownloaderCallable(FileInfo info) {
            this.info = info;
        }

        @Override
        public Void call() throws Exception {
            downloadListener.completedADownload(info.getSize());
            throw new UnsupportedOperationException(".call not supported yet.");
        }
    }

    private class VerifierCallable implements Callable<Void> {

        final FileInfo info;

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
