package org.sartframework.serializers;

public class EvolvableStructure<T> implements Comparable<EvolvableStructure<T>> {

    String identity;
    
    int version;
    
    ContentSerializer<T> contentSerializer;

    public EvolvableStructure() {
        super();
    }

    public EvolvableStructure(String identity, int version, ContentSerializer<T> contentSerializer) {
        super();
        this.identity = identity;
        this.version = version;
        this.contentSerializer = contentSerializer;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public int getVersion() {
        return version;
    }

    public String getIdentity() {
        return identity;
    }

    public ContentSerializer<T> getContentSerializer() {
        return contentSerializer;
    }

    public void setContentSerializer(ContentSerializer<T> contentSerializer) {
        this.contentSerializer = contentSerializer;
    }

    @Override
    public int compareTo(EvolvableStructure<T> other) {
        
        return Integer.compare(getVersion(), other.getVersion());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identity == null) ? 0 : identity.hashCode());
        result = prime * result + version;
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
        EvolvableStructure<T> other = (EvolvableStructure<T>) obj;
        if (identity == null) {
            if (other.identity != null)
                return false;
        } else if (!identity.equals(other.identity))
            return false;
        if (version != other.version)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EvolvableStructure [identity=" + identity + ", version=" + version + ", contentSerializer=" + contentSerializer + "]";
    }

}
