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

package com.onyxscheduler.web;

import com.onyxscheduler.OnyxSchedulerApplication;
import com.onyxscheduler.domain.Scheduler;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = OnyxSchedulerApplication.class, webEnvironment=SpringBootTest.WebEnvironment.DEFINED_PORT)
@WebAppConfiguration
public class JobControllerIT {
  //TODO verify proper handling of exceptions, validations, and expected responses (status and body)

  @Value("${local.server.port}")
  int appPort;

  @Value("${security.user.name}")
  String username;

  @Value("${security.user.password}")
  String password;

  private TestRestTemplate restTemplate;

  private String appUrl;

  @Bean
  public Scheduler scheduler() {
    return Mockito.mock(Scheduler.class);
  }

  @Before
  public void setup() {
    appUrl = "http://localhost:" + appPort;
    restTemplate = new TestRestTemplate(username, password);
  }
}
