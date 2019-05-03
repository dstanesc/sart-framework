package org.sartframework.aggregate;

public class DestructibleEntity {

    protected Long xmax;

    public void destruct(Long xmax) {
        setXmax(xmax);
    }
    
    public Long getXmax() {
        return xmax;
    }

    public void setXmax(Long xmax) {
        this.xmax = xmax;
    }
}
