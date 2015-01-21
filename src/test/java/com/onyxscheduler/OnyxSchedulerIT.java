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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.onyxscheduler.util.TriggerTestUtils.CRON;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = OnyxSchedulerApplication.class)
@WebAppConfiguration
@IntegrationTest({"server.port=0"})
public class OnyxSchedulerIT {

  //TODO change this into a single smoke test and move all the tests to SchedulerIT

  public static final String PORT_TEMPLATE_PROPERTY_KEY = "port";
  public static final String JOB_NAME_TEMPLATE_PROPERTY_KEY = "jobName";
  public static final String CRON_TEMPLATE_PROPERTY_KEY = "cron";
  public static final String FIXED_DATE_TEMPLATE_PROPERTY_KEY = "fixedDate";

  private static final long VERIFYING_POLL_PERIOD_IN_MILLIS = 500;
  private static final long VERIFYING_POLL_TIMEOUT_IN_MILLIS = 5000;
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

  private RestTemplate restTemplate;

  private String appUrl;

  @SuppressWarnings("SpringJavaAutowiredMembersInspection")
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

  private void pollingVerify(Runnable verify) {
    AssertionError lastVerification;
    Stopwatch watch = Stopwatch.createStarted();
    do {
      try {
        lastVerification = null;
        verify.run();
      } catch (AssertionError e) {
        lastVerification = e;
        try {
          Thread.sleep(VERIFYING_POLL_PERIOD_IN_MILLIS);
        } catch (InterruptedException e1) {
          Throwables.propagate(e1);
        }
      }
    } while (watch.elapsed(TimeUnit.MILLISECONDS) < VERIFYING_POLL_TIMEOUT_IN_MILLIS);
    if (lastVerification != null) {
      throw lastVerification;
    }
  }

  private String getUrlFromJobName(String jobName) {
    return "/" + jobName;
  }

  private String buildFixedTimeJobRequestBodyFromNameAndFixedTime(String jobName, Date fixedDate) {
    ImmutableMap<String, Serializable> model = ImmutableMap.of(PORT_TEMPLATE_PROPERTY_KEY,
      wireMockRule.port(),
      JOB_NAME_TEMPLATE_PROPERTY_KEY,
      jobName,
      FIXED_DATE_TEMPLATE_PROPERTY_KEY,
      fixedDate);
    return solveTemplate("jobWithFixedDate.json.ftl", model);
  }

  private String solveTemplate(String templateName, ImmutableMap<String, Serializable> model) {
    try {
      Template template = freemarkerConfig.getTemplate(templateName);
      return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    } catch (IOException | TemplateException e) {
      throw Throwables.propagate(e);
    }
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
  public void shouldFireHttpRequestWithReachableFutureDate() {
    String jobName = "reachableFuture";
    String requestBody =
      buildFixedTimeJobRequestBodyFromNameAndFixedTime(jobName, buildReachableFutureDate());
    ResponseEntity<String> response = scheduleJob(requestBody);

    assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
    pollingVerifyJobRequest(jobName);
  }

  private Date buildReachableFutureDate() {
    return toDate(LocalDateTime.now().plusSeconds(3));
  }

  @Test
  public void shouldNotFireHttpRequestWithNonReachableFutureDate() {
    String jobName = "nonReachableFuture";
    String requestBody =
      buildFixedTimeJobRequestBodyFromNameAndFixedTime(jobName, buildNonReachableFutureDate());
    ResponseEntity<String> response = scheduleJob(requestBody);

    assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
    try {
      pollingVerifyJobRequest(jobName);
      fail("Received request when it should not trigger");
    } catch (VerificationException ignored) {

    }
  }

  private Date buildNonReachableFutureDate() {
    return toDate(LocalDateTime.now().plusMinutes(1));
  }

  @Test
  public void shouldFireMultipleHttpRequestsWithCron() {
    String jobName = "cron";
    String requestBody = buildCronJobRequestBodyFromNameAndCronExpression(jobName, CRON);
    ResponseEntity<String> response = scheduleJob(requestBody);

    assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
    pollingVerifyJobRequestWithAtLeastCount(2, jobName);
  }

  private void pollingVerifyJobRequestWithAtLeastCount(int count, String jobName) {
    pollingVerify(() -> assertThat(findAll(getRequestedFor(urlEqualTo(getUrlFromJobName(jobName)))),
      hasSize(greaterThanOrEqualTo(count))));
  }

  private String buildCronJobRequestBodyFromNameAndCronExpression(String jobName, String cron) {
    ImmutableMap<String, Serializable> model = ImmutableMap.of(PORT_TEMPLATE_PROPERTY_KEY,
      wireMockRule.port(),
      JOB_NAME_TEMPLATE_PROPERTY_KEY,
      jobName,
      CRON_TEMPLATE_PROPERTY_KEY,
      cron);
    return solveTemplate("jobWithCron.json.ftl", model);
  }

}
