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

package com.onyxscheduler.web;

import com.onyxscheduler.domain.Job;
import com.onyxscheduler.domain.JobKey;
import com.onyxscheduler.domain.Scheduler;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Main entry point for the onyx REST API (and job scheduling and managing).
 * <p/>
 * Here are hosted all logic and configuration related to the API from documentation to mapping
 * to proper requests, responses, exception handling, etc.
 */
@RestController
@RequestMapping("/onyx")
public class JobController {
  private final Scheduler scheduler;

  @Autowired
  public JobController(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  @RequestMapping(value = "/groups/{group}/jobs", method = RequestMethod.POST)
  public ResponseEntity<Job> addJob(@PathVariable String group, @RequestBody Job job)
    throws NonMatchingGroupsException, Scheduler.DuplicateJobKeyException {
    if (job.getGroup() != null && !job.getGroup().equals(group)) {
      throw new NonMatchingGroupsException(group, job.getGroup());
    }
    job.setGroup(group);
    scheduler.scheduleJob(job);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
      .path("/{job}").buildAndExpand(job.getName())
      .toUri();
    return ResponseEntity.created(location).body(job);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  static class NonMatchingGroupsException extends Exception {
    public NonMatchingGroupsException(String pathGroup, String bodyGroup) {
      super("group provided in the job body should be unspecified or match the path group. " +
        "Current group in path is '" + pathGroup + "' and in body is '" + bodyGroup + "'.");
    }
  }

  @ExceptionHandler(Scheduler.DuplicateJobKeyException.class)
  void handleBadRequests(HttpServletResponse response) throws IOException {
    response.sendError(HttpStatus.BAD_REQUEST.value());
  }

  @RequestMapping(value = "/jobs", method = RequestMethod.GET)
  public Set<JobKey> getJobKeys() {
    return scheduler.getJobKeys();
  }

  @RequestMapping(value = "/groups/{group}/jobs", method = RequestMethod.GET)
  public Set<JobKey> getJobKeysByGroup(@PathVariable String group) {
    return scheduler.getJobKeysByGroup(group);
  }

  @RequestMapping(value = "/groups/{group}/jobs/{name}", method = RequestMethod.GET)
  public Job getJob(@PathVariable String group, @PathVariable String name)
    throws JobNotFoundException {
    return scheduler.getJob(new JobKey(group, name))
      .orElseThrow(() -> new JobNotFoundException(group, name));
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  static class JobNotFoundException extends Exception {
    public JobNotFoundException(String group, String name) {
      super("could not find job in group '" + group + "' with name '" + name + "'.");
    }
  }

  @RequestMapping(value = "/groups/{group}/jobs/{name}", method = RequestMethod.DELETE)
  public void deleteJob(@PathVariable String group, @PathVariable String name) {
    scheduler.deleteJob(new JobKey(group, name));
  }

}
