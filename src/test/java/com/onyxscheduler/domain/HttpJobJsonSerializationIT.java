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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onyxscheduler.OnyxSchedulerApplication;
import com.onyxscheduler.util.TriggerTestUtils;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import static com.onyxscheduler.util.FreemarkerUtils.solveTemplate;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@SpringApplicationConfiguration(classes = OnyxSchedulerApplication.class)
public class HttpJobJsonSerializationIT {

  @Autowired
  protected MappingJackson2HttpMessageConverter converter;

  @Autowired
  private freemarker.template.Configuration freemarkerConfig;

  @Value("httpJobWithMinimumParametersAndFixedDate.json")
  private Resource minimumParamsAndFixedDateJsonFile;

  @Value("httpJobWithNonDefaultParametersAndCron.json")
  private Resource nonDefaultParamsAndCronJsonFile;

  @Test
  public void shouldGetJobWithAllSpecifiedParametersWhenLoadFromJsonWithNonDefaultParameters()
      throws IOException {
    Job loadedJob = loadJobFromJsonFile(nonDefaultParamsAndCronJsonFile);
    HttpJob expectedJob = buildJobWithNonDefaultParameters(loadedJob.getId());
    assertThat(loadedJob, is(expectedJob));
  }

  private HttpJob buildJobWithNonDefaultParameters(UUID id) throws MalformedURLException {
    HttpJob expectedJob = new HttpJob();
    expectedJob.setId(id);
    expectedJob.setName("httpJobWithNonDefaultParams");
    //This is just to avoid a different ID to make the assert fail
    expectedJob.setUrl(new URL("http://localhost/test"));
    expectedJob.setMethod(HttpMethod.PUT);
    expectedJob.setHeaders(ImmutableMap.of("Content-Type", "application/json"));
    expectedJob.setBody("{\"field\":\"value\"}");
    expectedJob.setTriggers(ImmutableSet.of(TriggerTestUtils.buildTriggerWithCron()));
    return expectedJob;
  }

  private Job loadJobFromJsonFile(Resource file) throws IOException {
    return converter.getObjectMapper()
        .readValue(file.getInputStream(), Job.class);
  }

  @Test
  public void shouldGetJobWithDefaultParametersWhenLoadFromJsonWithMinimumParameters()
      throws IOException {
    Job loadedJob = loadJobFromJsonFile(minimumParamsAndFixedDateJsonFile);
    HttpJob expectedJob = buildJobWithDefaultParameters(loadedJob.getId());
    assertThat(loadedJob, is(expectedJob));
  }

  private HttpJob buildJobWithDefaultParameters(UUID id) throws MalformedURLException {
    HttpJob expectedJob = new HttpJob();
    expectedJob.setId(id);
    expectedJob.setName("httpJobWithMinimumParamsAndFixedDate");
    //This is just to avoid a different ID to make the assert fail
    expectedJob.setUrl(new URL("http://localhost/test"));
    expectedJob.setTriggers(ImmutableSet.of(TriggerTestUtils.buildTriggerWithFixedTime()));
    return expectedJob;
  }

  @Test
  public void shouldGetExpectedJsonWithAllSpecifiedParametersWhenWritingToJson()
      throws IOException, JSONException {
    UUID jobId = UUID.randomUUID();
    HttpJob job = buildJobWithNonDefaultParameters(jobId);
    JSONAssert
        .assertEquals(
            solveTemplate(freemarkerConfig, "httpJobWithNonDefaultParametersAndCron.json.ftl",
                          ImmutableMap.<String, Serializable>of("jobId", jobId)),
            buildJsonFromJob(job),
            true);
  }

  private String buildJsonFromJob(HttpJob httpJob) throws JsonProcessingException {
    return converter.getObjectMapper().writeValueAsString(httpJob);
  }

  @Test
  public void shouldGetExpectedJsonWhenWritingToJsonWithDefaultParameters()
      throws IOException, JSONException {
    UUID jobId = UUID.randomUUID();
    HttpJob job = buildJobWithDefaultParameters(jobId);
    JSONAssert
        .assertEquals(solveTemplate(freemarkerConfig,
                                    "httpJobWithMinimumParametersAndFixedDateWithId.json.ftl",
                                    ImmutableMap.<String, Serializable>of("jobId", jobId)),
                      buildJsonFromJob(job),
                      true);
  }

}
