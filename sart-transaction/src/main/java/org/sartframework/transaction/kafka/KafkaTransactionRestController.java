package org.sartframework.transaction.kafka;

import org.sartframework.transaction.BusinessTransactionManager;
import org.sartframework.transaction.TransactionRestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KafkaTransactionRestController extends TransactionRestController {

    final static Logger LOGGER = LoggerFactory.getLogger(KafkaTransactionRestController.class);

    public KafkaTransactionRestController(BusinessTransactionManager txnManager) {
        super(txnManager);
    }

}
