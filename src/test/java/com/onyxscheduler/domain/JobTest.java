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
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import com.onyxscheduler.util.TriggerTestUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

public abstract class JobTest {

  private static final String JOB_NAME = "name";
  private static final String JOB_GROUP = "group";

  private Job job;

  @Before
  public void setup() {
    job = buildJob();
    job.setName(JOB_NAME);
    job.setGroup(JOB_GROUP);
  }

  protected abstract Job buildJob();

  @Test
  public void shouldGetQuartzTriggersFromJobTriggersWhenBuildQuartzTriggers() {
    Trigger trigger1 = Mockito.mock(Trigger.class);
    Trigger trigger2 = Mockito.mock(Trigger.class);
    org.quartz.Trigger quartzTrigger1 = Mockito.mock(org.quartz.Trigger.class);
    org.quartz.Trigger quartzTrigger2 = Mockito.mock(org.quartz.Trigger.class);

    when(trigger1.buildQuartzTrigger()).thenReturn(quartzTrigger1);
    when(trigger2.buildQuartzTrigger()).thenReturn(quartzTrigger2);
    job.setTriggers(ImmutableSet.of(trigger1, trigger2));

    assertThat(job.buildQuartzTriggers(), is(ImmutableSet.of(quartzTrigger1, quartzTrigger2)));
  }

  @Test
  public void shouldGetQuartzJobDetailWithProperKeyWhenBuildQuartzJobDetail() {
    assertThat(job.buildQuartzJobDetail().getKey(), is(new org.quartz.JobKey(JOB_NAME, JOB_GROUP)));
  }

  @Test
  public void shouldGetQuartzJobDetailWithConcreteJobDataWhenBuildQuartzJobDetail() {
    assertThat(job.buildQuartzJobDetail().getJobDataMap(), is(buildJobDataMap()));
  }

  protected abstract JobDataMap buildJobDataMap();

  @Test
  public void shouldGetJobWithProperGroupAndNameWhenBuildQuartzJobDetail() {
    Job job = Job.fromQuartzJobDetailAndTriggers(buildJobDetail(), TriggerTestUtils.buildQuartzTriggers());
    assertThat(job.getName(), is(JOB_NAME));
    assertThat(job.getGroup(), is(JOB_GROUP));
  }

  private JobDetail buildJobDetail() {
    return JobBuilder.newJob(job.getClass())
      .withIdentity(JOB_NAME, JOB_GROUP)
      .setJobData(buildJobDataMap())
      .build();
  }

  @Test
  public void shouldGetJobWithProperTriggersWhenBuildQuartzJobDetail() {
    Job job = Job.fromQuartzJobDetailAndTriggers(buildJobDetail(), TriggerTestUtils.buildQuartzTriggers());
    assertThat(job.getTriggers(), Matchers.is(TriggerTestUtils.buildTriggers()));
  }

  @Test
  public void shouldGetJobWithInitializedConcretePropertiesWhenBuildQuartzJobDetail() {
    Job job = Job.fromQuartzJobDetailAndTriggers(buildJobDetail(), TriggerTestUtils.buildQuartzTriggers());
    assertConcreteProperties(job);
  }

  protected abstract void assertConcreteProperties(Job job);

}
