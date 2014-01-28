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

import java.util.concurrent.CountDownLatch;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import me.lachlanap.summis.ResponseSource;

/**
 *
 * @author Lachlan Phillips
 */
public class ResponsePanel extends JPanel implements ResponseSource {

    private CountDownLatch latch;
    private Choice choice;

    public ResponsePanel() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        updateBtn = new javax.swing.JButton();
        launchBtn = new javax.swing.JButton();

        updateBtn.setText("Update");
        updateBtn.setEnabled(false);
        updateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateBtnActionPerformed(evt);
            }
        });

        launchBtn.setText("Just Launch");
        launchBtn.setEnabled(false);
        launchBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                launchBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(updateBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(launchBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(launchBtn)
                    .addComponent(updateBtn))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void updateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed
        choice = Choice.Update;
        latch.countDown();
    }//GEN-LAST:event_updateBtnActionPerformed

    private void launchBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_launchBtnActionPerformed
        choice = Choice.Launch;
        latch.countDown();
    }//GEN-LAST:event_launchBtnActionPerformed

    @Override
    public Choice updateOrLaunch() throws InterruptedException {
        latch = new CountDownLatch(1);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                updateBtn.setText("Update");
                updateBtn.setEnabled(true);
                launchBtn.setEnabled(true);
            }
        });

        latch.await();

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                updateBtn.setEnabled(false);
                launchBtn.setEnabled(false);
            }
        });

        return choice;
    }

    @Override
    public Choice launchOrQuit() throws InterruptedException {
        latch = new CountDownLatch(1);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                updateBtn.setText("Quit");
                updateBtn.setEnabled(true);
                launchBtn.setEnabled(true);
            }
        });

        latch.await();

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                updateBtn.setEnabled(false);
                launchBtn.setEnabled(false);
            }
        });

        return choice;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton launchBtn;
    private javax.swing.JButton updateBtn;
    // End of variables declaration//GEN-END:variables
}
