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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Throwables;
import java.util.*;
import java.util.stream.*;
import javax.validation.*;
import org.hibernate.validator.constraints.NotEmpty;
import org.quartz.*;
import org.quartz.JobKey;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Jobs are the basic unit of work of onyx scheduler. They host the logic to be executed once the
 * given triggers are fired and required configuration to be able to serialize/de-serialize these
 * jobs from/to JSON.
 * <p/>
 * Additionally they currently have the logic for handling the interaction with underlying quartz
 * creating jobs and triggers from jobDetails and quartz Triggers, and vice versa.
 * <p/>
 * When creating a new Job type (for example if you want some job for queuing in RabbitMQ or
 * whatever) you just have to extend this class implementing the run method and add the
 * appropriate JsonSubTypes.Type configuration in this file mapping the class to the appropriate
 * value of type field of the JSON representation.
 * <p/>
 * As they currently host interaction with quartz Jobs, they need to provide proper
 * buildQuartzJobDetail implementation to be able to restore all needed configuration needed by
 * run and parsed by initFromQuartzJobDataMap.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = HttpJob.class, name = "http")})
public abstract class Job extends QuartzJobBean implements Runnable {
  protected String name;
  protected String group;
  @NotEmpty
  @Valid
  protected Set<Trigger> triggers;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public Set<Trigger> getTriggers() {
    return triggers;
  }

  public void setTriggers(Set<Trigger> triggers) {
    this.triggers = triggers;
  }

  public Set<org.quartz.Trigger> buildQuartzTriggers() {
    return triggers.stream().
      map(Trigger::buildQuartzTrigger).
      collect(Collectors.toSet());
  }

  public JobDetail buildQuartzJobDetail() {
    return org.quartz.JobBuilder.newJob(getClass())
      .withIdentity(name, group)
      .usingJobData(buildQuartzJobDataMap())
      .build();
  }

  protected abstract JobDataMap buildQuartzJobDataMap();

  public static Job fromQuartzJobDetailAndTriggers(JobDetail jobDetail,
    Set<? extends org.quartz.Trigger> triggers) {
    try {
      Job job = (Job) jobDetail.getJobClass().newInstance();
      JobKey jobKey = jobDetail.getKey();
      job.setName(jobKey.getName());
      job.setGroup(jobKey.getGroup());
      job.setTriggers(triggers.stream()
        .map(Trigger::fromQuartzTrigger)
        .collect(Collectors.toSet()));
      job.initFromQuartzJobDataMap(jobDetail.getJobDataMap());
      return job;
    } catch (InstantiationException | IllegalAccessException e) {
      throw Throwables.propagate(e);
    }
  }

  protected abstract void initFromQuartzJobDataMap(JobDataMap jobDataMap);

  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    run();
  }

}
