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
import java.net.*;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class HttpJobTest extends JobTest {
  private static final URL URL = buildUrl();

  private static URL buildUrl() {
    try {
      return new URL("http://test.com");
    } catch (MalformedURLException e) {
      throw Throwables.propagate(e);
    }
  }

  @Test
  public void shouldGetSameJobWhenBuildingJobBackFromGeneratedJobDetailWithOnlyUrl() {
    HttpJob job = new HttpJob();
    job.setUrl(URL);
    verifyGettingSameJobWhenBuildingJobBackFromGeneratedJobDetail(job);
  }

  @Test
  public void shouldGetSameJobWhenBuildingJobBackFromGeneratedJobDetailWithNoDefaultMethod() {
    HttpJob job = new HttpJob();
    job.setUrl(URL);
    job.setMethod(HttpMethod.GET);
    verifyGettingSameJobWhenBuildingJobBackFromGeneratedJobDetail(job);
  }

  @Test
  public void shouldGetSameJobWhenBuildingJobBackFromGeneratedJobDetailWithBody() {
    HttpJob job = new HttpJob();
    job.setUrl(URL);
    job.setBody("test");
    verifyGettingSameJobWhenBuildingJobBackFromGeneratedJobDetail(job);
  }

  @Test
  public void shouldGetSameJobWhenBuildingJobBackFromGeneratedJobDetailWithHeaders() {
    HttpJob job = new HttpJob();
    job.setUrl(URL);
    job.setHeaders(ImmutableMap.of("h1", "v1", "h2", "v2"));
    verifyGettingSameJobWhenBuildingJobBackFromGeneratedJobDetail(job);
  }

}
