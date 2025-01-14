/*
 * Copyright 2017 Stephan Fuhrmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.sfuhrm.radiorecorder.http;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Builder for a Apache HttpClient v4 based connection.
 * @author Stephan Fuhrmann
 */
@Slf4j
class ApacheHttpClient4ConnectionBuilder extends AbstractHttpConnectionBuilder implements HttpConnectionBuilder {

    private final RequestConfig.Builder configBuilder;
    private final RequestBuilder requestBuilder;

    ApacheHttpClient4ConnectionBuilder(URL url) throws URISyntaxException {
        configBuilder = RequestConfig.custom();
        requestBuilder = RequestBuilder.get(url.toURI());

        log.debug("Request for uri {}", requestBuilder.getUri());
    }

    @Override
    public HttpConnection build() throws IOException {
        if (connectTimeout.isPresent()) {
            configBuilder.setConnectTimeout(connectTimeout.get());
            configBuilder.setConnectionRequestTimeout(connectTimeout.get());
        }
        readTimeout.ifPresent(configBuilder::setSocketTimeout);
        if (! requestProperties.isEmpty()) {
            requestProperties
                    .forEach(requestBuilder::addHeader);
        }
        if (proxy.isPresent()) {
            HttpHost proxyHost = new HttpHost(proxy.get().getHost(), proxy.get().getPort(), proxy.get().getProtocol());
            configBuilder.setProxy(proxyHost);
        }

        CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(configBuilder.build()).build();
        return new ApacheHttpClient4Connection(client, client.execute(requestBuilder.build()), requestBuilder.getUri());
    }
}
