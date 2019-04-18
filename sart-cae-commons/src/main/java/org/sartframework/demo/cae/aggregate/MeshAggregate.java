package org.sartframework.demo.cae.aggregate;

import org.sartframework.aggregate.AnnotatedDomainAggregate;
import org.sartframework.annotation.Evolvable;

@Evolvable(identity="cae.aggregate.MeshAggregate", version = 1)
public class MeshAggregate extends AnnotatedDomainAggregate {

}
