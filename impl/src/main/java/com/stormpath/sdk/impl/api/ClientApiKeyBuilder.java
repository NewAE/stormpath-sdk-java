/*
 * Copyright 2014 Stormpath, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stormpath.sdk.impl.api;

import com.stormpath.sdk.api.ApiKey;
import com.stormpath.sdk.api.ApiKeyBuilder;
import com.stormpath.sdk.lang.Classes;
import com.stormpath.sdk.lang.Strings;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Properties;

/**
 * @since 1.0.RC
 */
public class ClientApiKeyBuilder implements ApiKeyBuilder {

    //private ApiKey apiKey;
    private String apiKeyId;
    private String apiKeySecret;
    private String apiKeyFileLocation;
    private InputStream apiKeyInputStream;
    private Reader apiKeyReader;
    private Properties apiKeyProperties;
    private String apiKeyIdPropertyName = "apiKey.id";
    private String apiKeySecretPropertyName = "apiKey.secret";

    @Override
    public ApiKeyBuilder setId(String id) {
        this.apiKeyId = id;
        return this;
    }

    @Override
    public ApiKeyBuilder setSecret(String secret) {
        this.apiKeySecret = secret;
        return this;
    }

    @Override
    public ApiKeyBuilder setProperties(Properties properties) {
        this.apiKeyProperties = properties;
        return this;
    }

    @Override
    public ApiKeyBuilder setReader(Reader reader) {
        this.apiKeyReader = reader;
        return this;
    }

    @Override
    public ApiKeyBuilder setInputStream(InputStream is) {
        this.apiKeyInputStream = is;
        return this;
    }

    @Override
    public ApiKeyBuilder setFileLocation(String location) {
        this.apiKeyFileLocation = location;
        return this;
    }

    @Override
    public ApiKeyBuilder setIdPropertyName(String idPropertyName) {
        this.apiKeyIdPropertyName = idPropertyName;
        return this;
    }

    @Override
    public ApiKeyBuilder setSecretPropertyName(String secretPropertyName) {
        this.apiKeySecretPropertyName = secretPropertyName;
        return this;
    }

    @Override
    public ApiKey build() {

        ApiKey apiKey;

        if (Strings.hasText(apiKeyId) && Strings.hasText(apiKeySecret)) {
            apiKey = new ClientApiKey(apiKeyId, apiKeySecret);
        } else {
            apiKey = loadApiKey();
        }

        return apiKey;
    }

    //since 0.8
    protected ApiKey loadApiKey() {

        Properties properties = loadApiKeyProperties();

        String apiKeyId = getRequiredPropertyValue(properties, this.apiKeyIdPropertyName, "apiKeyId");

        String apiKeySecret = getRequiredPropertyValue(properties, this.apiKeySecretPropertyName, "apiKeySecret");

        return createApiKey(apiKeyId, apiKeySecret);
    }

    //since 0.8
    protected Properties loadApiKeyProperties() {

        Properties properties = this.apiKeyProperties;

        if (properties == null || properties.isEmpty()) {

            //need to load the properties file:

            Reader reader = getAvailableReader();

            if (reader == null) {
                String msg = "No API Key properties could be found or loaded from a file location.  Please " +
                        "configure the 'apiKeyFileLocation' property or alternatively configure a " +
                        "Properties, Reader or InputStream instance.";
                throw new IllegalArgumentException(msg);
            }

            properties = new Properties();
            try {
                properties.load(reader);
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to load apiKey properties file.", e);
            }
        }

        return properties;
    }

    //since 0.5
    protected ApiKey createApiKey(String id, String secret) {
        return new ClientApiKey(id, secret);
    }

    private String getPropertyValue(Properties properties, String propName) {
        String value = properties.getProperty(propName);
        if (value != null) {
            value = value.trim();
            if ("".equals(value)) {
                value = null;
            }
        }
        return value;
    }

    private String getRequiredPropertyValue(Properties props, String propName, String masterName) {
        String value = getPropertyValue(props, propName);
        if (value == null) {
            String msg = "There is no '" + propName + "' property in the " +
                    "configured apiKey properties.  You can either specify that property or " +
                    "configure the " + masterName + "PropertyName value on the ClientBuilder to specify a " +
                    "custom property name.";
            throw new IllegalArgumentException(msg);
        }
        return value;
    }

    private Reader getAvailableReader() {
        if (this.apiKeyReader != null) {
            return this.apiKeyReader;
        }

        InputStream is = this.apiKeyInputStream;

        if (is == null && this.apiKeyFileLocation != null) {
            try {
                is = ResourceUtils.getInputStreamForPath(apiKeyFileLocation);
            } catch (IOException e) {
                String msg = "Unable to load API Key using apiKeyFileLocation '" + this.apiKeyFileLocation + "'.  " +
                        "Please check and ensure that file exists or use the 'setFileLocation' method to specify " +
                        "a valid location.";
                throw new IllegalStateException(msg, e);
            }
        }

        if (is != null) {
            return toReader(is);
        }

        //no configured input, just return null to indicate this:
        return null;
    }

    private Reader toReader(InputStream is) {
        try {
            return new InputStreamReader(is, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("ISO-8859-1 character set is not available on the current JVM.  " +
                    "This is required to read a Java-compatible Properties file. ", e);
        }
    }

    private static class ResourceUtils {

        /**
         * Resource path prefix that specifies to load from a classpath location, value is <b>{@code classpath:}</b>
         */
        public static final String CLASSPATH_PREFIX = "classpath:";
        /**
         * Resource path prefix that specifies to load from a url location, value is <b>{@code url:}</b>
         */
        public static final String URL_PREFIX = "url:";
        /**
         * Resource path prefix that specifies to load from a file location, value is <b>{@code file:}</b>
         */
        public static final String FILE_PREFIX = "file:";

        /**
         * Prevent instantiation.
         */
        private ResourceUtils() {
        }

        /**
         * Returns {@code true} if the resource path is not null and starts with one of the recognized
         * resource prefixes ({@link #CLASSPATH_PREFIX CLASSPATH_PREFIX},
         * {@link #URL_PREFIX URL_PREFIX}, or {@link #FILE_PREFIX FILE_PREFIX}), {@code false} otherwise.
         *
         * @param resourcePath the resource path to check
         * @return {@code true} if the resource path is not null and starts with one of the recognized
         *         resource prefixes, {@code false} otherwise.
         * @since 0.8
         */
        @SuppressWarnings({"UnusedDeclaration"})
        public static boolean hasResourcePrefix(String resourcePath) {
            return resourcePath != null &&
                    (resourcePath.startsWith(CLASSPATH_PREFIX) ||
                            resourcePath.startsWith(URL_PREFIX) ||
                            resourcePath.startsWith(FILE_PREFIX));
        }

        /**
         * Returns the InputStream for the resource represented by the specified path, supporting scheme
         * prefixes that direct how to acquire the input stream
         * ({@link #CLASSPATH_PREFIX CLASSPATH_PREFIX},
         * {@link #URL_PREFIX URL_PREFIX}, or {@link #FILE_PREFIX FILE_PREFIX}).  If the path is not prefixed by one
         * of these schemes, the path is assumed to be a file-based path that can be loaded with a
         * {@link java.io.FileInputStream FileInputStream}.
         *
         * @param resourcePath the String path representing the resource to obtain.
         * @return the InputStraem for the specified resource.
         * @throws java.io.IOException if there is a problem acquiring the resource at the specified path.
         */
        public static InputStream getInputStreamForPath(String resourcePath) throws IOException {

            InputStream is;
            if (resourcePath.startsWith(CLASSPATH_PREFIX)) {
                is = loadFromClassPath(stripPrefix(resourcePath));

            } else if (resourcePath.startsWith(URL_PREFIX)) {
                is = loadFromUrl(stripPrefix(resourcePath));

            } else if (resourcePath.startsWith(FILE_PREFIX)) {
                is = loadFromFile(stripPrefix(resourcePath));

            } else {
                is = loadFromFile(resourcePath);
            }

            if (is == null) {
                throw new IOException("Resource [" + resourcePath + "] could not be found.");
            }

            return is;
        }

        private static InputStream loadFromFile(String path) throws IOException {
            return new FileInputStream(path);
        }

        private static InputStream loadFromUrl(String urlPath) throws IOException {
            URL url = new URL(urlPath);
            return url.openStream();
        }

        private static InputStream loadFromClassPath(String path) {
            return Classes.getResourceAsStream(path);
        }

        private static String stripPrefix(String resourcePath) {
            return resourcePath.substring(resourcePath.indexOf(":") + 1);
        }
    }


}
