/*
 * Copyright (C) 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onyxscheduler.domain;

import com.google.common.base.Throwables;
import java.net.*;
import javax.validation.constraints.*;
import org.quartz.JobDataMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Job which executes http requests logging their response when they are done.
 * <p/>
 * Currently no HTTP headers are supported. And by default the POST method is used if not specified.
 */
public class HttpJob extends Job {

  private static final Logger LOG = LoggerFactory.getLogger(HttpJob.class);
  public static final String URL_DATAMAP_KEY = "url";
  public static final String METHOD_DATAMAP_KEY = "method";
  public static final String BODY_DATAMAP_KEY = "body";

  @NotNull
  private URL url;
  private HttpMethod method = HttpMethod.POST;
  private String body;

  public URL getUrl() {
    return url;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  public HttpMethod getMethod() {
    return method;
  }

  public void setMethod(HttpMethod method) {
    this.method = method;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  @Override
  protected JobDataMap buildQuartzJobDataMap() {
    JobDataMap dataMap = new JobDataMap();
    dataMap.put(URL_DATAMAP_KEY, url.toString());
    dataMap.put(METHOD_DATAMAP_KEY, method.toString());
    dataMap.put(BODY_DATAMAP_KEY, body);
    return dataMap;
  }

  @Override
  protected void initFromQuartzJobDataMap(JobDataMap jobDataMap) {
    try {
      url = new URL(jobDataMap.getString(URL_DATAMAP_KEY));
    } catch (MalformedURLException e) {
      throw Throwables.propagate(e);
    }
    method = HttpMethod.valueOf(jobDataMap.getString(METHOD_DATAMAP_KEY));
    body = jobDataMap.getString(BODY_DATAMAP_KEY);
  }

  @Override
  public void run() {
    HttpEntity<String> request = new HttpEntity<>(body);
    ResponseEntity<String> response =
      new RestTemplate().exchange(url.toString(), method, request, String.class);
    int code = response.getStatusCode().value();
    String responseBody = response.getBody();
    LOG.info("{} {} => {}\n{}", method, url, code, responseBody);
  }

}
