package org.sartframework.transaction;

public interface DetailFactory<T extends AbstractDetail> {

    T collect(String name);
}
