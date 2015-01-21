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

import com.google.common.base.*;
import com.google.common.collect.ImmutableMap;
import java.net.*;
import java.util.*;
import java.util.Objects;
import javax.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

  @SuppressWarnings("SpringJavaAutowiredMembersInspection")
  @Autowired
  private RestTemplate restTemplate;

  @SuppressWarnings("UnusedDeclaration")
  public URL getUrl() {
    return url;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  @SuppressWarnings("UnusedDeclaration")
  public HttpMethod getMethod() {
    return method;
  }

  public void setMethod(HttpMethod method) {
    this.method = method;
  }

  @SuppressWarnings("UnusedDeclaration")
  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public void setRestTemplate(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  protected Map<String, Object> buildDataMap() {
    ImmutableMap.Builder<String, Object> builder = ImmutableMap.<String, Object>builder()
      .put(URL_DATAMAP_KEY, url.toString())
      .put(METHOD_DATAMAP_KEY, method.toString());
    if (body != null) {
      builder.put(BODY_DATAMAP_KEY, body);
    }
    return builder.build();
  }

  @Override
  protected void initFromDataMap(Map<String, Object> dataMap) {
    try {
      url = new URL((String) dataMap.get(URL_DATAMAP_KEY));
    } catch (MalformedURLException e) {
      throw Throwables.propagate(e);
    }
    method = HttpMethod.valueOf((String) dataMap.get(METHOD_DATAMAP_KEY));
    body = (String) dataMap.get(BODY_DATAMAP_KEY);
  }

  @Override
  public void run() {
    HttpEntity<String> request = new HttpEntity<>(body);
    ResponseEntity<String> response =
      restTemplate.exchange(url.toString(), method, request, String.class);
    int code = response.getStatusCode().value();
    String responseBody = response.getBody();
    LOG.info("{}", new HttpAuditRecord(this, code, responseBody));
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, group, name, triggers, url, method, body);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final HttpJob other = (HttpJob) obj;
    return Objects.equals(this.id, other.id) && Objects.equals(this.group, other.group)
      && Objects.equals(this.name, other.name) && Objects.equals(this.triggers, other.triggers)
      && Objects.equals(this.url, other.url) && Objects.equals(this.method, other.method) && Objects
      .equals(this.body, other.body);
  }

  @Override
  public String toString() {
    return com.google.common.base.Objects.toStringHelper(this)
      .add("id", id)
      .add("group", group)
      .add("name", name)
      .add("triggers", triggers)
      .add("url", url)
      .add("method", method)
      .add("body", body)
      .toString();
  }

  private static class HttpAuditRecord {

    private final HttpJob request;
    private final int responseCode;
    private final String responseBody;

    private HttpAuditRecord(
      HttpJob request,
      int responseCode,
      String responseBody) {
      this.request = request;
      this.responseCode = responseCode;
      this.responseBody = responseBody;
    }

    @Override
    public String toString() {
      return com.google.common.base.Objects.toStringHelper(this)
        .add("request", request)
        .add("responseCode", responseCode)
        .add("responseBody", responseBody)
        .toString();
    }
  }

}
