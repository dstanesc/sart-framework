package org.sartframework.event.transaction;

public class RemoteDomainEvent {

    String content;

    public RemoteDomainEvent() {
        super();
    }

    public RemoteDomainEvent(String content) {
        super();
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "RemoteDomainEvent [content=" + content + "]";
    }

}
