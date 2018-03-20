package com.itsybitso.bl;

import com.itsybitso.entity.DiffOrder;
import com.itsybitso.entity.DiffOrderPayload;
import com.itsybitso.entity.InternalOrderBook;
import com.itsybitso.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import static com.itsybitso.entity.OrderStatus.*;

/**
 * this class has the business logic for processing a diff order from the queue
 *
 * @author laszlo
 */
public class DiffOrderProcessor extends BusinessLogic {

  private static final Logger LOGGER = LoggerFactory.getLogger(DiffOrderProcessor.class);
  private static final String BOOK = "btc_mxn";
  private static final String COMPLETED = "completed";
  private static final String CANCELLED = "cancelled";
  private static final String OPEN = "open";

  public static String updateInternalOrderBook(DiffOrder diffOrder) throws Exception {
    StringBuilder ordersProcessed = new StringBuilder();
    long diffOrderSequence = diffOrder.getSequence();
    long orderBookSequence = InternalOrderBook.getSequence();
    if (diffOrderSequence > orderBookSequence) {
      // there can be more than one payload in a diff order; for now we will handle these sequentially as this is not typical
      for (DiffOrderPayload diffOrderPayload : diffOrder.getPayload()) {
        Order previousOrder = applyPayload(diffOrderPayload);
        if (previousOrder != null) {
          ordersProcessed.append(" ").append(previousOrder.getOid()).append(" ")
              .append(previousOrder.getAmount()).append(" ").append(previousOrder.getPrice());
        }
      }
    }
    return ordersProcessed.toString();
  }

  private static Order applyPayload(DiffOrderPayload diffOrderPayload) throws Exception {
    Order previousOrder;
    switch (diffOrderPayload.getT()) {
      case 0:
        // = buy = update bids
        previousOrder = applyBid(diffOrderPayload);
        break;
      case 1:
        // = sell = update asks
        previousOrder = applyAsk(diffOrderPayload);
        break;
      default:
        throw new Exception("illegal value for diff-order type: " + diffOrderPayload.getT());
    }
    return previousOrder;

  }

  private static Order applyBid(DiffOrderPayload payload) throws Exception {
    Order previousOrder;
    switch (payload.getS()) {
      case CANCELLED:
      case COMPLETED:
        previousOrder = InternalOrderBook.removeBid(payload.getO());
        LOGGER.info("<<<<< B- " + payload.getO());
        break;
       case OPEN:
         previousOrder = InternalOrderBook.addBid(
             new Order(BOOK, payload.getR(), payload.getA(), payload.getO()));
         LOGGER.info("<<<<< B+ " + payload.getO());
         break;
       default:
         throw new Exception("illegal value for diff-order payload status: " + payload.getS());
     }
     return previousOrder;
   }

   private static Order applyAsk(DiffOrderPayload payload) throws Exception {
     Order previousOrder;
     switch (payload.getS()) {
       case CANCELLED:
       case COMPLETED:
         previousOrder = InternalOrderBook.removeAsk(payload.getO());
         LOGGER.info("<<<<< A- " + payload.getO());
        break;
      case OPEN:
        previousOrder = InternalOrderBook.addAsk(
            new Order(BOOK, payload.getR(), payload.getA(), payload.getO()));
        LOGGER.info("<<<<< A+ " + payload.getO());
        break;
      default:
        throw new Exception("illegal value for diff-order payload status: " + payload.getS());
    }
    return previousOrder;
  }

}
