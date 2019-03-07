package org.sartframework.demo.cae.query;

import org.sartframework.query.AbstractQuery;

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
