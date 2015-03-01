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

import com.onyxscheduler.util.TriggerTestUtils;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TriggerTest {

  @Test
  public void shouldGetTriggerWithCronWhenFromCronExpression() {
    assertThat(Trigger.fromCronExpression(TriggerTestUtils.CRON).getCron(), Matchers.is(
        TriggerTestUtils.CRON));
  }

  @Test
  public void shouldGetTriggerWithFixedTimeWhenFromFixedDate() {
    assertThat(Trigger.fromFixedTime(TriggerTestUtils.FIXED_TIME).getWhen(), Matchers.is(
        TriggerTestUtils.FIXED_TIME));
  }

  @Test
  public void shouldBeValidTriggerWhenOnlyCronIsSpecified() {
    MatcherAssert.assertThat(TriggerTestUtils.buildTriggerWithCron().isValid(), is(true));
  }

  @Test
  public void shouldBeValidTriggerWhenOnlyFixedTimeIsSpecified() {
    MatcherAssert.assertThat(TriggerTestUtils.buildTriggerWithFixedTime().isValid(), is(true));
  }

  @Test
  public void shouldNotBeValidTriggerWhenCronNorFixedTimeAreSpecified() {
    Trigger trigger = new Trigger();
    assertThat(trigger.isValid(), is(false));
  }

  @Test
  public void shouldNotBeValidTriggerWhenCronAndFixedTimeAreSpecified() {
    Trigger trigger = new Trigger();
    trigger.setCron(TriggerTestUtils.CRON);
    trigger.setWhen(TriggerTestUtils.FIXED_TIME);
    assertThat(trigger.isValid(), is(false));
  }

  @Test
  public void shouldGetSameCronTriggerWhenBuildingTriggerBackFromGeneratedQuartzTrigger() {
    Trigger originalTrigger = TriggerTestUtils.buildTriggerWithCron();

    Trigger restoredTrigger = Trigger.fromQuartzTrigger(originalTrigger.buildQuartzTrigger());

    assertThat(restoredTrigger, is(originalTrigger));
  }

  @Test
  public void shouldGetSameFixedTimeTriggerWhenBuildingTriggerBackFromGeneratedQuartzTrigger() {
    Trigger originalTrigger = TriggerTestUtils.buildTriggerWithFixedTime();

    Trigger restoredTrigger = Trigger.fromQuartzTrigger(originalTrigger.buildQuartzTrigger());

    assertThat(restoredTrigger, is(originalTrigger));
  }

}
