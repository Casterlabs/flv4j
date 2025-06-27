package co.casterlabs.flv4j.rtmp.net.rpc;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

public class RPCPromise<T> {
    private final ReentrantLock lock = new ReentrantLock();

    private final List<Consumer<T>> thenCbs = new LinkedList<>();
    private final List<Consumer<? super Throwable>> catchCbs = new LinkedList<>();
    private final List<Runnable> finallyCbs = new LinkedList<>();

    private volatile boolean complete = false;
    private volatile Throwable failureReason = null;
    private volatile T result = null;

    public RPCPromise(Init<T> init) {
        final Consumer<T> resolve = (result) -> {
            this.lock.lock();
            try {
                if (this.complete) return;

                this.result = result;
                this.complete = true;
                this.thenCbs.forEach((c) -> c.accept(result));
                this.finallyCbs.forEach(Runnable::run);
            } finally {
                this.lock.unlock();
            }
        };
        final Consumer<Throwable> reject = (t) -> {
            this.lock.lock();
            try {
                if (this.complete) return;

                this.failureReason = t;
                this.complete = true;
                this.catchCbs.forEach((r) -> r.accept(t));
                this.finallyCbs.forEach(Runnable::run);
            } finally {
                this.lock.unlock();
            }
        };
        init.init(new Handle<>(resolve, reject));
    }

    /* ------------------------ */

    public <R> RPCPromise<R> then(Then<T, R> then) {
        return new RPCPromise<>((handle) -> {
            this.lock.lock();
            try {
                if (this.complete) {
                    if (this.failureReason == null) {
                        try {
                            R intermediate = then.then(this.result);
                            handle.resolve(intermediate);
                        } catch (InterruptedException | IOException | CallError e) {
                            handle.reject(e);
                        }
                    } else {
                        handle.reject(this.failureReason);
                    }
                } else {
                    this.thenCbs.add((c) -> {
                        try {
                            R intermediate = then.then(this.result);
                            handle.resolve(intermediate);
                        } catch (InterruptedException | IOException | CallError e) {
                            handle.reject(e);
                        }
                    });
                    this.catchCbs.add(handle::reject);
                }
            } finally {
                this.lock.unlock();
            }
        });
    }

    public RPCPromise<Void> then(VoidThen<T> then) {
        return this.then((r) -> {
            then.then(r);
            return null;
        });
    }

    /**
     * @apiNote usually either {@link InterruptedException} {@link IOException} or
     *          {@link CallError}
     */
    public RPCPromise<T> catch_(Consumer<? super Throwable> ctch) {
        this.lock.lock();
        try {
            if (this.complete) {
                if (this.failureReason != null) {
                    ctch.accept(this.failureReason);
                }
            } else {
                this.catchCbs.add(ctch);
            }
        } finally {
            this.lock.unlock();
        }
        return this;
    }

    public RPCPromise<T> finally_(Runnable fnlly) {
        this.lock.lock();
        try {
            if (this.complete) {
                fnlly.run();
            } else {
                this.finallyCbs.add(fnlly);
            }
        } finally {
            this.lock.unlock();
        }
        return this;
    }

    @SneakyThrows
    public T await() throws InterruptedException, IOException, CallError {
        CompletableFuture<T> future = new CompletableFuture<>();

        this.finally_(() -> {
            if (this.failureReason != null) {
                future.completeExceptionally(this.failureReason);
            } else {
                future.complete(this.result);
            }
        });

        try {
            return future.get();
        } catch (ExecutionException e) {
            throw e.getCause(); // sneakythrows lets us wrap this :^)
        }
    }

    /* ------------------------ */

    @FunctionalInterface
    public static interface Then<T, R> {
        public R then(T result) throws InterruptedException, IOException, CallError;
    }

    @FunctionalInterface
    public static interface VoidThen<T> {
        public void then(T result) throws InterruptedException, IOException, CallError;
    }

    @FunctionalInterface
    public static interface Init<T> {
        public void init(Handle<T> handle);
    }

    @AllArgsConstructor
    public static class Handle<T> {
        private final Consumer<T> resolve;
        private final Consumer<Throwable> reject;

        public void resolve(T result) {
            this.resolve.accept(result);
        }

        public void reject(Throwable t) {
            this.reject.accept(t);
        }

    }

}
