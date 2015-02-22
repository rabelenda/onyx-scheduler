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

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;

import java.util.concurrent.TimeUnit;

public class PollingVerifier {

    private static final long VERIFYING_POLL_PERIOD_IN_MILLIS = 500;
    private static final long VERIFYING_POLL_TIMEOUT_IN_MILLIS = 5000;

    public static void pollingVerify(Runnable verify) {
        AssertionError lastVerification;
        Stopwatch watch = Stopwatch.createStarted();
        do {
            try {
                lastVerification = null;
                verify.run();
            } catch (AssertionError e) {
                lastVerification = e;
                try {
                    Thread.sleep(VERIFYING_POLL_PERIOD_IN_MILLIS);
                } catch (InterruptedException e1) {
                    Throwables.propagate(e1);
                }
            }
        } while (watch.elapsed(TimeUnit.MILLISECONDS) < VERIFYING_POLL_TIMEOUT_IN_MILLIS);
        if (lastVerification != null) {
            throw lastVerification;
        }
    }

}
