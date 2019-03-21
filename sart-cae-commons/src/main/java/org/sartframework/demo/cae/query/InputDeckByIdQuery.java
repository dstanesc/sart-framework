package org.sartframework.demo.cae.query;

import org.sartframework.annotation.Evolvable;
import org.sartframework.query.AbstractQuery;

@Evolvable(identity="cae.query.InputDeckById", version = 1)
public class InputDeckByIdQuery extends AbstractQuery {

    String inputDeckId;
    
    public InputDeckByIdQuery() {
        super();
    }

    public InputDeckByIdQuery(String inputDeckId) {
        super();
        this.inputDeckId = inputDeckId;
    }
    
    public String getInputDeckId() {
        return inputDeckId;
    }

    public void setInputDeckId(String inputDeckId) {
        this.inputDeckId = inputDeckId;
    }

    public boolean matches(String inputDeckId) {
        return this.inputDeckId.equals(inputDeckId);
    }

}
