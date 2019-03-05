package org.sartframework.config;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sart.config")
public class SartConfiguration {

    public static class Projections {

        @NotNull
        Conflict conflict;

        public Conflict getConflict() {
            return conflict;
        }

        public void setConflict(Conflict conflict) {
            this.conflict = conflict;
        }
    }

    public static class Conflict {

        @NotNull
        Listener listener;

        public Listener getListener() {
            return listener;
        }

        public void setListener(Listener listener) {
            this.listener = listener;
        }
    }

    public static class Transaction {

        @NotNull
        Listener listener;

        public Listener getListener() {
            return listener;
        }

        public void setListener(Listener listener) {
            this.listener = listener;
        }
    }

    public static class Listener {

        @NotEmpty
        String name;

        @NotEmpty
        Integer port;

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String toConnectString() {
            return name + ':' + port;
        }
    }

    public static class Zookeeper {

        Listener listener;

        public Listener getListener() {
            return listener;
        }

        public void setListener(Listener listener) {
            this.listener = listener;
        }
    }

    @NotNull
    Projections projections;

    @NotNull
    Transaction transaction;

    @NotNull
    Zookeeper zookeeper;

    public Projections getProjections() {
        return projections;
    }

    public void setProjections(Projections projections) {
        this.projections = projections;
    }

    public Listener getConflictListener() {

        return projections.getConflict().getListener();
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Listener getTransactionListener() {

        return transaction.getListener();
    }

    public Listener getZookeeperListener() {

        return zookeeper.getListener();
    }

}
