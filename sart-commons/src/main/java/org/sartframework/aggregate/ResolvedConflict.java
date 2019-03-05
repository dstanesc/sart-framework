package org.sartframework.aggregate;

public interface ResolvedConflict extends Conflict {

    boolean isCurrentChangeApplied();
}
