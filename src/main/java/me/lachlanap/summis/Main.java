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

import me.lachlanap.config.Configuration;

/**
 *
 * @author lachlan
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        Configuration config = Configuration.builder()
                .loadBase("core.properties").build();

        UpdateInformationGrabber uig = new UpdateInformationGrabber(config);
        uig.begin();

        VersionReader versionReader = new VersionReader(config);
        UpdateInformation versionInfo = uig.get();

        switch (versionReader.getPresence()) {
            case NotThere:
                // Grab Latest by nondiff
                break;
            case Corrupt:
                // Ask to update
                // then Grab Latest by nondiff
                break;
            case Present:
                Version version = versionReader.getVersion();
                if (versionInfo.getLatest().isGreaterThan(version)) {
                    // Ask to update
                    // then Grab Latest by diff
                }
                break;
        }

        // launch app
    }
}
