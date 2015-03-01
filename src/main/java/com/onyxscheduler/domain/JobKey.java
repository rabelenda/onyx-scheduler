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

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Objects;

/**
 * Simple abstraction of job keys analogous to quartz JobKey. <p/> This class was created to not
 * have dependencies from JobController to any quartz class and make it simpler and would simplify
 * potential migration out of quartz.
 */
public class JobKey {

  private final String name;
  private final String group;

  @JsonCreator
  public JobKey(String group, String name) {
    this.name = name;
    this.group = group;
  }

  public String getName() {
    return name;
  }

  @SuppressWarnings("UnusedDeclaration")
  public String getGroup() {
    return group;
  }

  public org.quartz.JobKey buildQuartzJobKey() {
    return new org.quartz.JobKey(name, group);
  }

  public static JobKey fromQuartzJobKey(org.quartz.JobKey key) {
    return new JobKey(key.getGroup(), key.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, group);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final JobKey other = (JobKey) obj;
    return Objects.equals(this.name, other.name) && Objects.equals(this.group, other.group);
  }

  @Override
  public String toString() {
    return com.google.common.base.Objects.toStringHelper(this)
        .add("name", name)
        .add("group", group)
        .toString();
  }

}
