package com.telekom.cot.device.agent.platform.mqtt;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;

/**
 * The {@code PublishFuture} represents the generic result of an asynchronous computation. 
 * Methods are provided to check if the computation is complete, to wait for its completion,
 * and to retrieve the generic result of the computation.
 * The {@code PublishFuture} acts as a wrapper and map the exception to {@code AbstractAgentException}.
 * @param <V>
 *            the result type
 */
public class PublishFuture<V> {

    /** The wrapped future. */
    private Future<V> future;

    public PublishFuture(Future<V> future) {
        this.future = future;
    }

    /**
     * Attempts to cancel execution of this task. 
     * 
     * @param mayInterruptIfRunning
     * @return
     */
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    /**
     * Returns {@code true} if this task was cancelled before it completed
     * normally.
     */
    public boolean isCancelled() {
        return future.isCancelled();
    }

    /**
     * Returns {@code true} if this task completed.
     */
    public boolean isDone() {
        return future.isDone();
    }

    /**
     * Waits if necessary for the computation to complete, and then
     * retrieves its result.
     */
    public V get() throws AbstractAgentException {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new PlatformServiceException("can't get PublishedValues", e);
        }
    }

    /**
     * Waits if necessary for the computation to complete, and then
     * retrieves its result.
     */
    public V get(long timeout, TimeUnit unit) throws AbstractAgentException {
        try {
            return future.get(timeout, unit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new PlatformServiceException("can't get PublishedValues", e);
        }
    }
}
