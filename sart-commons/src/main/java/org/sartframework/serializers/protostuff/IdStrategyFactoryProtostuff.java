package org.sartframework.serializers.protostuff;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.protostuff.runtime.ExplicitIdStrategy;
import io.protostuff.runtime.IdStrategy;

public class IdStrategyFactoryProtostuff implements IdStrategy.Factory {

    ExplicitIdStrategy.Registry r = new ExplicitIdStrategy.Registry();

    TreeMap<Integer, Class<?>> localRegistry = new TreeMap<Integer, Class<?>>();

    Set<Class<?>> trace = new HashSet<>();

    AtomicInteger counter = new AtomicInteger(0);

    @Override
    public IdStrategy create() {
        System.out.println("EXPLICIT @IdStrategyFactoryProtostuff");
        return r.strategy;
    }

    @Override
    public void postCreate() {
        Set<Entry<Integer, Class<?>>> entrySet = localRegistry.entrySet();
        for (Entry<Integer, Class<?>> entry : entrySet) {
            r.registerPojo(entry.getValue(), entry.getKey());
        }
    }

    public void add(Class<?> clazz) {
        if (!trace.contains(clazz)) {
            trace.add(clazz);
            localRegistry.put(counter.incrementAndGet(), clazz);
        }
    }
}
