package com.itsybitso.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsybitso.entity.InternalOrderBook;
import com.itsybitso.entity.OrderBook;
import com.itsybitso.executor.AppMonitor;
import com.itsybitso.executor.DiffOrderConsumer;

import com.itsybitso.executor.DiffOrderQueuer;
import com.itsybitso.util.BitsoHealthCheck;
import com.itsybitso.util.HealthCheck;
import com.itsybitso.util.HealthResult;
import com.itsybitso.util.HealthStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * REST endpoint to trigger execution of the processes.
 * 
 * ex: http://localhost:8080/itsybitso/service/populatequeue
 * http://localhost:8080/itsybitso/service/consumequeue
 * http://localhost:8080/itsybitso/service/consumewith/4
 * 
 * @author laszlo
 */
@Path("/")
public class AppController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppController.class);

    @GET
    @Path("service/orderbook")
    @Consumes("application/json")
    public Response createInternalOrderBook() throws Exception {
        OrderBook orderBook = BitsoRestClient.GetBitsoOrderBook();
        InternalOrderBook.initialize(orderBook);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValueAsString(orderBook);
        // this returns right away to the REST caller
        return Response.accepted(objectMapper.writeValueAsString(orderBook)).build();
    }

    @GET
    @Path("service/populatequeue")
    @Consumes("application/json")
    public Response runPopulateQueue() throws Exception {
        Future<String> f = DiffOrderQueuer.startAsyncPopulateQueue();
        // this returns right away to the REST caller
        return Response.accepted("Hello Stranger! POPULATE " +
                " has started and God willing, will continue to run forever.. ").build();
    }


    @GET
    @Path("service/consumequeue")
    @Consumes("application/json")
    public Response runConsumeQueue() throws Exception {
        Future<String> f = DiffOrderConsumer.startAsyncConsumeQueue();
        // this returns right away to the REST caller
        return Response.accepted("Hello Stranger! CONSUME " +
                " has started and God willing, will continue to run forever.. ").build();
    }

    @GET
    @Path("service/consumewith/{consumeWith}")
    @Consumes("application/json")
    public Response runConsumeWith(@PathParam("consumeWith") String consumeWith) throws Exception {

        // switch the pool
        try {
            DiffOrderConsumer.switchPool(consumeWith);
        } catch (NumberFormatException e) {
            return Response.accepted("non-int argument passed for number of threads.. Try again!").build();
        }

        // start consuming again
        Future<String> f = DiffOrderConsumer.startAsyncConsumeQueue();

        // this returns right away to the REST caller
        return Response.accepted("Hello Stranger! Your job CONSUME " +
                " has started and God willing, will complete successfully in due time.. ").build();
    }

    @GET
    @Path("service/startmonitor")
    @Consumes("application/json")
    public Response runMonitor() throws Exception {
        String currentMonitor = AppMonitor.startAppMonitor();
        return Response.accepted(currentMonitor).build();
    }

    @GET
    @Path("service/monitor")
    @Consumes("application/json")
    public Response monitor() throws Exception {
        String currentMonitor = AppMonitor.getCurrentMonitor();
        return Response.accepted(currentMonitor).build();
    }

    @GET
    @Path("admin/health/check")
    @Produces("application/json")
    public Response healthCheck() throws Exception {

        List<HealthResult> healthResultList = new ArrayList<>();

        HealthCheck healthCheck = new BitsoHealthCheck();
        String healthCheckName = healthCheck.getName();
        HealthStatus healthStatus = healthCheck.check();
        healthResultList.add(new HealthResult(healthCheckName, healthStatus));

        if (healthResultList.size() == 0) {
            return Response.noContent().build();
        }

        GenericEntity<List<HealthResult>> result = new GenericEntity<List<HealthResult>>(healthResultList) {
        };
        return Response.ok(result).build();
    }

}
