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
package me.lachlanap.summis.update;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.ExponentialBackOff;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import me.lachlanap.config.Configuration;
import me.lachlanap.summis.MemoryUnit;
import me.lachlanap.summis.UpdateInformation;
import me.lachlanap.summis.UpdateInformation.FileInfo;
import me.lachlanap.summis.UpdateInformation.FileSet;
import me.lachlanap.summis.Version;

/**
 *
 * @author Lachlan Phillips
 */
public class UpdateInformationGrabber {

    private static final int FORMAT_VERSION = 1;

    private final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactory() {

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("Summis Client: Update Information HTTP Executor");
            return thread;
        }
    });

    private final Configuration config;
    private Future<Void> future;
    private final List<VersionInfo> versions;

    public UpdateInformationGrabber(Configuration config) {
        this.config = config;
        this.versions = new ArrayList<>();
    }

    public void begin() {
        future = EXECUTOR.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getVersionInformation(config);
                return null;
            }
        });
    }

    private void getVersionInformation(Configuration config) throws IOException {
        String serverAddress = config.getString("server.address");
        String project = config.getString("server.project");

        final int VERSION = 1;
        String updateSourceUrl = serverAddress + String.format("%d/project/%s/%s.json", VERSION, project, project);

        HttpRequestFactory factory = new NetHttpTransport().createRequestFactory();
        try {
            HttpRequest request = factory.buildGetRequest(new GenericUrl(updateSourceUrl));
            request.setUnsuccessfulResponseHandler(new HttpBackOffUnsuccessfulResponseHandler(new ExponentialBackOff()));
            request.setReadTimeout(3000);
            HttpResponse response = request.execute();

            process(response.parseAsString());

            response.disconnect();
        } catch (HttpResponseException hre) {
            if (hre.getStatusCode() == 404) {
                System.out.println(updateSourceUrl + " does not exist");
            }
        }
    }

    private void process(String allText) {
        JsonObject json = new JsonParser().parse(allText).getAsJsonObject();
        if (json.get("version").getAsInt() != FORMAT_VERSION)
            throw new RuntimeException("Format version "
                                       + json.get("version").getAsInt()
                                       + " is not supported.");

        versions.clear();
        for (JsonElement element : json.getAsJsonArray("versions")) {
            versions.add(parseVersion(element.getAsJsonObject()));
        }

        // Sort latest to earliest
        Collections.sort(versions, new Comparator<VersionInfo>() {
            @Override
            public int compare(VersionInfo o1, VersionInfo o2) {
                return -o1.version.compareTo(o2.version);
            }
        });
    }

    private VersionInfo parseVersion(JsonObject versionJson) {
        Version version = Version.parse(versionJson.get("number").getAsString());
        String description = versionJson.get("description").getAsString();
        FileSet diffSet = parseFileSet(versionJson.getAsJsonArray("diff"));
        FileSet fullSet = parseFileSet(versionJson.getAsJsonArray("full"));

        return new VersionInfo(version, description, diffSet, fullSet);
    }

    private FileSet parseFileSet(JsonArray fileSetJson) {
        List<FileInfo> infos = new ArrayList<>();
        for (JsonElement element : fileSetJson) {
            JsonObject fileInfoJson = element.getAsJsonObject();

            infos.add(new FileInfo(fileInfoJson.get("name").getAsString(),
                                   new MemoryUnit(fileInfoJson.get("size").getAsLong()),
                                   fileInfoJson.get("url").getAsString(),
                                   fileInfoJson.get("md5").getAsString(),
                                   fileInfoJson.get("sha1").getAsString()));
        }

        return new FileSet(infos);
    }

    public UpdateInformation get(Version current) throws InterruptedException {
        try {
            future.get();
        } catch (ExecutionException ee) {
            if (ee.getCause() instanceof RuntimeException)
                throw (RuntimeException) ee.getCause();
            else
                throw new RuntimeException(ee.getCause());
        }

        VersionInfo latest = versions.get(0);
        FileSet diffSet = computeDiffSet(current, latest);
        UpdateInformation info = new UpdateInformation(latest.version, current,
                                                       diffSet, latest.fullSet);
        return info;
    }

    private FileSet computeDiffSet(Version current, VersionInfo latest) {
        List<FileInfo> diffSetList = new ArrayList<>();
        for (VersionInfo aVersion : versions) {
            if (!aVersion.version.isGreaterThan(current))
                break;

            for (FileInfo aFile : aVersion.diffSet.getFiles()) {
                boolean alreadyInDiffSet = false;
                for (FileInfo check : diffSetList)
                    if (aFile.getName().equals(check.getName()))
                        alreadyInDiffSet = true;

                if (!alreadyInDiffSet && latest.fullSet.getFiles().contains(aFile))
                    diffSetList.add(aFile);
            }
        }

        return new FileSet(diffSetList);
    }

    private static class VersionInfo {

        final Version version;
        final String description;
        final FileSet diffSet;
        final FileSet fullSet;

        public VersionInfo(Version version, String description, FileSet diffSet, FileSet fullSet) {
            this.version = version;
            this.description = description;
            this.diffSet = diffSet;
            this.fullSet = fullSet;
        }
    }
}
