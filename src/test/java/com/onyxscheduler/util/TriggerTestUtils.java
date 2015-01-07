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

package com.onyxscheduler.util;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.onyxscheduler.domain.Trigger;
import java.text.*;
import java.util.*;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;

public class TriggerTestUtils {

  private static final DateFormat DATE_FORMAT = new ISO8601DateFormat();

  public static final String CRON = "0/2 * * * * ?";
  public static final Date FIXED_TIME = parseDate("2030-01-01T00:00:00Z");

  private static Date parseDate(String str) {
    try {
      return DATE_FORMAT.parse(str);
    } catch (ParseException e) {
      throw Throwables.propagate(e);
    }
  }

  public static Set<Trigger> buildTriggers() {
    return ImmutableSet.of(Trigger.fromCronExpression(CRON), Trigger.fromFixedTime(FIXED_TIME));
  }

  public static Trigger buildTriggerWithCron() {
    return Trigger.fromCronExpression(CRON);
  }

  public static Trigger buildTriggerWithFixedTime() {
    return Trigger.fromFixedTime(FIXED_TIME);
  }

  public static Set<org.quartz.Trigger> buildQuartzTriggers() {
    return ImmutableSet.of(buildFixedTimeQuartzTrigger(), buildCronQuartzTrigger());
  }

  public static CronTrigger buildCronQuartzTrigger() {
    return TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(CRON)).build();
  }

  public static org.quartz.Trigger buildFixedTimeQuartzTrigger() {
    return TriggerBuilder.newTrigger().startAt(FIXED_TIME).build();
  }

}
