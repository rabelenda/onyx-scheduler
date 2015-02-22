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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.onyxscheduler.util.TriggerTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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

    Job job = FakeJob.build();

    scheduler.scheduleJob(job);

    verify(quartzScheduler).scheduleJob(job.buildQuartzJobDetail(), job.buildQuartzTriggers(), false);
  }

  static class FakeJob extends com.onyxscheduler.domain.Job {

    private Set<Trigger> quartzTriggers;

    static FakeJob build() {
      FakeJob job = new FakeJob();
      job.setGroup(SchedulerTest.JOB_GROUP);
      job.setName(SchedulerTest.JOB_NAME);
      job.setTriggers(TriggerTestUtils.buildTriggers());
      return job;
    }

    @Override
    public Set<Trigger> buildQuartzTriggers() {
      //changed this method to always return the same triggers and allow easier verification of
      // expected triggers (otherwise new triggers are created that have different id)
      if (quartzTriggers == null) {
        quartzTriggers = super.buildQuartzTriggers();
      }
      return quartzTriggers;
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

  @Test
  public void shouldThrowDuplicateJobKeyWhenScheduleJobWithObjectAlreadyExistsQuartzScheduler()
    throws com.onyxscheduler.domain.Scheduler.DuplicateJobKeyException, SchedulerException {

    ObjectAlreadyExistsException cause = new ObjectAlreadyExistsException("test");
    doThrow(cause).when(quartzScheduler).scheduleJob(any(), any(), eq(false));

    expectedException.expect(com.onyxscheduler.domain.Scheduler.DuplicateJobKeyException.class);
    expectedException.expectCause(is(cause));

    scheduler.scheduleJob(FakeJob.build());
  }

  @Test
  public void shouldPropagateExceptionWhenScheduleJobWithFailingQuartzScheduler()
    throws com.onyxscheduler.domain.Scheduler.DuplicateJobKeyException, SchedulerException {

    SchedulerException cause = new SchedulerException();
    doThrow(cause).when(quartzScheduler).scheduleJob(any(), any(), eq(false));

    expectedException.expect(RuntimeException.class);
    expectedException.expectCause(is(cause));

    scheduler.scheduleJob(FakeJob.build());
  }

  @Test
  public void shouldGetJobKeysFromQuartzSchedulerWhenGetJobKeys() throws SchedulerException {
    String job1Group = "group1";
    String job2Group = "group2";
    ImmutableSet<org.quartz.JobKey> quartzJobKeys = ImmutableSet.of(
      new org.quartz.JobKey(JOB1_NAME, job1Group),
      new org.quartz.JobKey(JOB2_NAME, job2Group));
    when(quartzScheduler.getJobKeys(GroupMatcher.anyJobGroup())).thenReturn(quartzJobKeys);

    ImmutableSet<com.onyxscheduler.domain.JobKey> jobKeys = ImmutableSet.of(
      new com.onyxscheduler.domain.JobKey(job2Group, JOB2_NAME),
      new com.onyxscheduler.domain.JobKey(job1Group, JOB1_NAME));
    assertThat(scheduler.getJobKeys(), is(jobKeys));
  }

  @Test
  public void shouldPropagateExceptionWhenGetJobKeysWithFailingQuartzScheduler()
    throws SchedulerException {

    SchedulerException cause = new SchedulerException();
    doThrow(cause).when(quartzScheduler).getJobKeys(GroupMatcher.anyJobGroup());

    expectedException.expect(RuntimeException.class);
    expectedException.expectCause(is(cause));

    scheduler.getJobKeys();
  }

  @Test
  public void shouldGetJobKeysWithGroupMatcherFromQuartzSchedulerWhenGetJobKeysByGroup() throws
    SchedulerException {

    ImmutableSet<org.quartz.JobKey> quartzJobKeys = ImmutableSet.of(
      new org.quartz.JobKey(JOB1_NAME, JOB_GROUP),
      new org.quartz.JobKey(JOB2_NAME, JOB_GROUP));
    when(quartzScheduler.getJobKeys(GroupMatcher.jobGroupEquals(JOB_GROUP))).thenReturn(
      quartzJobKeys);

    ImmutableSet<com.onyxscheduler.domain.JobKey> jobKeys = ImmutableSet.of(
      new com.onyxscheduler.domain.JobKey(JOB_GROUP, JOB2_NAME),
      new com.onyxscheduler.domain.JobKey(JOB_GROUP, JOB1_NAME));
    assertThat(scheduler.getJobKeysByGroup(JOB_GROUP), is(jobKeys));
  }

  @Test
  public void shouldPropagateExceptionWhenGetJobKeysByGroupWithFailingQuartzScheduler()
    throws SchedulerException {

    SchedulerException cause = new SchedulerException();
    doThrow(cause).when(quartzScheduler).getJobKeys(GroupMatcher.jobGroupEquals(JOB_GROUP));

    expectedException.expect(RuntimeException.class);
    expectedException.expectCause(is(cause));

    scheduler.getJobKeysByGroup(JOB_GROUP);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldGetJobFromQuartzSchedulerWhenGetJob() throws SchedulerException {
    org.quartz.JobKey quartzJobKey = getQuartzJobKey();
    FakeJob job = FakeJob.build();
    setupQuartzSchedulerJobDetail(quartzJobKey, job);
    ImmutableList quartzTriggers = ImmutableList.copyOf(job.buildQuartzTriggers());
    when(quartzScheduler.getTriggersOfJob(quartzJobKey)).thenReturn(quartzTriggers);

    assertThat(scheduler.getJob(getJobKey()), is(Optional.of(job)));
  }

  private org.quartz.JobKey getQuartzJobKey() {
    return new org.quartz.JobKey(JOB_NAME, JOB_GROUP);
  }

  private void setupQuartzSchedulerJobDetail(org.quartz.JobKey quartzJobKey, FakeJob job)
    throws SchedulerException {
    when(quartzScheduler.getJobDetail(quartzJobKey)).thenReturn(job.buildQuartzJobDetail());
  }

  @Test
  public void shouldGetEmptyOptionalWhenGetJobWithUnknownJobKey() {
    assertThat(scheduler.getJob(getJobKey()),
      is(Optional.empty()));
  }

  private JobKey getJobKey() {
    return new JobKey(JOB_GROUP, JOB_NAME);
  }

  @Test
  public void shouldPropagateExceptionWhenGetJobWithFailingQuartzSchedulerGetJobDetail()
    throws SchedulerException {

    SchedulerException cause = new SchedulerException();
    doThrow(cause).when(quartzScheduler).getJobDetail(getQuartzJobKey());

    expectedException.expect(RuntimeException.class);
    expectedException.expectCause(is(cause));

    scheduler.getJob(getJobKey());
  }

  @Test
  public void shouldPropagateExceptionWhenGetJobWithFailingQuartzSchedulerGetTriggersOfJob()
    throws SchedulerException {

    org.quartz.JobKey quartzJobKey = getQuartzJobKey();
    FakeJob job = FakeJob.build();
    setupQuartzSchedulerJobDetail(quartzJobKey, job);
    SchedulerException cause = new SchedulerException();
    doThrow(cause).when(quartzScheduler).getTriggersOfJob(quartzJobKey);

    expectedException.expect(RuntimeException.class);
    expectedException.expectCause(is(cause));

    scheduler.getJob(getJobKey());
  }

  @Test
  public void shouldReturnTrueWhenDeleteJobWithQuartzSchedulerDeleteReturningTrue()
    throws SchedulerException {

    when(quartzScheduler.deleteJob(getQuartzJobKey())).thenReturn(true);

    assertThat(scheduler.deleteJob(getJobKey()), is(true));
  }

  @Test
  public void shouldReturnFalseWhenDeleteJobWithQuartzSchedulerDeleteReturningFalse()
    throws SchedulerException {

    when(quartzScheduler.deleteJob(getQuartzJobKey())).thenReturn(false);

    assertThat(scheduler.deleteJob(getJobKey()), is(false));
  }

  @Test
  public void shouldPropagateExceptionWhenDeleteJobWithFailingQuartzSchedulerDelete()
    throws SchedulerException {

    SchedulerException cause = new SchedulerException();
    when(quartzScheduler.deleteJob(getQuartzJobKey())).thenThrow(cause);

    expectedException.expect(RuntimeException.class);
    expectedException.expectCause(is(cause));

    scheduler.deleteJob(getJobKey());
  }

}
