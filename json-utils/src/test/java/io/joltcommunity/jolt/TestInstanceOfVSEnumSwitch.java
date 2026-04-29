/*
 * Copyright 2013-2023 Bazaarvoice, Inc.
 * Copyright 2025 Jolt Community
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
package io.joltcommunity.jolt;

import org.testng.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Small test to see if it is more efficient to do instanceof checks
 * or to have Concrete subclasses have an Enum type.
 * <p>
 * Answer :
 * InstanceOf Test looping 50000000
 * Took : 24156
 * Typed Test looping 50000000
 * Took : 23571
 * <p>
 * It doesn't really matter   ;)
 */
public class TestInstanceOfVSEnumSwitch {

    private static final int LOOP_COUNT = 1000 * 1000 * 50;

    //@Test
    public void testTyped() {

        System.out.println("Typed Test looping " + LOOP_COUNT);
        long begin = System.currentTimeMillis();
        for (int index = 0; index < LOOP_COUNT; index++) {
            int typeToMake = index % 5;

            EnumBaseInterface t = switch (typeToMake) {
                case 0 -> new StringEnum();
                case 1 -> new IntegerEnum();
                case 2 -> new BooleanEnum();
                case 3 -> new DateEnum();
                case 4 -> new LogicalEnum();
                default -> throw new RuntimeException("pants");
            };

            switch (t.getType()) {
                case STRING:
                    StringEnum s = (StringEnum) t;
                    List<String> sValues = s.getValues();
                    Assert.assertEquals(Arrays.asList("A", "B"), sValues);
                    break;
                case INTEGER:
                    IntegerEnum i = (IntegerEnum) t;
                    List<Integer> iValues = i.getValues();
                    Assert.assertEquals(Arrays.asList(1, 2), iValues);
                    break;
                case BOOLEAN:
                    BooleanEnum b = (BooleanEnum) t;
                    List<Boolean> bValues = b.getValues();
                    Assert.assertEquals(Arrays.asList(true, false), bValues);
                    break;
                case DATE:
                    DateEnum d = (DateEnum) t;
                    List<String> dValues = d.getValues();
                    Assert.assertEquals(Arrays.asList("10", "11"), dValues);
                    break;
                case LOGICAL:
                    LogicalEnum l = (LogicalEnum) t;
                    List<EnumBaseInterface> lValues = l.getValues();
                    Assert.assertEquals(0, lValues.size());
                    break;
            }
        }

        long end = System.currentTimeMillis();

        System.out.println("Took : " + (end - begin));
    }

    //@Test
    public void testInstanceOf() {

        System.out.println("InstanceOf Test looping " + LOOP_COUNT);
        long begin = System.currentTimeMillis();
        for (int index = 0; index < LOOP_COUNT; index++) {
            int typeToMake = index % 5;

            InstanceOfInterface t = switch (typeToMake) {
                case 0 -> new StringInstanceOf();
                case 1 -> new IntegerInstanceOf();
                case 2 -> new BooleanInstanceOf();
                case 3 -> new DateInstanceOf();
                case 4 -> new LogicalInstanceOf();
                default -> throw new RuntimeException("pants");
            };

            if (t instanceof StringInstanceOf s) {
                List<String> sValues = s.getValues();
                Assert.assertEquals(Arrays.asList("A", "B"), sValues);
            } else if (t instanceof IntegerInstanceOf i) {
                List<Integer> iValues = i.getValues();
                Assert.assertEquals(Arrays.asList(1, 2), iValues);
            } else if (t instanceof BooleanInstanceOf b) {
                List<Boolean> bValues = b.getValues();
                Assert.assertEquals(Arrays.asList(true, false), bValues);
            } else if (t instanceof DateInstanceOf d) {
                List<String> dValues = d.getValues();
                Assert.assertEquals(Arrays.asList("10", "11"), dValues);
            } else if (t instanceof LogicalInstanceOf l) {
                List<InstanceOfInterface> lValues = l.getValues();
                Assert.assertEquals(0, lValues.size());
            }
        }

        long end = System.currentTimeMillis();

        System.out.println("Took : " + (end - begin));
    }

    enum Type {
        STRING,
        INTEGER,
        BOOLEAN,
        DATE,
        LOGICAL
    }

    interface EnumBaseInterface<T> {
        Type getType();

        List<T> getValues();
    }

    interface InstanceOfInterface<T> {
        List<T> getValues();
    }

    class StringEnum implements EnumBaseInterface<String> {
        public Type getType() {
            return Type.STRING;
        }

        public List<String> getValues() {
            return Arrays.asList("A", "B");
        }
    }

    class IntegerEnum implements EnumBaseInterface<Integer> {
        public Type getType() {
            return Type.INTEGER;
        }

        public List<Integer> getValues() {
            return Arrays.asList(1, 2);
        }
    }

    class BooleanEnum implements EnumBaseInterface<Boolean> {
        public Type getType() {
            return Type.BOOLEAN;
        }

        public List<Boolean> getValues() {
            return Arrays.asList(true, false);
        }
    }

    class DateEnum implements EnumBaseInterface<String> {
        public Type getType() {
            return Type.DATE;
        }

        public List<String> getValues() {
            return Arrays.asList("10", "11");
        }
    }

    class LogicalEnum implements EnumBaseInterface<EnumBaseInterface> {
        public Type getType() {
            return Type.LOGICAL;
        }

        public List<EnumBaseInterface> getValues() {
            return new ArrayList<>();
        }
    }

    class StringInstanceOf implements InstanceOfInterface<String> {
        public List<String> getValues() {
            return Arrays.asList("A", "B");
        }
    }

    class IntegerInstanceOf implements InstanceOfInterface<Integer> {
        public List<Integer> getValues() {
            return Arrays.asList(1, 2);
        }
    }

    class BooleanInstanceOf implements InstanceOfInterface<Boolean> {
        public List<Boolean> getValues() {
            return Arrays.asList(true, false);
        }
    }

    class DateInstanceOf implements InstanceOfInterface<String> {
        public List<String> getValues() {
            return Arrays.asList("10", "11");
        }
    }

    class LogicalInstanceOf implements InstanceOfInterface<InstanceOfInterface> {
        public List<InstanceOfInterface> getValues() {
            return new ArrayList<>();
        }
    }
}
