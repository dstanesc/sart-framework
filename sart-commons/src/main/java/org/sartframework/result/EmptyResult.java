package org.sartframework.result;

import org.sartframework.annotation.Evolvable;

@Evolvable(version = 1)
public class EmptyResult extends  MarkerResult {

    public EmptyResult() {
        super();
    }

    public EmptyResult(String resultKey) {
        super(resultKey);
    }

}
