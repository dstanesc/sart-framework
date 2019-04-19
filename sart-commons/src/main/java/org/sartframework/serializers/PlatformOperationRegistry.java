package org.sartframework.serializers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.sartframework.aggregate.DomainAggregate;
import org.sartframework.aggregate.TransactionAggregate;
import org.sartframework.command.DomainCommand;
import org.sartframework.command.transaction.TransactionCommand;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.QueryEvent;
import org.sartframework.event.TransactionEvent;
import org.sartframework.query.DomainQuery;
import org.sartframework.result.QueryResult;
import org.sartframework.serializers.protostuff.ContentSerializerProtostuff;
import org.sartframework.session.RunningTransactions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PlatformOperationRegistry {

    final static Logger LOGGER = LoggerFactory.getLogger(PlatformOperationRegistry.class);

    public static PlatformOperationRegistry instance;

    Map<Class<?>, EvolvableStructure<?>> versionsByJavaClass = new HashMap<>();

    Map<String, VersionChain<?>> versionsByIdentity = new HashMap<>();

    Map<Class<?>, Adapter<?>> adapterRegistry = new HashMap<>();
    
    Class<?> defaultContentSerializer;
    
    public final static PlatformOperationRegistry get() {

        return instance;
    }

    public PlatformOperationRegistry() {
        super();
    }

    @PostConstruct
    public final PlatformOperationRegistry registerDefaultsAndInit() {

        registerDefaults();

        return init();
    }

    @SuppressWarnings("static-access")
    public final PlatformOperationRegistry init() {

        this.instance = this;

        return this;
    }

    public PlatformOperationRegistry registerDefaultContentSerializer(Class<?> defaultContentSerializer) {
    
        this.defaultContentSerializer = defaultContentSerializer;
        
        return this;
    }
    
    public Class<?> getDefaultContentSerializer() {
        
        
        return  defaultContentSerializer;
    }
    
    public PlatformOperationRegistry registerDefaults() {
        
        registerDefaultContentSerializer(ContentSerializerProtostuff.class);

        registerOperationCategory(DomainAggregate.class, "org.sartframework");
        registerOperationCategory(DomainCommand.class, "org.sartframework");
        registerOperationCategory(DomainEvent.class, "org.sartframework");
        registerOperationCategory(DomainQuery.class, "org.sartframework");
        registerOperationCategory(QueryResult.class, "org.sartframework");
        registerOperationCategory(QueryEvent.class, "org.sartframework");
        registerOperationCategory(TransactionAggregate.class, "org.sartframework");
        registerOperationCategory(TransactionCommand.class, "org.sartframework");
        registerOperationCategory(TransactionEvent.class, "org.sartframework");
        registerOperationCategory(RunningTransactions.class, "org.sartframework");

        return this;
    }

    public final <T> PlatformOperationRegistry registerAdapter(Class<T> operation, Adapter<T> adapter) {

        adapterRegistry.put(operation, adapter);

        return this;
    }
    
    
    public final boolean hasAdapter(Class<?> operation) {
        
        return adapterRegistry.containsKey(operation);
    }

    @SuppressWarnings("unchecked")
    public final <T> Adapter<T> getAdapter(Class<T> operation) {

        return (Adapter<T>) adapterRegistry.get(operation);
    }
    
    public final PlatformOperationRegistry registerOperationCategory(Class<?> category, String basePackage) {

        LOGGER.debug("Scanning category {} in pkg {} ", category, basePackage);

        scanAndRegisterCategory(category, basePackage);

        return this;
    }

    @SuppressWarnings("unchecked")
    public final <T> EvolvableStructure<T> getEvolvableStructureByJavaClass(Class<T> javaType) {

        return (EvolvableStructure<T>) versionsByJavaClass.get(javaType);
    }

    @SuppressWarnings("unchecked")
    public final <T> ContentSerializer<T> getContentSerializer(String structureIdentity, int structureVersion, boolean latestAvailable) {

        VersionChain<T> versionChain = (VersionChain<T>) versionsByIdentity.get(structureIdentity);

        if (versionChain == null)
            throw new UnsupportedOperationException("Cannot find serializer for " + structureIdentity);

        EvolvableStructure<T> versionedStructure = latestAvailable ? versionChain.getLast() : versionChain.getVersion(structureVersion);

        if (latestAvailable) {
            
            LOGGER.debug("ContentSerializer chosen for latest available structure {}", versionedStructure);
        }
        
        return versionedStructure.getContentSerializer();
    }

    public final void scanAndRegisterCategory(Class<?> category, String basePackage) {

        new VersionedStructureScanner<>(category).scanAndRegister(basePackage, this);
    }

    @SuppressWarnings("unchecked")
    public final <T> void registerEvolvableStructure(Class<T> serializableStructure, String structureIdentity, int structureVersion,
                                                     ContentSerializer<T> contentSerializer) {

        LOGGER.debug("Registering structure {}, id={}, ver={}, ser={}", serializableStructure, structureIdentity, structureVersion,
            contentSerializer.getClass());

        EvolvableStructure<T> versionedStructure = new EvolvableStructure<T>(structureIdentity, structureVersion, contentSerializer);

        versionsByJavaClass.put(serializableStructure, versionedStructure);

        VersionChain<T> versionedChain = (VersionChain<T>) versionsByIdentity.get(structureIdentity);

        if (versionedChain == null) {
            versionedChain = new VersionChain<>();
            versionsByIdentity.put(structureIdentity, versionedChain);
        }

        versionedChain.add(versionedStructure);
    }
}
