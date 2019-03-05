package org.sartframework.projection;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface ConflictResolutionRepository extends JpaRepository<ConflictResolutionEntity, UUID>{
    
    List<ConflictResolutionEntity> findByAggregateKey(String aggregateKey);
    
    List<ConflictResolutionEntity> findByChangeKey(String changeKey);

    @Query(value = "SELECT * FROM CONFLICT_RESOLUTION_ENTITY WHERE WINNER_XID = ?1 OR OTHER_XID = ?1", nativeQuery = true)
    List<ConflictResolutionEntity> findByXid(long xid);
}
