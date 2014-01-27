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

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.ExponentialBackOff;
import java.io.IOException;
import java.util.concurrent.*;
import me.lachlanap.config.Configuration;

/**
 *
 * @author lachlan
 */
public class Main {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactory() {

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("Summis Client: Single Thread Executor");
            return thread;
        }
    });

    public static void main(String[] args) throws InterruptedException {
        Configuration config = Configuration.builder()
                .loadBase("core.properties").build();

        Object versionStatus = getVersionInformationAsync(config);

        System.out.println(versionStatus);
        System.out.println(System.getProperty("java.home"));
    }

    private static Object getVersionInformationAsync(final Configuration config) throws InterruptedException {
        Future<Object> versionStatusFuture = EXECUTOR.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return getVersionInformation(config);
            }
        });

        try {
            return versionStatusFuture.get();
        } catch (ExecutionException ee) {
            throw (RuntimeException) ee.getCause();
        }
    }

    private static Object getVersionInformation(Configuration config) {
        String serverAddress = config.getString("server.address");
        String project = config.getString("server.project");

        final int VERSION = 1;
        String updateSourceUrl = serverAddress + String.format("project/%s/%s.%d.json", project, project, VERSION);

        Object versionStatus = null;
        HttpRequestFactory factory = new NetHttpTransport().createRequestFactory();
        try {
            HttpRequest request = factory.buildGetRequest(new GenericUrl(updateSourceUrl));
            request.setUnsuccessfulResponseHandler(new HttpBackOffUnsuccessfulResponseHandler(new ExponentialBackOff()));
            request.setReadTimeout(3000);
            HttpResponse response = request.execute();

            // Deal with response
            versionStatus = null;

            response.disconnect();
        } catch (HttpResponseException hre) {
            if (hre.getStatusCode() == 404) {
                System.out.println("404");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return versionStatus;
    }
}
