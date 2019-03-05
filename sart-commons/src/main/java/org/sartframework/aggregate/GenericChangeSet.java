package org.sartframework.aggregate;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SortedMap;
import java.util.stream.Collectors;

import org.sartframework.command.DomainCommand;
import org.sartframework.event.DomainEvent;

public class GenericChangeSet implements ChangeSet {

    final SortedMap<Long, Queue<DomainEvent<? extends DomainCommand>>> changesByVersion;

    Map<?, List<DomainEvent<? extends DomainCommand>>> changesByAspect;

    public GenericChangeSet(SortedMap<Long, Queue<DomainEvent<? extends DomainCommand>>> changesByVersion) {
        super();
        this.changesByVersion = changesByVersion;
        this.changesByAspect = changesByVersion.entrySet().stream().map(e -> e.getValue()).flatMap(q -> q.stream())
            .collect(Collectors.groupingBy(DomainEvent<? extends DomainCommand>::getChangeKey));
    }

    @Override
    public boolean containsChanges(Object aspect) {

        return changesByAspect.containsKey(aspect);
    }

    @Override
    public List<DomainEvent<?>> getChanges(Object aspect) {

        return changesByAspect.get(aspect);
    }

}
