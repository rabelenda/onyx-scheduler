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

package com.onyxscheduler.quartz;

import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.jdbcjobstore.DriverDelegate;
import org.quartz.spi.JobStore;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

@ConfigurationProperties(prefix = QuartzProperties.PREFIX)
public class QuartzProperties {

    public static final String PREFIX = "quartz";

    public Properties buildQuartzProperties() {
        return jobstore.buildQuartzProperties();
    }

    public static class JobStoreProperties {

        private Class<? extends JobStore> clazz;

        private Class<? extends DriverDelegate> driverDelegateClass;

        private Boolean isClustered;

        public Class<? extends JobStore> getClazz() {
            return clazz;
        }

        public void setClazz(Class<? extends JobStore> clazz) {
            this.clazz = clazz;
        }

        public Class<? extends DriverDelegate> getDriverDelegateClass() {
            return driverDelegateClass;
        }

        public void setDriverDelegateClass(Class<? extends DriverDelegate> driverDelegateClass) {
            this.driverDelegateClass = driverDelegateClass;
        }

        public boolean isClustered() {
            return isClustered;
        }

        public void setClustered(boolean isClustered) {
            this.isClustered = isClustered;
        }

        public Properties buildQuartzProperties() {
            Properties props = new Properties();
            if (clazz != null) {
                props.put(StdSchedulerFactory.PROP_JOB_STORE_CLASS, clazz);
            }
            if (driverDelegateClass != null) {
                props.put(StdSchedulerFactory.PROP_JOB_STORE_PREFIX + "driverDelegateClass", driverDelegateClass);
            }
            if (isClustered != null) {
                props.put(StdSchedulerFactory.PROP_JOB_STORE_PREFIX + "isClustered", isClustered);
            }
            return props;
        }
    }

    private JobStoreProperties jobstore = new JobStoreProperties();

    public JobStoreProperties getJobstore() {
        return jobstore;
    }

    public void setJobstore(JobStoreProperties jobstore) {
        this.jobstore = jobstore;
    }

}
