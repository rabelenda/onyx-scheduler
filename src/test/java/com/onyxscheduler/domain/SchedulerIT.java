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

import com.google.common.collect.ImmutableSet;

import com.onyxscheduler.OnyxSchedulerApplication;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test the proper working of the scheduler component in conjunction with quartz library. Basically
 * test that quartz library is being properly used and expected behavior.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@SpringBootTest(classes = OnyxSchedulerApplication.class)
public class SchedulerIT {

  public static final int FIRE_THRESHOLD_TIMEOUT_IN_MILLIS = 5000;
  public static final int PAST_DATE_MINUTES = 1;
  public static final int NON_REACHABLE_FUTURE_MINUTES = 1;
  public static final int REACHABLE_FUTURE_SECONDS = 3;
  public static final String JOB_GROUP = "group";
  public static final String JOB_NAME = "name";
  public static final String EVERY_2_SECONDS_CRON_EXPRESSION = "0/2 * * * * ?";
  /* this threshold is required since job might have already updated the countdownlatch of the
  fakejob but still not ended its execution or updating internal information */
  private static final long FIRED_JOB_AWAIT_THRESHOLD_IN_MILLIS = 500;


  @Autowired
  Scheduler scheduler;

  @Autowired
  org.quartz.Scheduler quartzScheduler;

  @Autowired
  LatchProvider latchProvider;

  @Bean
  public LatchProvider latchProvider() {
    return new LatchProvider();
  }

  public static class LatchProvider {

    private CountDownLatch latch;

    public void setLatch(CountDownLatch latch) {
      this.latch = latch;
    }

    public CountDownLatch getLatch() {
      return latch;
    }
  }

  @Before
  public void setup() throws SchedulerException {
        /* Preferred accessing quartzScheduler here to do cleanup instead of implementing a method in scheduler just
        used for testing */
    quartzScheduler.clear();
  }

  @Test(timeout = FIRE_THRESHOLD_TIMEOUT_IN_MILLIS)
  public void shouldFireJobOnceWithPastDate()
      throws Scheduler.DuplicateJobKeyException, InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    latchProvider.setLatch(latch);
    CountDownJob existingJob = buildJobWithFixedDateTrigger(buildPastDate());
    scheduler.scheduleJob(existingJob);
    latch.await();
  }

  private Instant buildPastDate() {
    return Instant.now().minus(PAST_DATE_MINUTES, ChronoUnit.MINUTES);
  }

  private CountDownJob buildJobWithFixedDateTrigger(Instant fixedDate) {
    return CountDownJob.buildFromTriggers(ImmutableSet.of(Trigger.fromFixedTime(fixedDate)));
  }

  public static class CountDownJob extends Job {

    private CountDownLatch latch;

    @Autowired
    public void setLatchProvider(LatchProvider latchProvider) {
      this.latch = latchProvider.getLatch();
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
      latch.countDown();
    }

    public static CountDownJob buildFromTriggers(Set<Trigger> triggers) {
      CountDownJob job = new CountDownJob();
      job.setGroup(JOB_GROUP);
      job.setName(JOB_NAME);
      job.setTriggers(triggers);
      return job;
    }

    public static CountDownJob buildFromGroupAndNameAndTriggers(String group, String name,
                                                                ImmutableSet<Trigger> triggers) {
      CountDownJob job = new CountDownJob();
      job.setGroup(group);
      job.setName(name);
      job.setTriggers(triggers);
      return job;
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, group, name, triggers);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final CountDownJob other = (CountDownJob) obj;
      return Objects.equals(this.id, other.id) && Objects.equals(this.group, other.group)
             && Objects.equals(this.name, other.name) && Objects
          .equals(this.triggers, other.triggers);
    }

    @Override
    public String toString() {
      return com.google.common.base.MoreObjects.toStringHelper(this)
          .add("id", id)
          .add("group", group)
          .add("name", name)
          .add("triggers", triggers)
          .toString();
    }
  }

  @Test(timeout = FIRE_THRESHOLD_TIMEOUT_IN_MILLIS)
  public void shouldFireJobOnceWithReachableFutureDate() throws Scheduler.DuplicateJobKeyException,
                                                                InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    latchProvider.setLatch(latch);
    CountDownJob job = buildJobWithFixedDateTrigger(buildReachableFutureDate());

    scheduler.scheduleJob(job);

    latch.await();
  }

  private Instant buildReachableFutureDate() {
    return Instant.now().plusSeconds(REACHABLE_FUTURE_SECONDS);
  }

  @Test
  public void shouldNotFireJobWithNonReachableFutureDate() throws InterruptedException,
                                                                  Scheduler.DuplicateJobKeyException {
    CountDownLatch latch = new CountDownLatch(1);
    latchProvider.setLatch(latch);
    CountDownJob job = buildJobWithFixedDateTrigger(buildNonReachableFutureDate());

    scheduler.scheduleJob(job);

    assertThat(latch.await(FIRE_THRESHOLD_TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS), is(false));
  }

  private Instant buildNonReachableFutureDate() {
    return Instant.now().plus(NON_REACHABLE_FUTURE_MINUTES, ChronoUnit.MINUTES);
  }

  @Test(timeout = FIRE_THRESHOLD_TIMEOUT_IN_MILLIS)
  public void shouldFireMultipleTimesWithCron()
      throws Scheduler.DuplicateJobKeyException, InterruptedException {
    CountDownLatch latch = new CountDownLatch(2);
    latchProvider.setLatch(latch);
    CountDownJob job = buildEvery2SecondsCronJob();

    scheduler.scheduleJob(job);

    latch.await();
  }

  private CountDownJob buildEvery2SecondsCronJob() {
    return CountDownJob.buildFromTriggers(
        ImmutableSet.of(Trigger.fromCronExpression(EVERY_2_SECONDS_CRON_EXPRESSION)));
  }

  @Test(timeout = FIRE_THRESHOLD_TIMEOUT_IN_MILLIS)
  public void shouldFireMultipleTimesWithTwoPastTriggers()
      throws Scheduler.DuplicateJobKeyException,
             InterruptedException {
    CountDownLatch latch = new CountDownLatch(2);
    latchProvider.setLatch(latch);
    Instant pastDate = buildPastDate();
    CountDownJob job = CountDownJob.buildFromTriggers(
        ImmutableSet.of(Trigger.fromFixedTime(pastDate),
                        Trigger.fromFixedTime(pastDate.plusSeconds(1))));

    scheduler.scheduleJob(job);

    latch.await();
  }

  @Test(expected = Scheduler.DuplicateJobKeyException.class)
  public void shouldThrowExceptionWhenScheduledWithPendingJobWithSameNameAndGroup()
      throws Scheduler.DuplicateJobKeyException {
    CountDownJob existingJob = buildJobWithFixedDateTrigger(buildNonReachableFutureDate());
    scheduler.scheduleJob(existingJob);

    scheduler.scheduleJob(buildJobWithFixedDateTrigger(buildPastDate()));
  }

  @Test(timeout = FIRE_THRESHOLD_TIMEOUT_IN_MILLIS)
  public void shouldNotThrowExceptionWhenScheduledWithAlreadyFiredJobWithSameNameAndGroup()
      throws Scheduler.DuplicateJobKeyException, InterruptedException {
    waitJobWithPastDate();
    Thread.sleep(FIRED_JOB_AWAIT_THRESHOLD_IN_MILLIS);

    scheduler.scheduleJob(buildJobWithFixedDateTrigger(buildPastDate()));
  }

  private void waitJobWithPastDate()
      throws Scheduler.DuplicateJobKeyException, InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    latchProvider.setLatch(latch);
    CountDownJob existingJob = buildJobWithFixedDateTrigger(buildPastDate());
    scheduler.scheduleJob(existingJob);
    latch.await();
  }

  @Test
  public void shouldGetOnlyPendingJobKeysWhenGetJobKeys()
      throws Scheduler.DuplicateJobKeyException, InterruptedException {
    int jobId = 0;
    Set<JobKey> pendingJobsKeys = new HashSet<>();
    waitJobWithPastDateWithName(getJobNameFromJobId(jobId++));
    String jobName = getJobNameFromJobId(jobId++);
    pendingJobsKeys.add(new JobKey(JOB_GROUP, jobName));
    scheduleNonReachableFutureJobFromName(jobName);
    waitJobWithPastDateWithName(getJobNameFromJobId(jobId++));
    jobName = getJobNameFromJobId(jobId);
    pendingJobsKeys.add(new JobKey(JOB_GROUP, jobName));
    scheduleNonReachableFutureJobFromName(jobName);
    Thread.sleep(FIRED_JOB_AWAIT_THRESHOLD_IN_MILLIS);

    assertThat(scheduler.getJobKeys(), is(pendingJobsKeys));
  }

  private void scheduleNonReachableFutureJobFromName(String jobName)
      throws Scheduler.DuplicateJobKeyException {
    CountDownJob existingJob = CountDownJob.buildFromGroupAndNameAndTriggers(
        JOB_GROUP, jobName, ImmutableSet.of(Trigger.fromFixedTime(buildNonReachableFutureDate())));
    scheduler.scheduleJob(existingJob);
  }

  private void waitJobWithPastDateWithName(String jobName)
      throws Scheduler.DuplicateJobKeyException,
             InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    latchProvider.setLatch(latch);
    CountDownJob existingJob = CountDownJob.buildFromGroupAndNameAndTriggers(
        JOB_GROUP, jobName, ImmutableSet.of(Trigger.fromFixedTime(buildPastDate())));
    scheduler.scheduleJob(existingJob);
    latch.await();
  }

  @Test
  public void shouldGetEmptyListWhenGetJobKeysWithNoPendingJobs() {
    assertThat(scheduler.getJobKeys(), is(Collections.emptySet()));
  }

  private String getJobNameFromJobId(int jobId) {
    return jobId + "";
  }

  @Test
  public void shouldGetOnlyPendingJobKeysFromSpecifiedGroupsWhenGetJobKeysByGroup()
      throws Scheduler.DuplicateJobKeyException, InterruptedException {
    String group1 = "group1", group2 = "group2";
    int jobId = 0;
    Set<JobKey> group2JobsKeys = new HashSet<>();
    scheduleNonReachableFutureJobFromGroupAndName(group1, getJobNameFromJobId(jobId++));
    String jobName = getJobNameFromJobId(jobId++);
    group2JobsKeys.add(new JobKey(group2, jobName));
    scheduleNonReachableFutureJobFromGroupAndName(group2, jobName);
    scheduleNonReachableFutureJobFromGroupAndName(group1, getJobNameFromJobId(jobId++));
    jobName = getJobNameFromJobId(jobId);
    group2JobsKeys.add(new JobKey(group2, jobName));
    scheduleNonReachableFutureJobFromGroupAndName(group2, jobName);

    assertThat(scheduler.getJobKeysByGroup(group2), is(group2JobsKeys));
  }

  private void scheduleNonReachableFutureJobFromGroupAndName(String group, String jobName)
      throws Scheduler.DuplicateJobKeyException {
    CountDownJob existingJob = CountDownJob.buildFromGroupAndNameAndTriggers(
        group, jobName, ImmutableSet.of(Trigger.fromFixedTime(buildNonReachableFutureDate())));
    scheduler.scheduleJob(existingJob);
  }

  @Test
  public void shouldGetEmptyListWhenGetJobKeysByGroupWithNoJobsInTheGroup()
      throws Scheduler.DuplicateJobKeyException {
    scheduleNonReachableFutureJobFromGroupAndName(JOB_GROUP, JOB_NAME);
    assertThat(scheduler.getJobKeysByGroup("x"), is(Collections.emptySet()));
  }

  @Test
  public void shouldRetrievePendingJobInformationWhenGetJobWithPendingJob()
      throws Scheduler.DuplicateJobKeyException {
    CountDownJob job = buildJobWithFixedDateTrigger(buildNonReachableFutureDate());
    scheduler.scheduleJob(job);
    assertThat(scheduler.getJob(new JobKey(JOB_GROUP, JOB_NAME)), is(Optional.of(job)));
  }

  @Test
  public void shouldRetrievePendingJobInformationWhenGetJobWithCronJob()
      throws Scheduler.DuplicateJobKeyException {
    CountDownJob job = buildEvery2SecondsCronJob();
    scheduler.scheduleJob(job);
    assertThat(scheduler.getJob(new JobKey(JOB_GROUP, JOB_NAME)), is(Optional.of(job)));
  }

  @Test
  public void shouldGetNoJobWhenGetJobWithWithAlreadyFiredJob()
      throws InterruptedException, Scheduler.DuplicateJobKeyException {
    waitJobWithPastDate();
    Thread.sleep(FIRED_JOB_AWAIT_THRESHOLD_IN_MILLIS);
    assertThat(scheduler.getJob(new JobKey(JOB_GROUP, JOB_NAME)), is(Optional.empty()));
  }

  @Test
  public void shouldGetNoJobWhenGetJobWithWithUnknownJobKeys() {
    assertThat(scheduler.getJob(new JobKey(JOB_GROUP, JOB_NAME)), is(Optional.empty()));
  }

  @Test
  public void shouldGetNoJobWhenGetJobAfterDeletePendingJob()
      throws Scheduler.DuplicateJobKeyException {
    scheduleNonReachableFutureJobFromGroupAndName(JOB_GROUP, JOB_NAME);
    scheduler.deleteJob(new JobKey(JOB_GROUP, JOB_NAME));
    assertThat(scheduler.getJob(new JobKey(JOB_GROUP, JOB_NAME)), is(Optional.empty()));
  }

  @Test
  public void shouldNotIncludeDeletedJobWhenGetJobKeys() throws Scheduler.DuplicateJobKeyException {
    String job1Name = "job1", job2Name = "job2";
    scheduleNonReachableFutureJobFromGroupAndName(JOB_GROUP, job1Name);
    scheduleNonReachableFutureJobFromGroupAndName(JOB_GROUP, job2Name);
    scheduler.deleteJob(new JobKey(JOB_GROUP, job1Name));
    assertThat(scheduler.getJobKeys(), is(ImmutableSet.of(new JobKey(JOB_GROUP, job2Name))));
  }

  @Test
  public void shouldNotFireDeletedJobWithReachableFutureDate()
      throws Scheduler.DuplicateJobKeyException, InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    latchProvider.setLatch(latch);
    CountDownJob job = buildJobWithFixedDateTrigger(buildReachableFutureDate());
    scheduler.scheduleJob(job);
    scheduler.deleteJob(new JobKey(JOB_GROUP, JOB_NAME));
    assertThat(latch.await(FIRE_THRESHOLD_TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS), is(false));
  }

  @Test
  public void shouldStopFiringCronJobAfterDeleted()
      throws Scheduler.DuplicateJobKeyException, InterruptedException {
    CountDownLatch latch = new CountDownLatch(2);
    latchProvider.setLatch(latch);
    scheduler.scheduleJob(buildEvery2SecondsCronJob());
    scheduler.deleteJob(new JobKey(JOB_GROUP, JOB_NAME));
    assertThat(latch.await(FIRE_THRESHOLD_TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS), is(false));
  }

  @Test
  public void shouldReturnTrueWhenDeletePendingJob() throws Scheduler.DuplicateJobKeyException {
    scheduleNonReachableFutureJobFromGroupAndName(JOB_GROUP, JOB_NAME);
    assertThat(scheduler.deleteJob(new JobKey(JOB_GROUP, JOB_NAME)), is(true));
  }

  @Test
  public void shouldReturnTrueWhenDeleteCronJob() throws Scheduler.DuplicateJobKeyException {
    scheduler.scheduleJob(buildEvery2SecondsCronJob());
    assertThat(scheduler.deleteJob(new JobKey(JOB_GROUP, JOB_NAME)), is(true));
  }

  @Test
  public void shouldReturnFalseWhenDeleteAlreadyFiredJob()
      throws InterruptedException, Scheduler.DuplicateJobKeyException {
    waitJobWithPastDateWithName(JOB_NAME);
    Thread.sleep(FIRE_THRESHOLD_TIMEOUT_IN_MILLIS);
    assertThat(scheduler.deleteJob(new JobKey(JOB_GROUP, JOB_NAME)), is(false));
  }

  @Test
  public void shouldReturnFalseWhenDeleteWithUnknownJobKeys() {
    assertThat(scheduler.deleteJob(new JobKey(JOB_GROUP, JOB_NAME)), is(false));
  }

}
