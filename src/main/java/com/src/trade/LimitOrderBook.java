package com.src.trade;

import com.src.trade.model.MatchResult;
import com.src.trade.model.Order;
import com.src.trade.model.OrderSide;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

import static java.util.stream.Collectors.joining;

/**
 * The LimitOrderBook class represents an order book for limit orders.
 * It stores limit orders in two priority queues for buy and sell orders, respectively.
 */
public class LimitOrderBook {

	// map to store orders by UUID
    private final Map<UUID, Order> ordersHash = new HashMap<>();
    // map to store orders by price and side
    private final Map<OrderSide, Map<BigDecimal, PriorityQueue<Order>>> ordersQueue = new HashMap<>();
    // map to store opposite side of each order side
    private final Map<OrderSide, OrderSide> oppSide = new HashMap<>();


    /**
     * Constructor for LimitOrderBook
     * Initializes the buy and sell order queues and sets opposite sides for buy and sell
     */
    protected LimitOrderBook() {
        ordersQueue.put(OrderSide.BUY, new HashMap<>());
        ordersQueue.put(OrderSide.SELL, new HashMap<>());

        oppSide.put(OrderSide.BUY, OrderSide.SELL);
        oppSide.put(OrderSide.SELL, OrderSide.BUY);
    }


    /**
     * Returns the Order associated with the specified UUID
     *
     * @param id the UUID of the order to retrieve
     * @return the Order associated with the specified UUID
     */
    protected Order getOrder(UUID id) {
        return ordersHash.get(id);
    }

    /**
     * Adds the specified Order to the appropriate order queue
     *
     * @param order the Order to add
     */
    protected void add(Order order) {
        ordersQueue.get(order.getSide())
                .computeIfAbsent(order.getPrice(), k -> new PriorityQueue<>())
                .add(order);
        ordersHash.put(order.getId(), order);
    }

    /**
     * Removes the specified Order from the appropriate order queue
     *
     * @param order the Order to remove
     */
    protected void delete(Order order) {
        ordersQueue.get(order.getSide()).get(order.getPrice()).remove(order);
        ordersHash.remove(order.getId());
    }

    /**
     * Modifies the specified Order with the new quantity and returns the modified Order
     *
     * @param order       the Order to modify
     * @param newQuantity the new quantity for the Order
     * @return the modified Order
     */
    protected Order modify(Order order, int newQuantity) {
        delete(order);
        Order modifiedOrder = order.copy(newQuantity, LocalDateTime.now());
        add(modifiedOrder);
        return modifiedOrder;
    }

    /**
     * Tries to execute the specified quantity of the specified OrderSide at the specified price
     *
     * @param price    the price at which to execute the order
     * @param quantity the quantity of the order to execute
     * @param side     the OrderSide of the order to execute
     * @return a MatchResult indicating whether the order was fully executed, partially executed, or not executed
     */
    protected MatchResult tryExecute(BigDecimal price, int quantity, OrderSide side) {
        PriorityQueue<Order> pq = ordersQueue.get(oppSide.get(side)).get(price);
        if (pq == null || pq.isEmpty()) {
            // no matching order found, adding new trade to the order book
            add(new Order(price, quantity, side));
            return MatchResult.NONE;
        }

        int unfulfilled = quantity;
        Iterator<Order> it = pq.iterator();
        while (unfulfilled > 0 && it.hasNext()) {
            Order co = it.next();
            if (unfulfilled >= co.getQuantity()) {
                // current iterated order is entirely fulfilled and can be removed from lob
                it.remove();
                ordersHash.remove(co.getId());
                unfulfilled = unfulfilled - co.getQuantity();
            } else {
                co.setQuantity(co.getQuantity() - unfulfilled);
                unfulfilled = 0;
            }
        }

        if (unfulfilled > 0) {
            // add unfulfilled quantity as a new order to lob
            add(new Order(price, unfulfilled, side));
            return MatchResult.PARTIAL;
        }
        return MatchResult.FULL;
    }

    /**
     * Prints the current state of the order book.
     */
    protected void printState() {
        System.out.println("[BUY]");
        System.out.println(buildOutput(ordersQueue.get(OrderSide.BUY)));

        System.out.println("[SELL]");
        System.out.println(buildOutput(ordersQueue.get(OrderSide.SELL)));

        System.out.println("////////////////////////////////////////////////////////");
        System.out.println();
    }

    /**
     * Builds a string representation of the order book for a specific order side.
     *
     * @param qs the map of orders to be printed
     * @return a string representation of the order book for the given order side
     */
    private String buildOutput(Map<BigDecimal, PriorityQueue<Order>> qs) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<BigDecimal, PriorityQueue<Order>> es : qs.entrySet()) {
            String orders = es.getValue().stream().map(Order::toString).collect(joining(" << "));
            sb.append(fp(es.getKey())).append("| ").append(orders).append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     * Formats a BigDecimal price to a string with two decimal places.
     *
     * @param price the BigDecimal price to format
     * @return a string representation of the formatted BigDecimal price
     */
    private String fp(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP).toString();
    }
}
