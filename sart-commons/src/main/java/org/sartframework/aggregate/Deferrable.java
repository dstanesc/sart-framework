package org.sartframework.aggregate;

@FunctionalInterface
public interface Deferrable {

    void execute();
}
