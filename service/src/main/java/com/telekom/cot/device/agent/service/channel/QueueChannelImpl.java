package com.telekom.cot.device.agent.service.channel;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QueueChannelImpl<T> implements QueueChannel<T> {

    private ConcurrentLinkedQueue<T> myQueue = new ConcurrentLinkedQueue<T>();
    
    @Override
    public void add(T[] items) {
        for (int i = 0; i < items.length; i++) {
            myQueue.offer(items[i]);
        }
    }
    
	@Override
	public void add(List<T> items) {
		for (T item : items) {
			myQueue.offer(item);
		}
	}

    @Override
    public void add(T item) {
        myQueue.offer(item);
    }

    @Override
    public T getItem() {
        return myQueue.poll();
    }
}
