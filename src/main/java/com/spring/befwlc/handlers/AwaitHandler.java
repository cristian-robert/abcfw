package com.spring.befwlc.handlers;

import com.spring.befwlc.configuration.AwaitConfiguration;
import com.spring.befwlc.exceptions.MaximumIterationExceededException;
import com.spring.befwlc.exceptions.TestExecutionException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AwaitHandler {

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void awaitTrue(final ConditionMatcher conditionMatcher, final AwaitConfiguration awaitConfiguration) throws TestExecutionException {
        int iterations = 0;
        lock.lock();
        try {
            while (!conditionMatcher.isMatch()) {
                if (iterations >= awaitConfiguration.getIterations()) {
                    throw new MaximumIterationExceededException("Condition not met after maximum iterations");
                }
                condition.await(awaitConfiguration.getInterval(), TimeUnit.SECONDS);
                iterations++;
            }
        } catch (final InterruptedException e) {
            throw new TestExecutionException("Await handler process failed:\n", e.getMessage());
        } finally {
            lock.unlock();
        }
    }
}
