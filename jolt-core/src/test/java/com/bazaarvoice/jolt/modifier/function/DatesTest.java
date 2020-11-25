/*
 * Copyright 2020 Alessio Zamboni <zambotn@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bazaarvoice.jolt.modifier.function;

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.modifier.function.Dates;
import com.bazaarvoice.jolt.modifier.function.Dates.fromEpochMilli;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Alessio Zamboni <alessio.zamboni@unitn.it>
 * @date: 25/11/2020
 */
@SuppressWarnings( "deprecated" )
public class DatesTest extends AbstractTester {

  @Override
  @DataProvider(parallel = false)
  public Iterator<Object[]> getTestCases() {
    List<Object[]> testCases = new LinkedList<>(  );
    Function TO_EPOCH = new Dates.toEpochMilli();
    Function FROM_EPOCH = new Dates.fromEpochMilli();


    testCases.add(new Object[] {"fromEpoch-default-long", FROM_EPOCH, new Object[] {1L}, Optional.of("19700101")});
    testCases.add(new Object[] {"fromEpoch-pattern-long", FROM_EPOCH, new Object[] {1L, "yyyy"}, Optional.of("1970")});
    testCases.add(new Object[] {"fromEpoch-default-int", FROM_EPOCH, new Object[] {1}, Optional.of("19700101")});
    testCases.add(new Object[] {"fromEpoch-pattern-int", FROM_EPOCH, new Object[] {1, "yyyy"}, Optional.of("1970")});
    testCases.add(new Object[] {"fromEpoch-default-string", FROM_EPOCH, new Object[] {"1"}, Optional.empty()});
    testCases.add(new Object[] {"fromEpoch-pattern-string", FROM_EPOCH, new Object[] {"1", "yyyy"}, Optional.empty()});

    //Have to specify TIMEZONE here, otherwise is current locale
    testCases.add(new Object[] {"toEpoch-pattern", TO_EPOCH, new Object[] {"1970-01-01#UTC", "yyyy-MM-dd#z"}, Optional.of(0L)});

    return testCases.iterator();
  }

  @Test
  public void nowReturnsEpoch() {
    Optional<Object> opt = (new Dates.now()).apply(null);
    assert(opt.isPresent() && (opt.get() instanceof Long));

  }
}
