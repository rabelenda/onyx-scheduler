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

package com.onyxscheduler;

import com.google.common.collect.ImmutableMap;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;

import freemarker.template.Configuration;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.onyxscheduler.util.FreemarkerUtils.solveTemplate;
import static com.onyxscheduler.util.PollingVerifier.pollingVerify;
import static com.onyxscheduler.util.TriggerTestUtils.CRON;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = OnyxSchedulerApplication.class, webEnvironment=SpringBootTest.WebEnvironment.DEFINED_PORT)
@WebAppConfiguration
public class OnyxSchedulerIT {

  public static final String PORT_TEMPLATE_PROPERTY_KEY = "port";
  public static final String JOB_NAME_TEMPLATE_PROPERTY_KEY = "jobName";
  public static final String CRON_TEMPLATE_PROPERTY_KEY = "cron";
  public static final String FIXED_DATE_TEMPLATE_PROPERTY_KEY = "fixedDate";

  public static final String ONYX_TEST_GROUP_JOBS_PATH = "/onyx/groups/test/jobs";

  @Value("${local.server.port}")
  int appPort;

  @Value("${security.user.name}")
  String username;

  @Value("${security.user.password}")
  String password;

  @ClassRule
  public static WireMockClassRule wireMockClassRule = new WireMockClassRule(0);

  @Rule
  public WireMockClassRule wireMockRule = wireMockClassRule;

  private TestRestTemplate restTemplate;

  private String appUrl;

  @Autowired
  private Configuration freemarkerConfig;

  @Before
  public void setup() {
    appUrl = "http://localhost:" + appPort;
    stubFor(any(urlMatching(".*")).willReturn(aResponse()));
    restTemplate = new TestRestTemplate(username, password);
  }

  @Test
  public void shouldFireHttpRequestWithPastDate() {
    String jobName = "pastDate";
    String requestBody = buildFixedTimeJobRequestBodyFromNameAndFixedTime(jobName, buildPastDate());
    ResponseEntity<String> response = scheduleJob(requestBody);

    /* this assert is a fail fast mechanism in case the response is not the expected one to avoid
       waiting for an http request that will never arrive, same applies to similar asserts. This
       has not been added to pollingVerifyRequest and by doing so don't duplicate code since is
       outside of the scope of the method to do such assert.
      */
    assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
    pollingVerifyJobRequest(jobName);
  }

  private void pollingVerifyJobRequest(String jobName) {
    pollingVerify(() -> verify(getRequestedFor(urlEqualTo(getUrlFromJobName(jobName)))));
  }

  private String getUrlFromJobName(String jobName) {
    return "/" + jobName;
  }

  private String buildFixedTimeJobRequestBodyFromNameAndFixedTime(String jobName, Date fixedDate) {
    ImmutableMap<String, Serializable> model = ImmutableMap.<String, Serializable>builder()
        .put(PORT_TEMPLATE_PROPERTY_KEY, wireMockRule.port())
        .put(JOB_NAME_TEMPLATE_PROPERTY_KEY, jobName)
        .put(FIXED_DATE_TEMPLATE_PROPERTY_KEY, fixedDate)
        .build();
    return solveTemplate(freemarkerConfig, "jobWithFixedDate.json.ftl", model);
  }

  private Date buildPastDate() {
    return toDate(LocalDateTime.now().minusMinutes(1));
  }

  private Date toDate(LocalDateTime localDateTime) {
    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
  }

  private ResponseEntity<String> scheduleJob(String requestBody) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return restTemplate.postForEntity(appUrl + ONYX_TEST_GROUP_JOBS_PATH,
                                      new HttpEntity<>(requestBody, headers),
                                      String.class);
  }

  @Test
  public void shouldFireMultipleHttpPostsWithCron() {
    String jobName = "cron";
    String requestBody = buildCronJobRequestBodyFromNameAndCronExpression(jobName, CRON);
    ResponseEntity<String> response = scheduleJob(requestBody);

    assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
    pollingVerifyJobRequestWithAtLeastCount(2, jobName);
  }

  private void pollingVerifyJobRequestWithAtLeastCount(int count, String jobName) {
    pollingVerify(() -> assertThat(findAll(postRequestedFor(urlEqualTo(getUrlFromJobName(jobName)
                                   )).withRequestBody(equalTo("{\"field\":\"value\"}"))
                                               .withHeader(HttpHeaders.CONTENT_TYPE,
                                                           equalTo(
                                                               MediaType.APPLICATION_JSON_VALUE))),
                                   hasSize(greaterThanOrEqualTo(count))));
  }

  private String buildCronJobRequestBodyFromNameAndCronExpression(String jobName, String cron) {
    ImmutableMap<String, Serializable> model = ImmutableMap.<String, Serializable>builder()
        .put(PORT_TEMPLATE_PROPERTY_KEY, wireMockRule.port())
        .put(JOB_NAME_TEMPLATE_PROPERTY_KEY, jobName)
        .put(CRON_TEMPLATE_PROPERTY_KEY, cron)
        .build();
    return solveTemplate(freemarkerConfig, "jobWithCronAndHttpPost.json.ftl", model);
  }

}
