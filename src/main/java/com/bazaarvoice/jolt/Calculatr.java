package com.bazaarvoice.jolt;

import java.util.Map;

/**
 * two-pass processor:
 * - pass 1, specify paths and an aggregation function (might be able to hand off control to Java impls here, too)
 * - pass 2, specify where to put it
 */
public class Calculatr implements Chainable {

    @Override
    public Object process( Object input, Map<String, Object> joltPipelineEntry )
            throws JoltException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
