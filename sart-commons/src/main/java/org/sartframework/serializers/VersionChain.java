package org.sartframework.serializers;

import java.util.TreeMap;

public class VersionChain<T> {

    TreeMap<Integer, EvolvableStructure<T>> chain;
    
    public VersionChain() {
        
        chain = new TreeMap<Integer,  EvolvableStructure<T>>();
    }

    public void add(EvolvableStructure<T> versionedStructure) {
        
        chain.put(versionedStructure.getVersion(), versionedStructure);
    }
    
    public EvolvableStructure<T> getLast() {
        
         Integer lastVersion = chain.lastKey();
         
         return chain.get(lastVersion);
    }
    
    public EvolvableStructure<T> getVersion(int version) {
 
        return chain.get(version);
   }
}
