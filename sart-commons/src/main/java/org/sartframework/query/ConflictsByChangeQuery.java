package org.sartframework.query;

import org.sartframework.annotation.Evolvable;

@Evolvable(version = 1)
public class ConflictsByChangeQuery extends AbstractQuery {

    final String changeKey;

    public ConflictsByChangeQuery(String changeKey) {
        super();
        this.changeKey = changeKey;
    }

    public boolean matches(String changeKey) {

        return this.changeKey.equals(changeKey);
    }

    public String getChangeKey() {
        return changeKey;
    }

}
