package org.sartframework.serializers;

public class DuplicateEvolvableSpecification extends RuntimeException {

    private static final long serialVersionUID = -2073448116668802229L;

    private int version;

    private String identity;

    public DuplicateEvolvableSpecification(int version, String identity) {
        super();
        this.version = version;
        this.identity = identity;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    @Override
    public String getMessage() {

        return "Duplicate @Evolvable declaration. Please correct. @Evolvable(identity=" + getIdentity() + ", version = " + getVersion() + ")";
    }

}
