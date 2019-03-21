package org.sartframework.result;

import org.sartframework.annotation.Evolvable;

@Evolvable(version = 1)
public class EndResult extends MarkerResult {

    public EndResult() {
        super();
    }

    public EndResult(String resultKey) {
        super(resultKey);
    }

}
