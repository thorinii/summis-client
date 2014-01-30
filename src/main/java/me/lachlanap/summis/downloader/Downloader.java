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

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import me.lachlanap.summis.MemoryUnit;
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

        createDirectory(tmpRoot);
        createDirectory(binaryRoot);

        downloadListener.startingDownload(numberOfFiles, totalSize);
        downloadFiles(fileSet);

        downloadListener.startingVerify(numberOfFiles);
        verifyFiles(fileSet);

        deleteDirectory(tmpRoot);
    }

    private FileSet getFileSet() {
        if (downloadFresh)
            return versionInfo.getFullFileset();
        else
            return versionInfo.getDiffFileset();
    }

    private void createDirectory(Path directory) {
        try {
            deleteDirectory(directory);
            Files.createDirectory(directory);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to delete tmp download directory", ex);
        }
    }

    private void deleteDirectory(Path directory) {
        try {
            if (Files.exists(directory)) {
                Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (!Files.isDirectory(file))
                            Files.delete(file);
                        return super.visitFile(file, attrs);
                    }

                });
                Files.delete(directory);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to delete tmp download directory", ex);
        }
    }

    private void downloadFiles(FileSet fileSet) throws InterruptedException {
        List<Callable<Void>> downloaders = new ArrayList<>();
        for (FileInfo info : fileSet.getFiles())
            downloaders.add(new DownloaderCallable(info, binaryRoot, tmpRoot, downloadListener));

        List<Future<Void>> futures = EXECUTOR.invokeAll(downloaders);
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (ExecutionException ee) {
                ee.getCause().printStackTrace();
            }
        }

        deleteInvalidFiles();
    }

    private void deleteInvalidFiles() {
        try {
            Files.walkFileTree(binaryRoot, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!Files.isDirectory(file)) {
                        String relativeFilename = binaryRoot.relativize(file).toString();

                        boolean found = false;
                        for (FileInfo info : versionInfo.getFullFileset().getFiles())
                            if (info.getName().equals(relativeFilename))
                                found = true;

                        if (!found)
                            Files.delete(file);
                    }
                    return super.visitFile(file, attrs);
                }

            });
        } catch (IOException ex) {
            throw new RuntimeException("Failed to delete tmp download directory", ex);
        }
    }

    private void verifyFiles(FileSet fileSet) throws InterruptedException {
        List<Callable<Void>> verifiers = new ArrayList<>();
        for (FileInfo info : fileSet.getFiles())
            verifiers.add(new VerifierCallable(info, binaryRoot, downloadListener));
        EXECUTOR.invokeAll(verifiers);
    }
}
