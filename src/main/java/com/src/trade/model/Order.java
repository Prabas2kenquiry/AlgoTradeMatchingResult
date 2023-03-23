package com.src.trade.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * The Order class represents a financial order, which has a unique identifier, price, quantity,
 * order side (buy or sell), and placement time.
 */
public class Order implements Comparable<Order> {

	 /**
     * The unique identifier of the order.
     */
	
    private final UUID id;
    /**
     * The price of the order.
     */
    
    private final BigDecimal price;
    /**
     * The quantity of the order.
     */
    
    private int quantity;
    /**
     * The order side (buy or sell) of the order.
     */
    
    private final OrderSide side;
    /**
     * The time at which the order was placed.
     */
    
    private final LocalDateTime placed;


    public Order(BigDecimal price, int quantity, OrderSide side) {
        this(UUID.randomUUID(), price, quantity, side, LocalDateTime.now());
    }

    /**
     * Creates a new order with the specified price, quantity, and order side.
     *
     * @param price    the price of the order.
     * @param quantity the quantity of the order.
     * @param side     the order side (buy or sell) of the order.
     * @throws IllegalArgumentException if the price is null or not positive, or if the quantity
     *                                  is not positive, or if the order side is null.
     */
    private Order(UUID id, BigDecimal price, int quantity, OrderSide side, LocalDateTime placed) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be non-null and have a positive value");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive value");
        }
        if (side == null) {
            throw new IllegalArgumentException("Order side must be non-null");
        }
        if (placed == null || placed.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid order placed value");
        }
        this.id = id;
        this.price = price;
        this.quantity = quantity;
        this.side = side;
        this.placed = placed;
    }

    /**
     * Returns the unique identifier of the order.
     *
     * @return the unique identifier of the order.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the price of the order.
     *
     * @return the price of the order.
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Returns the quantity of the order.
     *
     * @return the quantity of the order.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity of the order.
     *
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Returns the order side (buy or sell) of the order.
     *
     * @return the order side (buy or sell) of the order.
     */
    public OrderSide getSide() {
        return side;
    }

    /**
     * Returns the time at which the order was placed.
     *
     * @return the time at which the order was placed.
     */
    public LocalDateTime getPlaced() {
        return placed;
    }

    public Order copy(int newQuantity, LocalDateTime at) {
        return new Order(this.getId(), this.getPrice(), newQuantity, this.getSide(), at);
    }

    /**
     * Indicates whether some other object is "equal to" this one based on their IDs.
     *
     * @param o the reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        return getId().equals(order.getId());
    }

    /**
     * Returns a hash code value for the object based on its ID.
     *
     * @return a hash code value for this object based on its ID.
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }


    /**
     * Returns a string representation of the Order object, including its ID, price, quantity, side, and the time
     * it was placed.
     *
     * @return a string representation of the Order object, including its ID, price, quantity, side, and the time
     * it was placed.
     */
    @Override
    public String toString() {
        return "Order(" +
                "id=" + shorten(id) +
                ", price=" + price +
                ", quantity=" + quantity +
                ", side=" + side +
                ", placed=" + placed.format(DateTimeFormatter.ofPattern("HH:mm:ss.SS")) +
                ')';
    }

    /**
     * Shortens the given UUID to a format of the first 2 characters and last 2 characters separated by ".."
     *
     * @param id the UUID to shorten
     * @return a shortened version of the given UUID
     */
    private String shorten(UUID id) {
        String strId = id.toString();
        return String.format("%s..%s", strId.substring(0, 2), strId.substring(strId.length() - 2));
    }

    /**
     * Compares this Order object with another Order object based on the time they were placed.
     *
     * @param o the Order object to compare to
     * @return a negative integer, zero, or a positive integer as this Order object is less than, equal to,
     * or greater than the specified Order object based on the time they were placed.
     */
    @Override
    public int compareTo(Order o) {
        return this.placed.compareTo(o.getPlaced());
    }

}