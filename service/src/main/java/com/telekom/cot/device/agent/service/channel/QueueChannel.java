package com.telekom.cot.device.agent.service.channel;

public interface QueueChannel<T> extends Channel<T> {
    /**
     * retrieves an item from the channel
     * @return
     */
	public T getItem();
}