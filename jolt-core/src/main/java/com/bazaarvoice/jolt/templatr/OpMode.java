/*
 * Copyright 2013 Bazaarvoice, Inc.
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

package com.bazaarvoice.jolt.templatr;

import java.util.List;
import java.util.Map;

/**
 * OpMode differentiates different flavors of Templatr
 *
 * Templatr can fill in leaf values as required in spec from a specified context, self or a hardcoded
 * default value. However whether or not that 'write' operation should go through, is determined by
 * this enum.
 *
 * All of these opModes validates if the if the source (map or list) and the key/index are valid,
 * i.e. not null or >= 0, etc.
 *
 * OVERWRITR always writes
 * DEFAULTR only writes when the the value at the key/index is null
 * DEFINR only writes when source does not contain the key/index
 *
 */
public enum OpMode {
    OVERWRITR {
        @Override
        public boolean isApplicable( final Map source, final String key ) {
            return super.isApplicable(source, key);
        }
        @Override
        public boolean isApplicable( final List source, final int reqIndex ,  int origSize) {
            return super.isApplicable(source, reqIndex , origSize);
        }
    },
    DEFAULTR {
        @Override
        public boolean isApplicable( final Map source, final String key ) {
            return super.isApplicable( source, key ) && source.get( key ) == null;
        }
        @Override
        public boolean isApplicable( final List source, final int reqIndex, int origSize ) {
            return super.isApplicable(source, reqIndex, origSize ) && source.get( reqIndex ) == null;
        }
    },
    DEFINER {
        @Override
        public boolean isApplicable( final Map source, final String key ) {
            return super.isApplicable(source, key) && !source.containsKey( key );
        }
        @Override
        public boolean isApplicable( final List source, final int reqIndex, int origSize ) {
            return super.isApplicable(source, reqIndex, origSize ) &&
                    // only new index contains null
                    reqIndex >= origSize && source.get( reqIndex ) == null;
        }
    };

    /**
     * Given a source map and a input key returns true if it is ok to go ahead with
     * write operation given a specific opMode
     */
    public boolean isApplicable(Map source, String key) {
        return source != null && key != null;
    }

    /**
     * Given a source list and a input index and original size of the list (when passed in as input)
     * returns true if it is ok to go ahead with write operation given a specific opMode
     */
    public boolean isApplicable(List source, int reqIndex, int origSize) {
        return source != null && reqIndex >= 0 && origSize >= 0;
    }
}
