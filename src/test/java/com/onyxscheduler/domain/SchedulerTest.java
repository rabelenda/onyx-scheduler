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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.onyxscheduler.util.TriggerTestUtils;
import java.util.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.matchers.GroupMatcher;

@RunWith(MockitoJUnitRunner.class)
public class SchedulerTest {

  public static final String JOB_GROUP = "group";
  public static final String JOB_NAME = "name";
  public static final String JOB1_NAME = "name1";
  public static final String JOB2_NAME = "name2";

  @Mock
  private org.quartz.Scheduler quartzScheduler;

  @InjectMocks
  private com.onyxscheduler.domain.Scheduler scheduler;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void shouldScheduleJobInQuartzWhenScheduleJob()
    throws com.onyxscheduler.domain.Scheduler.DuplicateJobKeyException, SchedulerException {
    JobDetail jobDetail = new JobDetailImpl();
    Set<org.quartz.Trigger> quartzTriggers = TriggerTestUtils.buildQuartzTriggers();
    com.onyxscheduler.domain.Job job = buildJobToScheduleFromJobDetailAndQuartzTriggers(jobDetail, quartzTriggers);

    scheduler.scheduleJob(job);

    verify(quartzScheduler).scheduleJob(jobDetail, quartzTriggers, false);
  }

  private com.onyxscheduler.domain.Job buildJobToScheduleFromJobDetailAndQuartzTriggers(JobDetail jobDetail1,
    Set<org.quartz.Trigger> quartzTriggers1) {
    com.onyxscheduler.domain.Job job = Mockito.mock(com.onyxscheduler.domain.Job.class);
    when(job.buildQuartzJobDetail()).thenReturn(jobDetail1);
    when(job.buildQuartzTriggers()).thenReturn(quartzTriggers1);
    when(job.getGroup()).thenReturn(JOB_GROUP);
    when(job.getName()).thenReturn(JOB_NAME);
    return job;
  }

  @Test
  public void shouldThrowDuplicateJobKeyWhenScheduleJobWithObjectAlreadyExistsQuartzScheduler()
    throws com.onyxscheduler.domain.Scheduler.DuplicateJobKeyException, SchedulerException {
    ObjectAlreadyExistsException cause = new ObjectAlreadyExistsException("test");
    doThrow(cause).when(quartzScheduler).scheduleJob(any(), any(), eq(false));
    expectedException.expect(com.onyxscheduler.domain.Scheduler.DuplicateJobKeyException.class);
    expectedException.expectCause(is(cause));

    scheduler.scheduleJob(buildJobToSchedule());
  }

  private com.onyxscheduler.domain.Job buildJobToSchedule() {
    return buildJobToScheduleFromJobDetailAndQuartzTriggers(new JobDetailImpl(),
      TriggerTestUtils.buildQuartzTriggers());
  }

  @Test
  public void shouldPropagateExceptionWhenScheduleJobWithFailingQuartzScheduler()
    throws com.onyxscheduler.domain.Scheduler.DuplicateJobKeyException, SchedulerException {
    SchedulerException cause = buildSchedulerException();
    doThrow(cause).when(quartzScheduler).scheduleJob(any(), any(), eq(false));
    expectedException.expect(RuntimeException.class);
    expectedException.expectCause(is(cause));

    scheduler.scheduleJob(buildJobToSchedule());
  }

  private SchedulerException buildSchedulerException() {
    return Mockito.mock(SchedulerException.class);
  }

  @Test
  public void shouldGetJobKeysFromQuartzSchedulerWhenGetJobKeys() throws SchedulerException {
    String job1Group = "group1";
    String job2Group = "group2";
    ImmutableSet<org.quartz.JobKey> quartzJobKeys =
      ImmutableSet.of(new org.quartz.JobKey(JOB1_NAME, job1Group),
        new org.quartz.JobKey(JOB2_NAME, job2Group));
    ImmutableSet<com.onyxscheduler.domain.JobKey> jobKeys =
      ImmutableSet.of(new com.onyxscheduler.domain.JobKey(job2Group, JOB2_NAME), new com.onyxscheduler.domain.JobKey(job1Group, JOB1_NAME));
    when(quartzScheduler.getJobKeys(GroupMatcher.anyJobGroup())).thenReturn(quartzJobKeys);
    assertThat(scheduler.getJobKeys(), is(jobKeys));
  }

  @Test
  public void shouldPropagateExceptionWhenGetJobKeysWithFailingQuartzScheduler()
    throws SchedulerException {
    SchedulerException cause = buildSchedulerException();
    doThrow(cause).when(quartzScheduler).getJobKeys(GroupMatcher.anyJobGroup());
    expectedException.expect(RuntimeException.class);
    expectedException.expectCause(is(cause));

    scheduler.getJobKeys();
  }

  @Test
  public void shouldGetJobKeysFromQuartzSchedulerWhenGetJobKeysByGroup() throws SchedulerException {
    ImmutableSet<org.quartz.JobKey> quartzJobKeys =
      ImmutableSet.of(new org.quartz.JobKey(JOB1_NAME, JOB_GROUP),
        new org.quartz.JobKey(JOB2_NAME, JOB_GROUP));
    ImmutableSet<com.onyxscheduler.domain.JobKey> jobKeys =
      ImmutableSet.of(new com.onyxscheduler.domain.JobKey(JOB_GROUP, JOB2_NAME), new com.onyxscheduler.domain.JobKey(JOB_GROUP, JOB1_NAME));
    when(quartzScheduler.getJobKeys(GroupMatcher.jobGroupEquals(JOB_GROUP))).thenReturn(
      quartzJobKeys);
    assertThat(scheduler.getJobKeysByGroup(JOB_GROUP), is(jobKeys));
  }

  @Test
  public void shouldPropagateExceptionWhenGetJobKeysByGroupWithFailingQuartzScheduler()
    throws SchedulerException {
    SchedulerException cause = buildSchedulerException();
    doThrow(cause).when(quartzScheduler).getJobKeys(GroupMatcher.jobGroupEquals(JOB_GROUP));
    expectedException.expect(RuntimeException.class);
    expectedException.expectCause(is(cause));

    scheduler.getJobKeysByGroup(JOB_GROUP);
  }

  @Test
  public void shouldGetJobFromQuartzSchedulerWhenGetJob() throws SchedulerException {
    setupQuartzSchedulerGetJobDetailWithFakeJob();
    List quartzTriggersList = ImmutableList.copyOf(TriggerTestUtils.buildQuartzTriggers());
    when(quartzScheduler.getTriggersOfJob(new org.quartz.JobKey(JOB_NAME, JOB_GROUP))).thenReturn(
      quartzTriggersList);


    FakeJob job = FakeJob.fromJobKeyAndTriggers(new com.onyxscheduler.domain.JobKey(JOB_GROUP, JOB_NAME), TriggerTestUtils


      .buildTriggers());

    assertThat(scheduler.getJob(new com.onyxscheduler.domain.JobKey(JOB_GROUP, JOB_NAME)), is(Optional.of(job)));
  }

  private void setupQuartzSchedulerGetJobDetailWithFakeJob() throws SchedulerException {
    JobDetail jobDetail =
      JobBuilder.newJob(FakeJob.class).withIdentity(JOB_NAME, JOB_GROUP).build();
    when(quartzScheduler.getJobDetail(new org.quartz.JobKey(JOB_NAME, JOB_GROUP))).thenReturn(
      jobDetail);
  }

  @Test
  public void shouldGetEmptyOptionalWhenGetJobWithUnknownJobKey() {
    assertThat(scheduler.getJob(new com.onyxscheduler.domain.JobKey(JOB_GROUP, JOB_NAME)), is(Optional.empty()));
  }

  @Test
  public void shouldPropagateExceptionWhenGetJobWithFailingQuartzSchedulerGetJobDetail()
    throws SchedulerException {
    SchedulerException cause = buildSchedulerException();
    doThrow(cause).when(quartzScheduler).getJobDetail(new org.quartz.JobKey(JOB_NAME, JOB_GROUP));
    expectedException.expect(RuntimeException.class);
    expectedException.expectCause(is(cause));

    scheduler.getJob(new com.onyxscheduler.domain.JobKey(JOB_GROUP, JOB_NAME));
  }

  @Test
  public void shouldPropagateExceptionWhenGetJobWithFailingQuartzSchedulerGetTriggersOfJob()
    throws SchedulerException {
    setupQuartzSchedulerGetJobDetailWithFakeJob();
    SchedulerException cause = buildSchedulerException();
    doThrow(cause).when(quartzScheduler)
      .getTriggersOfJob(new org.quartz.JobKey(JOB_NAME, JOB_GROUP));
    expectedException.expect(RuntimeException.class);
    expectedException.expectCause(is(cause));

    scheduler.getJob(new com.onyxscheduler.domain.JobKey(JOB_GROUP, JOB_NAME));
  }

  static class FakeJob extends com.onyxscheduler.domain.Job {

    static FakeJob fromJobKeyAndTriggers(com.onyxscheduler.domain.JobKey jobKey, Set<com.onyxscheduler.domain.Trigger> triggers) {
      FakeJob job = new FakeJob();
      job.setGroup(jobKey.getGroup());
      job.setName(jobKey.getName());
      job.setTriggers(triggers);
      return job;
    }

    @Override
    protected Map<String, Object> buildDataMap() {
      return Collections.emptyMap();
    }

    @Override
    protected void initFromDataMap(Map<String, Object> dataMap) {
    }

    @Override
    public void run() {
    }

    @Override
    public int hashCode() {
      return Objects.hash(group, name, triggers);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final FakeJob other = (FakeJob) obj;
      return Objects.equals(this.group, other.group) && Objects.equals(this.name, other.name)
        && Objects.equals(this.triggers, other.triggers);
    }

    @Override
    public String toString() {
      return "FakeJob{" +
        "group='" + group + '\'' +
        ", name='" + name + '\'' +
        ", triggers=" + triggers +
        '}';
    }
  }

}
