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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.onyxscheduler.quartz.AutowiringSpringBeanJobFactory;
import org.quartz.spi.JobFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class OnyxSchedulerApplication {

  public static void main(String[] args) {
    SpringApplication.run(OnyxSchedulerApplication.class, args);
  }

  @Bean
  public JobFactory jobFactory() {
    return new AutowiringSpringBeanJobFactory();
  }

  @Bean
  public SchedulerFactoryBean quartzSchedulerFactory(JobFactory jobFactory) {
    SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
    schedulerFactoryBean.setJobFactory(jobFactory);
    return schedulerFactoryBean;
  }

  /* since there is no way to set serializationInclusion through spring boot application.yml we
  need to define a custom object mapper, and due to this all other spring boot jackson settings
  are not able to be used through application.yml
    */
  @Bean
  public MappingJackson2HttpMessageConverter jacksonConverter() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.setDateFormat(new ISO8601DateFormat());
    return new MappingJackson2HttpMessageConverter(mapper);
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

}
