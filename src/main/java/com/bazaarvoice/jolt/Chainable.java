package com.bazaarvoice.jolt;

import java.util.Map;

public interface Chainable {

    Object process(Object input, Map<String, Object> joltPipelineEntry)
            throws JoltException;

}
