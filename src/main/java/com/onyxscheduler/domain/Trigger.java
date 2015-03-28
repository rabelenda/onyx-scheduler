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

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

import javax.validation.constraints.AssertTrue;

/**
 * Specifies a scheduling to be used in a job. <p/> Simpler abstraction than quartz triggers which
 * easily binds to the desired JSON representation for triggers.
 */
public class Trigger {

  private Instant when;
  private String cron;

  public static Trigger fromFixedTime(Instant when) {
    Trigger trigger = new Trigger();
    trigger.setWhen(when);
    return trigger;
  }

  public static Trigger fromCronExpression(String cron) {
    Trigger trigger = new Trigger();
    trigger.setCron(cron);
    return trigger;
  }

  public Instant getWhen() {
    return when;
  }

  public void setWhen(Instant when) {
    this.when = when;
  }

  public String getCron() {
    return cron;
  }

  public void setCron(String cron) {
    this.cron = cron;
  }

  @AssertTrue(message = "Either 'cron' or 'when' should be specified")
  @JsonIgnore
  public boolean isValid() {
    return cron != null ^ when != null;
  }

  public org.quartz.Trigger buildQuartzTrigger() {
    if (cron != null) {
      return TriggerBuilder.newTrigger()
          .withSchedule(CronScheduleBuilder.cronSchedule(cron))
          .build();
    } else {
      return TriggerBuilder.newTrigger().startAt(Date.from(when)).build();
    }
  }

  public static Trigger fromQuartzTrigger(org.quartz.Trigger quartzTrigger) {
    if (quartzTrigger instanceof CronTrigger) {
      CronTrigger conTrigger = (CronTrigger) quartzTrigger;
      return fromCronExpression(conTrigger.getCronExpression());
    } else {
      return fromFixedTime(quartzTrigger.getStartTime().toInstant());
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(when, cron);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final Trigger other = (Trigger) obj;
    return Objects.equals(this.when, other.when) && Objects.equals(this.cron, other.cron);
  }

  @Override
  public String toString() {
    return com.google.common.base.Objects.toStringHelper(this)
        .add("when", when)
        .add("cron", cron)
        .toString();
  }

}
