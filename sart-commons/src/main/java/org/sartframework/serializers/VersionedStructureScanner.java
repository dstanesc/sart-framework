package org.sartframework.serializers;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.sartframework.annotation.Evolvable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

public class VersionedStructureScanner<T> {
    
    final static Logger LOGGER = LoggerFactory.getLogger(VersionedStructureScanner.class);
    
    class AndFilter implements TypeFilter {

        final TypeFilter left;

        final TypeFilter right;

        public AndFilter(TypeFilter left, TypeFilter right) {
            super();
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {

            return left.match(metadataReader, metadataReaderFactory) && right.match(metadataReader, metadataReaderFactory);
        }
    }
    
    final Class<T> filteredType;

    public VersionedStructureScanner(Class<T> filteredType) {
        super();
        this.filteredType = filteredType;
    }

    @SuppressWarnings("unchecked")
    public void scanAndRegister(String basePackage, PlatformOperationRegistry serializerRegistry) {

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AndFilter(new AnnotationTypeFilter(Evolvable.class), new AssignableTypeFilter(filteredType)));

        Set<BeanDefinition> structureDefinitions = scanner.findCandidateComponents(basePackage);

        try {
            
            for (BeanDefinition structureDefinition : structureDefinitions) {

                String structureClassName = structureDefinition.getBeanClassName();
                
                LOGGER.info("Found matching structure {}", structureClassName);

                Class<T> structureClass = (Class<T>) Class.forName(structureClassName);

                Evolvable evolvableStructure = structureClass.getAnnotation(Evolvable.class);

                String structureIdentity = evolvableStructure.identity();

                if (structureIdentity.trim().length() == 0) {
                 
                    structureIdentity = structureClassName;
                }

                int structureVersion = evolvableStructure.version();
                
                Class<? extends ContentSerializer<T>> serializer = (Class<? extends ContentSerializer<T>>) evolvableStructure.serializer();
                
                if(UnspecifiedContentSerializer.class.isAssignableFrom(serializer)) {
                    
                    serializer = (Class<? extends ContentSerializer<T>>) serializerRegistry.getDefaultContentSerializer();
                }
                
                Constructor<? extends ContentSerializer<T>> constructor = serializer.getConstructor(Class.class);
                
                ContentSerializer<T> contentSerializer = constructor.newInstance(structureClass);

                serializerRegistry.registerEvolvableStructure(structureClass, structureIdentity, structureVersion, contentSerializer);
            }
            
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
