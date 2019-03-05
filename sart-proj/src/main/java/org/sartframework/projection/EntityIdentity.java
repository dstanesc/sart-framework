package org.sartframework.projection;

import java.io.Serializable;

public class EntityIdentity implements Serializable {

    private static final long serialVersionUID = -8292162666506866924L;

    String aggregateKey;
    
    long aggregateVersion;
    
    public EntityIdentity() { }

    public EntityIdentity(String id, long version) {
        super();
        this.aggregateKey = id;
        this.aggregateVersion = version;
    }

    public String getAggregateKey() {
        return aggregateKey;
    }

    public void setAggregateKey(String inputDeckId) {
        this.aggregateKey = inputDeckId;
    }

    public long getAggregateVersion() {
        return aggregateVersion;
    }

    public void setAggregateVersion(long inputDeckVersion) {
        this.aggregateVersion = inputDeckVersion;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aggregateKey == null) ? 0 : aggregateKey.hashCode());
        result = prime * result + (int) (aggregateVersion ^ (aggregateVersion >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EntityIdentity other = (EntityIdentity) obj;
        if (aggregateKey == null) {
            if (other.aggregateKey != null)
                return false;
        } else if (!aggregateKey.equals(other.aggregateKey))
            return false;
        if (aggregateVersion != other.aggregateVersion)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EntityIdentity [aggregateKey=" + aggregateKey + ", aggregateVersion=" + aggregateVersion + "]";
    }

}
