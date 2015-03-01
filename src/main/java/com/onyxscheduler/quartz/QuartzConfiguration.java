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

package com.onyxscheduler.quartz;

import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Optional;
import java.util.Properties;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(QuartzProperties.class)
public class QuartzConfiguration {

  @Bean
  public JobFactory jobFactory() {
    return new AutowiringSpringBeanJobFactory();
  }

  @Bean
  public SchedulerFactoryBean quartzSchedulerFactory(JobFactory jobFactory,
                                                     Optional<DataSource> dataSource,
                                                     @Qualifier("quartzProperties") Properties quartzProperties) {
    SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
    schedulerFactoryBean.setJobFactory(jobFactory);
    schedulerFactoryBean.setDataSource(dataSource.orElse(null));
    schedulerFactoryBean.setQuartzProperties(quartzProperties);
    return schedulerFactoryBean;
  }

  @Profile("default")
  @Bean(name = "quartzProperties")
  public Properties quartzProperties(QuartzProperties quartzProperties) {
    return quartzProperties.buildQuartzProperties();
  }

  @Profile("mysql-jobstore")
  @Bean(name = "quartzProperties")
  public Properties quartzJobStoreProperties(QuartzProperties quartzProperties) {
    Properties props = quartzProperties.buildQuartzProperties();
    props.putAll(quartzProperties.getJobstore().buildQuartzProperties());
    return props;
  }

  @Configuration
  @Profile("mysql-jobstore")
  @Import(DataSourceAutoConfiguration.class)
  public static class QuartzJobStoreConfiguration {

  }

}
