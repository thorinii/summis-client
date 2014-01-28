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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;
import me.lachlanap.summis.ResponseSource;
import me.lachlanap.summis.StatusListener;
import me.lachlanap.summis.Version;
import me.lachlanap.summis.downloader.DownloadListener;

/**
 *
 * @author Lachlan Phillips
 */
public class MainUI {

    private JDialog window;
    private ActionPanel actionPanel;
    private ResponsePanel responsePanel;
    private InfoPanel infoPanel;

    public MainUI() throws InterruptedException {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    window = new JDialog();
                    window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                    window.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));

                    Container c = window.getContentPane();
                    c.setLayout(new BorderLayout());

                    actionPanel = new ActionPanel();
                    responsePanel = new ResponsePanel();
                    infoPanel = new InfoPanel();

                    c.add(actionPanel, BorderLayout.CENTER);
                    c.add(responsePanel, BorderLayout.SOUTH);

                    window.pack();
                    window.setMinimumSize(new Dimension(window.getSize()));
                }
            });
        } catch (InvocationTargetException ite) {
            throw new RuntimeException("Error setting up main window", ite);
        }
    }

    public StatusListener getStatusListener() {
        return new UIStatusNotifier();
    }

    public ResponseSource getResponseSource() {
        return responsePanel;
    }

    private class UIStatusNotifier implements StatusListener {

        @Override
        public void checking() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    actionPanel.checking();

                    window.setVisible(true);
                }
            });
        }

        @Override
        public void foundLatest(final Version latest) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    actionPanel.foundLatest(latest);
                }
            });
        }

        @Override
        public void errorChecking(final Exception e) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Container c = window.getContentPane();
                    c.remove(actionPanel);
                    c.add(infoPanel, BorderLayout.CENTER);

                    infoPanel.setMessage("Error: "
                                         + ((e.getCause() == null) ? e.getClass() : e.getCause().getClass()),
                                         e.toString());
                    window.pack();
                    window.setMinimumSize(new Dimension(window.getSize()));
                }

            });
        }

        @Override
        public DownloadListener downloading() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    actionPanel.downloading();
                }
            });

            return null;
        }

        @Override
        public void launching() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    actionPanel.launching();
                }
            });
        }

        @Override
        public void finished() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    actionPanel.finished();

                    window.setVisible(false);
                    window.dispose();
                }
            });
        }
    }

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
    }
}
