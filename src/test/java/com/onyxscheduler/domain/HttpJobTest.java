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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.google.common.base.Throwables;
import java.net.*;
import org.junit.Test;
import org.quartz.JobDataMap;
import org.springframework.http.HttpMethod;

public class HttpJobTest extends JobTest {
  private static final URL URL = buildUrl();
  private static final HttpMethod METHOD = HttpMethod.PUT;
  private static final String BODY = "Testing body";

  private static URL buildUrl() {
    try {
      return new URL("http://test.com");
    } catch (MalformedURLException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  protected Job buildJob() {
    HttpJob job = new HttpJob();
    job.setUrl(URL);
    job.setMethod(METHOD);
    job.setBody(BODY);
    return job;
  }

  @Override
  protected JobDataMap buildJobDataMap() {
    JobDataMap data = new JobDataMap();
    data.put(HttpJob.URL_DATAMAP_KEY, URL.toString());
    data.put(HttpJob.METHOD_DATAMAP_KEY, METHOD.toString());
    data.put(HttpJob.BODY_DATAMAP_KEY, BODY);
    return data;
  }

  @Override
  protected void assertConcreteProperties(Job job) {
    HttpJob httpJob = (HttpJob) job;
    assertThat(httpJob.getUrl(), is(URL));
    assertThat(httpJob.getMethod(), is(METHOD));
    assertThat(httpJob.getBody(), is(BODY));
  }

  @Test
  public void shouldUsePostMethodByDefault() {
    assertThat(new HttpJob().getMethod(), is(HttpMethod.POST));
  }
}
