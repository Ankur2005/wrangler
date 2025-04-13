/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

 package io.cdap.wrangler.parser;

 import io.cdap.wrangler.api.parser.TimeDuration;
//  import org.junit.Test;
 import static org.junit.Assert.assertEquals;

import org.testng.annotations.Test;
 
 public class TimeDurationTest {
 
     @org.testng.annotations.Test
     public void testValidDurations() {
         assertEquals(5L, new TimeDuration("5ms").getMilliseconds());
         assertEquals(2100L, new TimeDuration("2.1s").getMilliseconds());
         assertEquals(60000L, new TimeDuration("1min").getMilliseconds());
         assertEquals(3600000L, new TimeDuration("1hr").getMilliseconds());
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testInvalidDuration() {
         new TimeDuration("3zz");
     }
 }