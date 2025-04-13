/*
 *  Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

 package io.cdap.wrangler.parser;
 import io.cdap.wrangler.api.parser.ByteSize;
//  import org.junit.Test;
 import org.testng.ITest;

import static org.junit.Assert.assertEquals;
 
 
 public class ByteSizeTest {
 
     @org.testng.annotations.Test
     public void testValidByteSizes() {
         assertEquals(10 * 1000L, new ByteSize("10KB").getBytes());
         assertEquals(1500000L, new ByteSize("1.5MB").getBytes());
         assertEquals(5L, new ByteSize("5B").getBytes());
         assertEquals(1000L * 1000L * 1000L, new ByteSize("1GB").getBytes());
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testInvalidByteSize() {
         new ByteSize("10XY");
     }
 }