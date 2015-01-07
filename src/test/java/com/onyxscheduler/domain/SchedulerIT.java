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

import com.onyxscheduler.OnyxSchedulerApplication;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = OnyxSchedulerApplication.class)
public class SchedulerIT {
  // TODO test quartz scheduling and methods (proper usage of quartz),
  // even though we tested in unit test that methods are invoked with proper parameters we didn't
  // test with actual quartz to verify if quartz is properly setup and the methods we invoke
  // behave as we want with quartz actual classes. Here use a FakeJob to avoid testing against
  // http, and just test that triggers are fired X times. Verify schedules, gets of pendings,
  // gets without pendings, filtering of groups, etc.

  @Autowired
  Scheduler scheduler;
}
