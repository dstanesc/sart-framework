package org.sartframework.serializers;

public class SerializedStructure {

    String structureIdentity;
    
    int structureVersion;

    byte[] payload;


    public SerializedStructure() {
        super();
    }


    public SerializedStructure(String structureIdentity, int structureVersion, byte[] payload) {
        super();
        this.structureIdentity = structureIdentity;
        this.structureVersion = structureVersion;
        this.payload = payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }


    public byte[] getPayload() {
        
        return payload;
    }


    public String getStructureIdentity() {
        return structureIdentity;
    }


    public void setStructureIdentity(String structureIdentity) {
        this.structureIdentity = structureIdentity;
    }


    public int getStructureVersion() {
        return structureVersion;
    }


    public void setStructureVersion(int structureVersion) {
        this.structureVersion = structureVersion;
    }

}
