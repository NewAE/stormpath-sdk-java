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
package com.stormpath.sdk.servlet.config;

import com.stormpath.sdk.lang.Assert;

import javax.servlet.ServletContext;
import java.util.Map;

public class EvaluationModel {

    private final ServletContext servletContext;

    private final Map<String,String> config;

    public EvaluationModel(ServletContext servletContext, Map<String, String> config) {
        Assert.notNull(servletContext);
        Assert.notNull(config);
        this.servletContext = servletContext;
        this.config = config;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public Map<String, String> getConfig() {
        return config;
    }
}