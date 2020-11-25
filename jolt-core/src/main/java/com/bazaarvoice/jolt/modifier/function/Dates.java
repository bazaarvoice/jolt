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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * @author Alessio Zamboni <alessio.zamboni@unitn.it>
 * @date: 25/11/2020
 */
@SuppressWarnings( "deprecated" )
public class Dates {
  public static Optional<Long> castToLong(Object obj) {
    if(obj instanceof Number) {
      return Optional.of(((Number) obj).longValue());
    }
    return Optional.empty();
  }

  public static Optional<String> fromEpochMilli(Object arg, Object format) {
    Optional<Long> optEpoch = castToLong(arg);
    if ( arg == null || !(format instanceof String) || !optEpoch.isPresent()) {
      return Optional.empty();
    }

    Long epoch = optEpoch.get();
    String formatStr = (String) format;
    Date d = Date.from(Instant.ofEpochMilli(epoch));
    SimpleDateFormat dateFormat = new SimpleDateFormat(formatStr);
    return Optional.<String>of(dateFormat.format(d));
  }

  public static Optional<Long> toEpochMilli(Object date, Object format) {
    if ( !((date instanceof String) && (format instanceof String)) ) {
      return Optional.empty();
    }
    String formatStr = (String) format;
    String dateStr = (String) date;
    SimpleDateFormat dateFormat = new SimpleDateFormat(formatStr);

    try {
      Date d = dateFormat.parse(dateStr);
      return Optional.<Long>of( d.toInstant().toEpochMilli() );
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  public static Optional<Long> now() {
    return Optional.<Long>of(Instant.now().toEpochMilli());
  }

  public static final class now implements Function {
    @Override
    @SuppressWarnings( "unchecked" )
    public Optional<Object> apply(Object... args) {
      return (Optional) now();
    }
  }

  @SuppressWarnings( "unchecked" )
  public static final class fromEpochMilli extends Function.BaseFunction<Object> {

    @Override
    protected Optional<Object> applyList(List<Object> input) {
      if ((input == null) && (input.size() != 2)) {
        return Optional.empty();
      } else {
        return (Optional) fromEpochMilli(input.get(0), input.get(1));
      }
    }

    @Override
    protected Optional<Object> applySingle(Object arg) {
      return (Optional) fromEpochMilli(arg, "yyyyMMdd");
    }
  }

  @SuppressWarnings( "unchecked" )
  public static final class toEpochMilli extends Function.BaseFunction<Object> {

    @Override
    protected Optional<Object> applyList(List<Object> input) {
      if ((input == null) && (input.size() != 2)) {
        return Optional.empty();
      } else {
        return (Optional) toEpochMilli(input.get(0), input.get(1));
      }
    }

    @Override
    protected Optional<Object> applySingle(Object arg) {
      return Optional.empty();
    }
  }
}
