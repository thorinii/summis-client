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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author Lachlan Phillips
 */
public class VersionRWer {

    public enum PresenceStatus {

        NotThere, Corrupt, Present
    }

    private final Path versionFile;

    private PresenceStatus status;
    private Version version;

    public VersionRWer(Path installRoot) {
        this.versionFile = installRoot.resolve("version");

        load();
    }

    private void load() {
        if (!Files.exists(versionFile)) {
            status = PresenceStatus.NotThere;
        } else {
            try (BufferedReader reader = Files.newBufferedReader(versionFile, StandardCharsets.UTF_8)) {
                String versionString = reader.readLine();
                String versionHashcodeString = reader.readLine();

                if (versionString == null || versionHashcodeString == null)
                    status = PresenceStatus.Corrupt;
                else {
                    validateVersion(versionString, versionHashcodeString);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                status = PresenceStatus.Corrupt;
            }
        }
    }

    private void validateVersion(String versionString, String versionHashcodeString) {
        int versionHashcode;
        try {
            versionHashcode = Integer.parseInt(versionHashcodeString);
        } catch (NumberFormatException nfe) {
            status = PresenceStatus.Corrupt;
            return;
        }

        if (versionString.hashCode() != versionHashcode)
            status = PresenceStatus.Corrupt;
        else {
            status = PresenceStatus.Present;
            version = Version.parse(versionString);
        }
    }

    public PresenceStatus getPresence() {
        return status;
    }

    public Version getVersion() {
        if (status != PresenceStatus.Present)
            throw new IllegalStateException("Cannot query version when it is not present");
        return version;
    }

    public Version getVersionElse(Version def) {
        if (status != PresenceStatus.Present)
            return def;
        return version;
    }

    public void write(Version newVersion) {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(versionFile, StandardCharsets.UTF_8))) {
            String versionString = newVersion.toString();

            writer.println(versionString);
            writer.println(versionString.hashCode());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            status = PresenceStatus.Corrupt;
        }
    }
}
