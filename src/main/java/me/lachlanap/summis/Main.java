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

import java.nio.file.Path;
import java.nio.file.Paths;
import me.lachlanap.config.Configuration;
import me.lachlanap.summis.ResponseSource.Choice;
import me.lachlanap.summis.downloader.DownloadListener;
import me.lachlanap.summis.downloader.Downloader;
import me.lachlanap.summis.ui.MainUI;
import me.lachlanap.summis.update.UpdateInformationGrabber;

/**
 *
 * @author lachlan
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        Configuration config = Configuration.builder()
                .loadBase("core.properties").build();

        MainUI mainUI = new MainUI();
        run(config,
            mainUI.getStatusListener(),
            mainUI.getResponseSource());
    }

    private static void run(Configuration config,
                            StatusListener statusListener,
                            ResponseSource responseSource) throws InterruptedException {
        Path installRoot = setupInstallRoot(config);

        UpdateInformationGrabber uig = new UpdateInformationGrabber(config);

        statusListener.checking();
        uig.begin();

        VersionReader versionReader = new VersionReader(installRoot);

        try {
            UpdateInformation versionInfo = uig.get(versionReader.getVersion());
            statusListener.foundLatest(versionInfo.getLatest());

            updateIfNeedBe(installRoot,
                           versionReader,
                           statusListener, responseSource,
                           versionInfo);

            statusListener.launching();
            launch(config);
        } catch (RuntimeException re) {
            statusListener.errorChecking(re);

            ResponseSource.Choice choice = responseSource.launchOrQuit();
            if (choice == Choice.Launch) {
                statusListener.launching();
                launch(config);
            }
        } finally {
            statusListener.finished();
        }
    }

    private static Path setupInstallRoot(Configuration config) {
        Path installRoot;
        if (config.getString("install.into-user-dir").equals("true"))
            installRoot = Paths.get(System.getProperty("user.home"));
        else
            installRoot = Paths.get(System.getProperty("user.dir"));
        return installRoot;
    }

    private static void updateIfNeedBe(Path installRoot,
                                       VersionReader versionReader,
                                       StatusListener statusListener,
                                       ResponseSource responseSource,
                                       UpdateInformation versionInfo) throws InterruptedException {
        Downloader downloader = null;
        switch (versionReader.getPresence()) {
            case NotThere:
                DownloadListener downloadListener = statusListener.downloading();
                downloader = new Downloader(installRoot, versionInfo, downloadListener, true);
                break;
            case Corrupt:
                ResponseSource.Choice choice = responseSource.updateOrLaunch();

                if (choice == Choice.Update) {
                    downloadListener = statusListener.downloading();
                    downloader = new Downloader(installRoot, versionInfo, downloadListener, true);
                }
                break;
            case Present:
                if (versionInfo.isNewUpdate()) {
                    choice = responseSource.updateOrLaunch();
                    if (choice == Choice.Update) {
                        downloadListener = statusListener.downloading();
                        downloader = new Downloader(installRoot, versionInfo, downloadListener, false);
                    }
                }
                break;
        }

        if (downloader != null)
            downloader.go();
    }

    private static void launch(Configuration config) {

    }
}
