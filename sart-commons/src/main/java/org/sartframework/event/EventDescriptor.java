package org.sartframework.event;

public class EventDescriptor {

    String eventName;
    
    String eventDetail;

    Long xid;

    Long xcs;

    Long creationTime;

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Long getXid() {
        return xid;
    }

    public void setXid(Long xid) {
        this.xid = xid;
    }

    public Long getXcs() {
        return xcs;
    }

    public void setXcs(Long xcs) {
        this.xcs = xcs;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }
    
    public String getEventDetail() {
        return eventDetail;
    }

    public void setEventDetail(String eventDetail) {
        this.eventDetail = eventDetail;
    }


}
