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

import static com.onyxscheduler.util.TriggerTestUtils.buildTriggers;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.*;

public abstract class JobTest {

  private static final String JOB_NAME = "name";
  private static final String JOB_GROUP = "group";


  //this test is relying on proper equals implementation in job subclasses which should be
  // auto-generated and updated with IDE to avoid bugs and have to create test for each potential
  // case of the equals method
  protected void verifyGettingSameJobWhenBuildingJobBackFromGeneratedJobDetail(Job originalJob) {
    originalJob.setName(JOB_NAME);
    originalJob.setGroup(JOB_GROUP);
    Set<Trigger> triggers = buildTriggers();
    originalJob.setTriggers(triggers);

    Job restoredJob =
      Job.fromQuartzJobDetailAndTriggers(originalJob.buildQuartzJobDetail(), originalJob.buildQuartzTriggers());

    assertThat(restoredJob, is(originalJob));
  }

}
