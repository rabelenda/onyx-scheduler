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

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.net.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class HttpJobIT {

  private static final String BODY = "Testing body";
  public static final String TEST_PATH = "/test";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(0);

  private HttpJob job;

  @Before
  public void setup() throws MalformedURLException {
    job = new HttpJob();
    job.setUrl(new URL("http://localhost:" + wireMockRule.port() + TEST_PATH));
    job.setBody(BODY);

    stubFor(any(urlMatching(".*")).willReturn(aResponse()));
  }

  @Test
  public void shouldSendPostToServerWhenRunWithDefaultMethod() {
    job.run();
    verify(postRequestedFor(urlEqualTo(TEST_PATH)).withRequestBody(equalTo(BODY)));
  }

  @Test
  public void shouldSendSpecifiedMethodToServerWhenRunWithNonDefaultMethod() {
    job.setMethod(HttpMethod.PUT);
    job.run();
    verify(putRequestedFor(urlEqualTo(TEST_PATH)).withRequestBody(equalTo(BODY)));
  }

  @Test
  public void shouldSupportNotSpecifyingBodyWhenRun() {
    job.setBody(null);
    job.run();
    verify(postRequestedFor(urlEqualTo(TEST_PATH)).withRequestBody(equalTo("")));
  }

}
