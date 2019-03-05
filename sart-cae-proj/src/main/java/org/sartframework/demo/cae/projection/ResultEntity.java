package org.sartframework.demo.cae.projection;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import org.sartframework.projection.SimpleDomainEntity;

@Entity
public class ResultEntity extends SimpleDomainEntity {

    private String resultName;
    
    private String resultFile;
    
    @ManyToMany(mappedBy = "results")
    private List<InputDeckEntity> inputDecks = new ArrayList<>();
    
    public ResultEntity() {
        super();
    }

    public ResultEntity(long xid, String resultId, String resultName, String resultFile) {
        super(xid, resultId);
        this.resultName = resultName;
        this.resultFile = resultFile;
    }

    public List<InputDeckEntity> getInputDecks() {
        return inputDecks;
    }

    public void setInputDecks(List<InputDeckEntity> inputDecks) {
        this.inputDecks = inputDecks;
    }

    public String getResultName() {
        return resultName;
    }


    public void setResultName(String resultName) {
        this.resultName = resultName;
    }


    public String getResultFile() {
        return resultFile;
    }

    public void setResultFile(String resultFile) {
        this.resultFile = resultFile;
    }
}
