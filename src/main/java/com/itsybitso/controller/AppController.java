package com.itsybitso.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsybitso.entity.InternalOrderBook;
import com.itsybitso.entity.OrderBook;
import com.itsybitso.entity.WindowConfig;
import com.itsybitso.executor.AppMonitors;
import com.itsybitso.executor.BitsoRestClient;
import com.itsybitso.executor.DiffOrderConsumer;

import com.itsybitso.executor.DiffOrderQueuer;
import com.itsybitso.executor.TradesPoller;
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
    @Path("service/gettrades")
    @Consumes("application/json")
    public Response getTrades() throws Exception {
        String f = TradesPoller.getInstance().startAsyncRefreshRecentTrades();
        // this returns right away to the REST caller
        return Response.accepted("started polling the recent trades from Bitso " + f).build();
    }

    @GET
    @Path("service/trade/{id}/{ups}/{downs}")
    @Consumes("application/json")
    public Response getTrades(@PathParam("id") String id, @PathParam("ups") String ups, @PathParam("downs") String downs) throws Exception {
        WindowConfig windowConfig = new WindowConfig(id, ups, downs);
        String f = TradesPoller.getInstance().addNewConfiguration(windowConfig);
        // this returns right away to the REST caller
        return Response.accepted("started polling the recent trades from Bitso with " + ups + " and " + downs + " " + f).build();
    }

    @GET
    @Path("service/orderbook")
    @Consumes("application/json")
    public Response createInternalOrderBook() throws Exception {
        OrderBook orderBook;
        if (!InternalOrderBook.initialized()) {
            orderBook = BitsoRestClient.getBitsoOrderBook();
            InternalOrderBook.initialize(orderBook);
        } else {
            orderBook = InternalOrderBook.getOrderBook();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        // this returns right away to the REST caller
        return Response.accepted(objectMapper.writeValueAsString(orderBook)).build();
    }

    @GET
    @Path("service/populatequeue")
    @Consumes("application/json")
    public Response runPopulateQueue() throws Exception {
        String f = DiffOrderQueuer.startAsyncPopulateQueue();
        // this returns right away to the REST caller
        return Response.accepted("Hello Stranger! POPULATE " +
                " has started and God willing, will continue to run forever.. " + f).build();
    }


    @GET
    @Path("service/consumequeue")
    @Consumes("application/json")
    public Response runConsumeQueue() throws Exception {
        String f = DiffOrderConsumer.startAsyncConsumeQueue();
        // this returns right away to the REST caller
        return Response.accepted("Hello Stranger! CONSUME " +
                " has started and God willing, will continue to run forever.. " + f).build();
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
        String f = DiffOrderConsumer.startAsyncConsumeQueue();

        // this returns right away to the REST caller
        return Response.accepted("Hello Stranger! Your job CONSUME " +
                " has started and God willing, will complete successfully in due time.. " + f).build();
    }

    @GET
    @Path("service/startmonitor")
    @Consumes("application/json")
    public Response runMonitor() throws Exception {
        String currentMonitor = AppMonitors.startMonitoring();
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
