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

 package io.cdap.directives.aggregates;

 import io.cdap.wrangler.api.Row;
 import io.cdap.wrangler.test.TestingRig;
 import io.cdap.wrangler.test.api.TestRecipe;
 import org.junit.jupiter.api.Test;
 import static org.junit.jupiter.api.Assertions.assertEquals;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class AggregateSizeAndTimeRecipeTest {
 
   @Test
   public void testAggregateWithRecipe() throws Exception {
     // Step 1: Write the recipe.
     String[] recipe = new String[] {
       "aggregate-size-time :size :time totalSize totalTime aggregationType=total sizeUnit=MB timeUnit=sec"
     };
 
     // Step 2: Create input rows.
     List<Row> rows = Arrays.asList(
       new Row("size", "1MB").add("time", "1s"),
       new Row("size", "2MB").add("time", "3s")
     );
 
     // Step 3: Execute recipe.
     TestRecipe testRecipe = new TestRecipe();
     testRecipe.add(recipe[0]);
     List<Row> results = TestingRig.pipeline(AggregateSizeAndTime.class, testRecipe)
       .execute(rows);
 
     // Step 4: Assert output.
     assertEquals(1, results.size());
     Row output = results.get(0);
     assertEquals(3.0, (Double) output.getValue("totalSize"), 0.001);
     assertEquals(4.0, (Double) output.getValue("totalTime"), 0.001);
  }
 }