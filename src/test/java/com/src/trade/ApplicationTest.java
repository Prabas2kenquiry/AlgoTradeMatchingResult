package com.src.trade;

import com.src.trade.model.MatchResult;
import com.src.trade.model.Order;
import com.src.trade.model.OrderSide;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The ApplicationTest class contains unit tests for the {@link Application} class.
 */
class ApplicationTest {

    private Application cut;

    /**
     * Sets up the test fixture by creating a new {@link Application} instance with a new {@link LimitOrderBook}.
     */
    @BeforeEach
    void setUp() {
        cut = new Application(new LimitOrderBook());
    }

    /**
     * Tests that invalid orders are not allowed to be created and that an {@link IllegalArgumentException} is thrown.
     */
    @Test
    void disallowInvalidOrderCreation() {
        assertThrows(IllegalArgumentException.class, () -> new Order(null, 5, OrderSide.BUY));
        assertThrows(IllegalArgumentException.class, () -> new Order(BigDecimal.TEN, -5, OrderSide.SELL));
        assertThrows(IllegalArgumentException.class, () -> new Order(BigDecimal.TEN, 105, null));
        assertThrows(IllegalArgumentException.class, () ->
                new Order(BigDecimal.TEN, 105, OrderSide.SELL).copy(110, null));
    }

    /**
     * Tests that an order created with {@link Application#addOrder(BigDecimal, int, OrderSide)} has its ID and placed values set.
     */
    @Test
    void createdOrderHasIdAndPlacedValuesSet() {
        Order order = cut.addOrder(BigDecimal.TEN, 20, OrderSide.SELL)
                .orElseThrow(() -> new NullPointerException("Error adding order"));
        assertAll(() -> assertNotNull(order.getId()), () -> assertNotNull(order.getPlaced()));
    }

    /**
     * Tests that an order can be deleted with {@link Application#deleteOrder(UUID)} and that its ID cannot be deleted again.
     */
    @Test
    void deleteOrder() {
        Order order = cut.addOrder(BigDecimal.TEN, 20, OrderSide.SELL)
                .orElseThrow(() -> new NullPointerException("Error adding order"));
        assertAll(() -> assertTrue(cut.deleteOrder(order.getId()).isPresent()),
                () -> assertTrue(cut.deleteOrder(order.getId()).isEmpty()));
    }

    /**
     * Tests that an order's placed value can be updated with {@link Application#modifyOrder(UUID, int)}.
     * @throws InterruptedException if the current thread is interrupted while sleeping
     */
    @Test
    void modifyOrderShouldUpdatePlacedValue() throws InterruptedException {
        Order order = cut.addOrder(BigDecimal.TEN, 20, OrderSide.SELL)
                .orElseThrow(() -> new NullPointerException("Error adding order"));
        sleep(1L);
        Optional<Order> modifiedOrder = cut.modifyOrder(order.getId(), 30);
        assertAll(() -> assertTrue(modifiedOrder.isPresent()),
                () -> assertEquals(order.getId(), modifiedOrder.get().getId()),
                () -> assertEquals(30, modifiedOrder.get().getQuantity()),
                () -> assertTrue(ChronoUnit.NANOS.between(order.getPlaced(), modifiedOrder.get().getPlaced()) != 0L));
    }

    /**
     * Tests that a buy order can be executed with {@link Application#executeTrade(BigDecimal, int, OrderSide)} and that multiple sell orders are reduced.
     */
    @Test
    void executesBuyOrderReducingMultipleSellOrders() {
        cut.addOrder(BigDecimal.TEN, 20, OrderSide.SELL)
                .orElseThrow(() -> new NullPointerException("Error adding order"));
        cut.addOrder(BigDecimal.TEN, 5, OrderSide.SELL)
                .orElseThrow(() -> new NullPointerException("Error adding order"));
        cut.addOrder(BigDecimal.TEN, 15, OrderSide.SELL)
                .orElseThrow(() -> new NullPointerException("Error adding order"));
        MatchResult result = cut.executeTrade(BigDecimal.TEN, 40, OrderSide.BUY);
        assertAll(() -> assertEquals(MatchResult.FULL, result));
    }

    /**
	 * Test method for {@link Application#executeTrade(BigDecimal, int, OrderSide)}.
	 * Test the scenario where the execution of a SELL order reduces the order quantity to zero.
	 * Add a BUY order to the order book, then execute a SELL order that matches the BUY order entirely.
	 * Assert that the resulting match result is FULL.
	 */
    @Test
    void executesSellOrderReducingSingleSellEntirely() {
        cut.addOrder(BigDecimal.TEN, 20, OrderSide.BUY)
                .orElseThrow(() -> new NullPointerException("Error adding order"));
        MatchResult result = cut.executeTrade(BigDecimal.TEN, 20, OrderSide.SELL);
        assertAll(() -> assertEquals(MatchResult.FULL, result));
    }

    /**
     * Test method for {@link Application#executeTrade(BigDecimal, int, OrderSide)}.
     * Test the scenario where a BUY order is added to the order book after a SELL order is executed and there are no matching orders.
     * Execute a BUY order with a price of 10 and a quantity of 20, then execute a SELL order with a price of 10 and a quantity of 20.
     * Assert that the match result for the BUY order is NONE and the match result for the SELL order is FULL.
     */
    @Test
    void addsBuyOrderToLobAfterNotFindingMatchingSell() {
        MatchResult r1 = cut.executeTrade(BigDecimal.TEN, 20, OrderSide.BUY);
        assertAll(() -> assertEquals(MatchResult.NONE, r1));
        MatchResult r2 = cut.executeTrade(BigDecimal.TEN, 20, OrderSide.SELL);
        assertAll(() -> assertEquals(MatchResult.FULL, r2));
    }

    /**
	 * Tests that a sell order is executed partially when it matches with an existing buy order in the LOB but
	 * there is not enough quantity in the buy order to fulfill the entire sell order.
	 */
    @Test
    void executesSellOrderPartially() {
        cut.addOrder(BigDecimal.TEN, 20, OrderSide.BUY)
                .orElseThrow(() -> new NullPointerException("Error adding order"));
        MatchResult r1 = cut.executeTrade(BigDecimal.TEN, 30, OrderSide.SELL);
        assertAll(() -> assertEquals(MatchResult.PARTIAL, r1));
    }
}