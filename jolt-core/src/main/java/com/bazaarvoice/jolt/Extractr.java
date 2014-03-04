/*
 * Copyright 2014 Bazaarvoice, Inc.
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
package com.bazaarvoice.jolt;

import com.bazaarvoice.jolt.common.WalkedPath;
import com.bazaarvoice.jolt.common.pathelement.LiteralPathElement;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.shiftr.ShiftrTraversr;
import com.bazaarvoice.jolt.shiftr.spec.ShiftrCompositeSpec;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Extractr is a frontend to Shiftr.
 * Useful for small Shifts, and command line tools.
 *
 */
public class Extractr implements SpecDriven, Transform {

    public static final String ROOT_KEY = "root";
    private final ShiftrCompositeSpec rootSpec;

    /**
     * Initialize an Extractor transform with a Spec.
     *
     * @throws com.bazaarvoice.jolt.exception.SpecException for a malformed spec
     */
    @Inject
    public Extractr( Object spec ) {

        if ( spec == null ){
            throw new SpecException( "Extractr expected a spec of Map type, got 'null'." );
        }
        if ( ! ( spec instanceof Map ) ) {
            throw new SpecException( "Extractr expected a spec of Map type, got " + spec.getClass().getSimpleName() );
        }

        Map<String, Object> extractrSpec = (Map) spec;

        Map<String, Object> generatedShiftrSpec = new HashMap<String, Object>();

        for( Map.Entry<String, Object> entry : extractrSpec.entrySet() ) {

            String path = entry.getKey();
            // TODO : verify path does not have any [] in it.
            // TODO : isNotBlank

            Object outputPathObj = entry.getValue();

            if ( outputPathObj instanceof String ) {
                String outputPath = (String) outputPathObj;
                // TODO : sanity check outputPath

                String[] pathSplit = path.split( "\\." );
                ShiftrTraversr shiftrTraversr = new ShiftrTraversr( path );

                shiftrTraversr.set( generatedShiftrSpec, Arrays.asList( pathSplit ), outputPath );
            }
            else {
                throw new SpecException( "Extractor spec must be a Map<String,String>.  Got key:" + path + " value:" + outputPathObj + " instead." );
            }
        }

        rootSpec = new ShiftrCompositeSpec( ROOT_KEY, generatedShiftrSpec );
    }


    /**
     * Applies the Shiftr transform built from the Extractr spec.
     *
     * @param input the JSON object to transform
     * @return the output object with data shifted to it
     * @throws com.bazaarvoice.jolt.exception.TransformException for a malformed spec or if there are issues during
     * the transform
     */
    @Override
    public Object transform( Object input ) {

        Map<String,Object> output = new HashMap<String,Object>();

        // Create a root LiteralPathElement so that # is useful at the root level
        LiteralPathElement rootLpe = new LiteralPathElement( ROOT_KEY );
        WalkedPath walkedPath = new WalkedPath();
        walkedPath.add( rootLpe );

        rootSpec.apply( ROOT_KEY, input, walkedPath, output );

        return output.get( ROOT_KEY );
    }
}
