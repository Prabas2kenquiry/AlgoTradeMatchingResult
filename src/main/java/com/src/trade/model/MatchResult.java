package com.src.trade.model;

/**
 * Enum representing the result of a match attempt in a limit order book.
 * 
 * <ul>
 * <li>{@code FULL} - indicates that the match was fully executed.</li>
 * <li>{@code PARTIAL} - indicates that the match was partially executed.</li>
 * <li>{@code NONE} - indicates that there were no matching orders.</li>
 * <li>{@code ERROR} - indicates an error occurred while attempting to match orders.</li>
 * </ul>
 */
public enum MatchResult {
    FULL, PARTIAL, NONE, ERROR
}
