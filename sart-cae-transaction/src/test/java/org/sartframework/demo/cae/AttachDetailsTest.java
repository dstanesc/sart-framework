package org.sartframework.demo.cae;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.sartframework.demo.cae.client.RestSimulationApi;
import org.sartframework.demo.cae.command.InputDeckCreateCommand;
import org.sartframework.driver.DefaultRestTransactionDriver;
import org.sartframework.driver.RestConflictQueryApi;
import org.sartframework.driver.RestTransactionApi;
import org.sartframework.driver.TransactionDriver;
import org.sartframework.transaction.TraceDetail;
import org.sartframework.transaction.TransactionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttachDetailsTest extends AbstractCaeTest {

    final static Logger LOGGER = LoggerFactory.getLogger(AttachDetailsTest.class);
    
    @Test
    public void testTransactionAttachTraceDetails() throws Exception {

        TransactionDriver driver = new DefaultRestTransactionDriver()
            .registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestConflictQueryApi())
            .registerCommandApi(new RestSimulationApi())
            .init();

        CompletableFuture<TransactionDetails> detailsLock = new CompletableFuture<>();

        
        String i = nextInputDeckIdentity();
        
        driver.createDomainTransaction()
        
        .onDetailsAttached(e -> {
            
            TransactionDetails details = e.getDetails();
            
            detailsLock.complete(details);
        })

        .attachTraceDetails(TraceDetail.START_TRACE)

        .executeCommand(() -> {
            
            return new InputDeckCreateCommand(i, "input-deck-name-" + i, "input-deck-file-" + i);
        });
        
        TransactionDetails transactionDetails = detailsLock.get(10, TimeUnit.SECONDS);
        
        Optional<TraceDetail> traceDetailOptional = transactionDetails.<TraceDetail>getDetail(TraceDetail.START_TRACE);
        
        Assert.assertTrue(traceDetailOptional.isPresent());
        
        TraceDetail traceDetail = traceDetailOptional.get();
        
        Assert.assertEquals("org.sartframework.demo.cae.AttachDetailsTest", traceDetail.getClassName());
        
        Assert.assertEquals("testTransactionAttachTraceDetails", traceDetail.getMethodName());
        
        Assert.assertEquals(47, traceDetail.getLineNumber());
    }
    
    
    @Test
    public void testDriverAttachTraceDetails() throws Exception {

        TransactionDriver driver = new DefaultRestTransactionDriver()
            .registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestConflictQueryApi())
            .registerCommandApi(new RestSimulationApi())
            .attachTraces()
            .init();

        CompletableFuture<TransactionDetails> detailsLock = new CompletableFuture<>();

        
        String i = nextInputDeckIdentity();
        
        driver.createDomainTransaction()
        
        .onDetailsAttached(e -> {
            
            TransactionDetails details = e.getDetails();
            
            detailsLock.complete(details);
        })
        
        .executeCommand(() -> {
            
            return new InputDeckCreateCommand(i, "input-deck-name-" + i, "input-deck-file-" + i);
        });
        
        TransactionDetails transactionDetails = detailsLock.get(10, TimeUnit.SECONDS);
        
        Optional<TraceDetail> traceDetailOptional = transactionDetails.<TraceDetail>getDetail(TraceDetail.START_TRACE);
        
        Assert.assertTrue(traceDetailOptional.isPresent());
        
        TraceDetail traceDetail = traceDetailOptional.get();
        
        Assert.assertEquals("org.sartframework.demo.cae.AttachDetailsTest", traceDetail.getClassName());
        
        Assert.assertEquals("testDriverAttachTraceDetails", traceDetail.getMethodName());
        
        Assert.assertEquals(85, traceDetail.getLineNumber());
    }


}
