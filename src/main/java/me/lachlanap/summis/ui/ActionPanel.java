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
package me.lachlanap.summis.ui;

import javax.swing.JPanel;
import me.lachlanap.summis.MemoryUnit;
import me.lachlanap.summis.StatusListener;
import me.lachlanap.summis.Version;
import me.lachlanap.summis.downloader.DownloadListener;

/**
 *
 * @author Lachlan Phillips
 */
public class ActionPanel extends JPanel implements StatusListener, DownloadListener {

    private int totalFiles;
    private MemoryUnit totalSize;
    private int currentCompleteFiles;
    private MemoryUnit runningTotal;

    /**
     * Creates new form ActionPanel
     */
    public ActionPanel() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        progressBar = new javax.swing.JProgressBar();

        progressBar.setMaximum(1000);
        progressBar.setString("-- not set up --");
        progressBar.setStringPainted(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void checking() {
        progressBar.setIndeterminate(true);
        progressBar.setString("Checking for Updates...");
    }

    @Override
    public void foundLatest(Version latest) {
        progressBar.setIndeterminate(false);
        progressBar.setString("Latest: " + latest);
    }

    @Override
    public void errorChecking(Exception e) {
    }

    @Override
    public DownloadListener downloading() {
        progressBar.setIndeterminate(true);
        progressBar.setString("Downloading...");
        return null;
    }

    @Override
    public void launching() {
        progressBar.setString("Launching...");
    }

    @Override
    public void finished() {
        progressBar.setString("Done");
    }

    @Override
    public void startingDownload(int numberOfFiles, MemoryUnit totalSize) {
        this.totalFiles = numberOfFiles;
        this.totalSize = totalSize;

        currentCompleteFiles = 0;
        runningTotal = MemoryUnit.ZERO;

        progressBar.setIndeterminate(false);
        refreshDownloadStatus();
    }

    @Override
    public void downloadedSome(MemoryUnit amount) {
        runningTotal = runningTotal.plus(amount);
        refreshDownloadStatus();
    }

    @Override
    public void completedADownload() {
        currentCompleteFiles++;
        refreshDownloadStatus();
    }

    @Override
    public void startingVerify(int numberOfFiles) {
        this.totalFiles = numberOfFiles;
        currentCompleteFiles = 0;
        refreshVerifyStatus();
    }

    @Override
    public void completedAVerify() {
        currentCompleteFiles++;
        refreshVerifyStatus();
    }

    private void refreshDownloadStatus() {
        progressBar.setString(
                String.format("Downloaded %d of %d files, %s of %s...",
                              currentCompleteFiles, totalFiles,
                              runningTotal.toString(), totalSize.toString()));
        progressBar.setValue((int) ((float) runningTotal.inBytes() * 1000 / totalSize.inBytes()));
    }

    private void refreshVerifyStatus() {
        progressBar.setString(
                String.format("Verified %d of %d files...",
                              currentCompleteFiles, totalFiles));
        progressBar.setValue((int) ((float) currentCompleteFiles * 1000 / totalFiles));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar progressBar;
    // End of variables declaration//GEN-END:variables


}
