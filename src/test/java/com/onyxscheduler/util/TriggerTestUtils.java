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

import com.google.common.collect.ImmutableSet;

import com.onyxscheduler.domain.Trigger;

import java.time.Instant;
import java.util.Set;

public class TriggerTestUtils {

  public static final String CRON = "0/2 * * * * ?";
  public static final Instant FIXED_TIME = Instant.parse("2030-01-01T00:00:00Z");

  private TriggerTestUtils() {
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

}
