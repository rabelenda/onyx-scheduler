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
import com.google.common.collect.ImmutableMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotNull;

/**
 * Job which executes http requests logging their response when they are done. <p/> Currently no
 * HTTP headers are supported. And by default the POST method is used if not specified.
 */

public class HttpJob extends Job {

  private static final Logger LOG = LoggerFactory.getLogger(HttpJob.class);
  public static final String URL_DATAMAP_KEY = "url";
  public static final String METHOD_DATAMAP_KEY = "method";
  public static final String BODY_DATAMAP_KEY = "body";
  public static final String HEADERS_JSON_DATAMAP_KEY = "headersJson";

  @NotNull
  private URL url;
  private HttpMethod method = HttpMethod.POST;
  private String body;
  private Map<String, String> headers = Collections.emptyMap();

  @SuppressWarnings("SpringJavaAutowiredMembersInspection")
  @Autowired
  private RestTemplate restTemplate;

  private ObjectMapper jsonMapper = new ObjectMapper();

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

  @SuppressWarnings("UnusedDeclaration")
  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  //this method is used by spring to automatically populate it when building the job from jobDetail
  @SuppressWarnings("unchecked")
  @JsonIgnore
  public void setHeadersJson(String json) {
    try {
      headers = jsonMapper.readValue(json, Map.class);
    } catch (IOException e) {
      //This exception should never happen so just propagating it for the unexpected problem
      throw Throwables.propagate(e);
    }
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
    try {
      builder.put(HEADERS_JSON_DATAMAP_KEY, jsonMapper.writeValueAsString(headers));
    } catch (JsonProcessingException e) {
      //This exception should never happen so just propagating it for the unexpected problem
      throw Throwables.propagate(e);
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
    setHeadersJson((String) dataMap.get(HEADERS_JSON_DATAMAP_KEY));
  }

  @Override
  public void run() {
    HttpHeaders httpHeaders = new HttpHeaders();
    headers.forEach(httpHeaders::add);
    HttpEntity<String> request = new HttpEntity<>(body, httpHeaders);
    ResponseEntity<String> response =
        restTemplate.exchange(url.toString(), method, request, String.class);
    int code = response.getStatusCode().value();
    String responseBody = response.getBody();
    LOG.info("{}", new HttpAuditRecord(this, code, responseBody));
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, group, name, triggers, url, method, body, headers);
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
           && Objects.equals(this.url, other.url) && Objects.equals(this.method, other.method)
           && Objects.equals(this.body, other.body) && Objects.equals(this.headers, other.headers);
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
        .add("headers", headers)
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
