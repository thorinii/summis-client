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
package me.lachlanap.summis.launcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import me.lachlanap.config.Configuration;

/**
 *
 * @author Lachlan Phillips
 */
public class Launcher {

    private final Configuration config;
    private final Path installRoot;

    private final String mainClass;
    private final Path binaryRoot;

    public Launcher(Configuration config, Path installRoot) {
        this.config = config;
        this.installRoot = installRoot;

        this.mainClass = config.getString("launcher.main-class");
        this.binaryRoot = installRoot.resolve("bin");
    }

    public void launch() {
        try {
            List<String> cmd = buildCommand();
            System.out.println(cmd);
            run(cmd);
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to launch app", ioe);
        }
    }

    private List<String> buildCommand() throws IOException {
        List<String> cmd = new ArrayList<>();

        String javaBin = findJavaBin().toString();
        cmd.add(javaBin);

        StringBuilder classpath = new StringBuilder();
        int i = 0;
        for (Path binary : findBinaries()) {
            if (i > 0)
                classpath.append(File.pathSeparatorChar);
            classpath.append(binary.toString());
            i++;
        }

        cmd.add("-cp");
        cmd.add(classpath.toString());

        cmd.add(mainClass);

        return cmd;
    }

    private Path findJavaBin() {
        Path javaHome = Paths.get(System.getProperty("java.home"));
        String osName = System.getProperty("os.name");

        if (osName.toLowerCase().contains("win"))
            return javaHome.resolve("bin/javaw.exe");
        else
            return javaHome.resolve("bin/java");
    }

    private List<Path> findBinaries() throws IOException {
        List<Path> binaries = new ArrayList<>();

        try (DirectoryStream<Path> binaryRootFiles = Files.newDirectoryStream(binaryRoot)) {
            for (Path file : binaryRootFiles) {
                if (file.toString().endsWith(".jar"))
                    binaries.add(file);
            }
        }

        return binaries;
    }

    private void run(List<String> cmd) throws IOException {
        Process process = new ProcessBuilder()
                .command(cmd)
                .directory(installRoot.toFile())
                .redirectError(installRoot.resolve("stderr.txt").toFile())
                .redirectOutput(installRoot.resolve("stdout.txt").toFile())
                .start();
    }
}
