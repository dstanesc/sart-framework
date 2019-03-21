package org.sartframework.demo.cae.query;

import org.sartframework.annotation.Evolvable;
import org.sartframework.query.AbstractQuery;

@Evolvable(identity="cae.query.InputDeckByName", version = 1)
public class InputDeckByNameQuery extends AbstractQuery {

    String inputDeckName;


    public InputDeckByNameQuery() {
        super();
    }

    public InputDeckByNameQuery(String inputDeckName) {
        super();
        this.inputDeckName = inputDeckName;
    }

    public boolean matches(String inputDeckName) {

        return this.inputDeckName.equals(inputDeckName);
    }

    public String getInputDeckName() {
        return inputDeckName;
    }

    
    public void setInputDeckName(String inputDeckName) {
        this.inputDeckName = inputDeckName;
    }

}
