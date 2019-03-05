package org.sartframework.demo.cae.projection;

import java.util.List;

import org.sartframework.projection.EntityIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InputDeckRepository extends JpaRepository<InputDeckEntity, EntityIdentity> {
   
    List<InputDeckEntity> findByAggregateKey(String inputDeckId);
    
    List<InputDeckEntity> findByXmin(long xid);
    
    List<InputDeckEntity> findByInputDeckName(String inputDeckName);
    
    List<InputDeckEntity> findByInputDeckFile(String inputDeckFile);
}
