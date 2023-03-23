package com.src.trade;

import com.src.trade.model.MatchResult;
import com.src.trade.model.Order;
import com.src.trade.model.OrderSide;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * This class represents an application that interacts with a {@link LimitOrderBook} to add, delete, modify, and execute trades.
 */
public class Application {

	 /**
     * The limit order book that this application interacts with.
     */
    private final LimitOrderBook lob;


    /**
     * Constructs a new Application with the given {@link LimitOrderBook}.
     *
     * @param lob the LimitOrderBook to interact with
     */
    public Application(LimitOrderBook lob) {
        this.lob = lob;
    }


    /**
     * Adds a new order to the limit order book.
     *
     * @param price the price of the order
     * @param quantity the quantity of the order
     * @param side the side of the order (BUY or SELL)
     * @return an Optional containing the newly added order if the operation was successful, or an empty Optional otherwise
     */
    public Optional<Order> addOrder(BigDecimal price, int quantity, OrderSide side) {
        if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        } else if (quantity <= 0) {
            return Optional.empty();
        }

        Order order = new Order(price, quantity, side);
        lob.add(order);
        return Optional.of(order);
    }

    /**
     * Deletes an order from the limit order book.
     *
     * @param id the ID of the order to delete
     * @return an Optional containing the deleted order if the operation was successful, or an empty Optional otherwise
     */
    public Optional<Order> deleteOrder(UUID id) {
        Order order = lob.getOrder(id);
        if (order == null) {
            return Optional.empty();
        }
        lob.delete(order);
        return Optional.of(order);
    }

    /**
     * Modifies the quantity of an existing order in the limit order book.
     *
     * @param id the ID of the order to modify
     * @param newQuantity the new quantity to set for the order
     * @return an Optional containing the modified order if the operation was successful, or an empty Optional otherwise
     */
    public Optional<Order> modifyOrder(UUID id, int newQuantity) {
        if (newQuantity <= 0) {
            return Optional.empty();
        }
        Order order = lob.getOrder(id);
        if (order == null) {
            return Optional.empty();
        }
        return Optional.of(lob.modify(order, newQuantity));
    }

    /**
     * Executes a trade at the given price and quantity and returns the result.
     *
     * @param price the price of the trade
     * @param quantity the quantity of the trade
     * @param side the side of the trade (BUY or SELL)
     * @return the result of the trade execution (FULL, PARTIAL, NONE, or ERROR)
     */
    public MatchResult executeTrade(BigDecimal price, int quantity, OrderSide side) {
        if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
            return MatchResult.ERROR;
        } else if (quantity <= 0) {
            return MatchResult.ERROR;
        }
        return lob.tryExecute(price, quantity, side);
    }
}