package com.telekom.cot.device.agent.service.channel;

import java.util.List;

public interface Channel<T> { 
    /**
     * Adds an array of items to a channel
     * @param items
     */
	public void add(T[] items);
	
	/**
	 * Adds a list of items to a channel
	 * @param items
	 */
	public void add(List<T> items);
	/**
	 * Adds an item to a channel
	 * @param item
	 */
	public void add(T item);
}