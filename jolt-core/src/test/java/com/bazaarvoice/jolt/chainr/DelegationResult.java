package com.bazaarvoice.jolt.chainr;

public class DelegationResult {

    public Object input;
    public Object spec;

    DelegationResult(Object input, Object spec) {
        this.input = input;
        this.spec = spec;
    }
}
