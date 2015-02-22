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

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class HttpJobIT {

  public static final String TEST_PATH = "/test";

  @ClassRule
  public static WireMockClassRule wireMockClassRule = new WireMockClassRule(0);

  @Rule
  public WireMockClassRule wireMockRule = wireMockClassRule;

  private HttpJob job;

  @Before
  public void setup() throws MalformedURLException {
    job = new HttpJob();
    job.setRestTemplate(new RestTemplate());
    job.setUrl(new URL("http://localhost:" + wireMockRule.port() + TEST_PATH));
    stubFor(any(urlMatching(".*")).willReturn(aResponse()));
  }

  @Test
  public void shouldSendPostToServerWhenRunWithDefaultMethod() {
    job.run();
    verify(postRequestedFor(urlEqualTo(TEST_PATH)));
  }

  @Test
  public void shouldSendSpecifiedMethodToServerWhenRunWithNonDefaultMethod() {
    job.setMethod(HttpMethod.PUT);
    job.run();
    verify(putRequestedFor(urlEqualTo(TEST_PATH)));
  }

  @Test
  public void shouldSendEmptyBodyToServerWhenRunWithNoBody() {
    job.run();
    verify(postRequestedFor(urlEqualTo(TEST_PATH)).withRequestBody(equalTo("")));
  }

  @Test
  public void shouldSendSpecifiedBodyToServerWhenRunWithBody() {
    String testingBody = "Testing body";
    job.setBody(testingBody);
    job.run();
    verify(postRequestedFor(urlEqualTo(TEST_PATH)).withRequestBody(equalTo(testingBody)));
  }

  @Test
  public void shouldSendSpecifiedHeadersToServerWhenRunWithHeaders() {
    String key1 = "k1", key2= "k2", value1= "v1", value2="v2";
    job.setHeaders(ImmutableMap.of(key1, value1, key2, value2));
    job.run();
    verify(postRequestedFor(urlEqualTo(TEST_PATH))
      .withHeader(key1, equalTo(value1))
      .withHeader(key2, equalTo(value2)));
  }

  @Test
  public void shouldSendMultiValuedHeadersToServerWhenRunWithMultiValuedHeaders() {
    String key = "k1", multiValue= "v1,v2";
    job.setHeaders(ImmutableMap.of(key, multiValue));
    job.run();
    verify(postRequestedFor(urlEqualTo(TEST_PATH))
      .withHeader(key, equalTo(multiValue)));
  }

  @Test
  public void shouldSendSpecifiedContentTypeToServerWhenRunWithContentTypeHeader() {
    String key = HttpHeaders.CONTENT_TYPE, value= MediaType.APPLICATION_JSON_VALUE;
    job.setHeaders(ImmutableMap.of(key, value));
    job.run();
    verify(postRequestedFor(urlEqualTo(TEST_PATH))
      .withHeader(key, equalTo(value)));
  }

  //TODO test json serialization
  //TODO test quartz job creation and execution

}
