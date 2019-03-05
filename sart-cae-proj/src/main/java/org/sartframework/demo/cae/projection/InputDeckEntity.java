package org.sartframework.demo.cae.projection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;

import org.sartframework.projection.ProjectedEntity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(include = com.fasterxml.jackson.annotation.JsonTypeInfo.As.WRAPPER_OBJECT, use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME)

@Entity
public class InputDeckEntity extends ProjectedEntity {

    private String inputDeckName;

    private String inputDeckFile;

    @ManyToMany(targetEntity = ResultEntity.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ResultEntity> results=new ArrayList<>();

    public InputDeckEntity() {
        super();
    }

    public InputDeckEntity(long xid, String inputDeckId, long inputDeckVersion, String inputDeckName, String inputDeckFile) {
        super(xid, inputDeckId, inputDeckVersion);
        this.inputDeckName = inputDeckName;
        this.inputDeckFile = inputDeckFile;
    }

    public InputDeckEntity copy(long xid, long inputDeckVersion) {

        InputDeckEntity inputDeckEntityCopy = new InputDeckEntity(xid, this.aggregateKey, inputDeckVersion, this.inputDeckName, this.inputDeckFile);
        
        getResults().forEach(result -> inputDeckEntityCopy.addResult(result));
        
        return inputDeckEntityCopy;
    }

    public String getInputDeckName() {
        return inputDeckName;
    }

    public void setInputDeckName(String inputDeckName) {
        this.inputDeckName = inputDeckName;
    }

    public String getInputDeckFile() {
        return inputDeckFile;
    }

    public void setInputDeckFile(String inputDeckFile) {
        this.inputDeckFile = inputDeckFile;
    }

    public List<ResultEntity> getResults() {
        return results;
    }

    public void setResults(List<ResultEntity> results) {
        this.results = results;
    }

    public void addResult(ResultEntity result) {

        this.results.add(result);
        result.getInputDecks().add(this);
    }

    // FIXME, this is conceptually wrong, we just want to have flag update
    // on delete/remove not actual removes to simplify conflict resolution

    public void removeResult(String resultId, long xid) {

        Iterator<ResultEntity> iterator = this.results.iterator();

        while (iterator.hasNext()) {
            ResultEntity next = iterator.next();
            if (next.getAggregateKey().equals(resultId)) {
                next.setXmax(xid);
            }
        }
    }
}
