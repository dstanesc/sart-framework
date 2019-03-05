package org.sartframework.query;

import java.util.HashMap;
import java.util.Map;

public class QueryVariables {

    public static class Variable {
        
        String name;
        
        String value;

        public Variable(String name, String value) {
            super();
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }
    
    private Map<String, String> content = new HashMap<>();
    
    
    public Map<String, String> getContent() {
        
        return content;
    }

    public final static QueryVariables of(Variable ... variables) {
        
        QueryVariables queryVariables = new QueryVariables();
        
        for (Variable variable : variables) {
            
            queryVariables.put(variable);
        }
       
        return queryVariables;
    }
    
    public void put(Variable variable) {
        
        content.put(variable.getName(), variable.getValue());
    }
    
    public final static Variable variable(String name, String value) {
        
        return new Variable(name, value);
    }
}
