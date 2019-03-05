package org.sartframework.demo.cae.query;

import static org.sartframework.query.QueryVariables.of;
import static org.sartframework.query.QueryVariables.variable;

import org.sartframework.query.AbstractQuery;
import org.sartframework.query.QueryVariables;
import org.sartframework.query.QueryVariables.Variable;

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
