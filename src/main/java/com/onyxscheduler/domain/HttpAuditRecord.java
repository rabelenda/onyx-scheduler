package com.onyxscheduler.domain;

import java.io.Serializable;
import java.util.Date;

public class HttpAuditRecord {

    private final HttpJob request;
    private final int responseCode;
    private final String responseBody;
    private final Date startTime;
    private final Date endTime;
    private final int trialCount;

    public HttpAuditRecord(
        HttpJob request,
        int responseCode,
        String responseBody, Date startTime, Date endTime, int trialCount) {
      this.request = request;
      this.responseCode = responseCode;
      this.responseBody = responseBody;
      this.startTime = startTime;
      this.endTime = endTime;
      this.trialCount = trialCount;
    }

    @Override
    public String toString() {
      return com.google.common.base.Objects.toStringHelper(this)
          .add("request", request)
          .add("responseCode", responseCode)
          .add("responseBody", responseBody)
          .add("startTime", startTime)
          .add("endTime", endTime)
          .toString();
    }
  }