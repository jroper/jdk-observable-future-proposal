package java.util.concurrent;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A {@link Future} that may include dependent functions and actions
 * that trigger upon its completion.
 *
 * <p>Methods are available for adding dependents based on
 * user-provided Functions, Consumers, or Runnables. The appropriate
 * form to use depends on whether actions require arguments and/or
 * produce results.  Completion of a dependent action will trigger the
 * completion of another ObservableFuture.  Actions may also be
 * triggered after either or both the current and another
 * ObservableFuture complete.  Multiple ObservableFutures may also
 * be grouped as one using {@link #anyOf(ObservableFuture...)} and
 * {@link #allOf(ObservableFuture...)}.
 *
 * <p>ObservableFutures themselves do not execute asynchronously.
 * However, actions supplied for dependent completions of another
 * ObservableFuture may do so, depending on whether they are provided
 * via one of the <em>async</em> methods (that is, methods with names
 * of the form <tt><var>xxx</var>Async</tt>).  The <em>async</em>
 * methods provide a way to commence asynchronous processing of an
 * action using either a given {@link Executor} or by default the
 * {@link ForkJoinPool#commonPool()}. To simplify monitoring,
 * debugging, and tracking, all generated asynchronous tasks are
 * instances of the marker interface {@link AsynchronousCompletionTask}.
 *
 * <p>Actions supplied for dependent completions of <em>non-async</em>
 * methods may be performed by the thread that completes the current
 * ObservableFuture, or by any other caller of these methods.  There
 * are no guarantees about the order of processing completions unless
 * constrained by these methods.
 *
 * <p>Since (unlike {@link FutureTask}) this class has no direct
 * control over the computation that causes it to be completed,
 * cancellation is treated as just another form of exceptional completion.
 * Method {@link #cancel cancel} has the same effect as
 * {@code completeExceptionally(new CancellationException())}.
 *
 * <p>Upon exceptional completion (including cancellation), or when a
 * completion entails an additional computation which terminates
 * abruptly with an (unchecked) exception or error, then all of their
 * dependent completions (and their dependents in turn) generally act
 * as {@code completeExceptionally} with a {@link CompletionException}
 * holding that exception as its cause.  However, the {@link
 * #exceptionally exceptionally} and {@link #handle handle}
 * completions <em>are</em> able to handle exceptional completions of
 * the ObservableFutures they depend on.
 *
 * <p>In case of exceptional completion with a CompletionException,
 * methods {@link #get()} and {@link #get(long, TimeUnit)} throw an
 * {@link ExecutionException} with the same cause as held in the
 * corresponding CompletionException.  However, in these cases,
 * methods {@link #join()} and {@link #getNow} throw the
 * CompletionException, which simplifies usage.
 *
 * <p>Arguments used to pass a completion result (that is, for parameters
 * of type {@code T}) may be null, but passing a null value for any other
 * parameter will result in a {@link NullPointerException} being thrown.
 *
 * @author Doug Lea
 * @since 1.8
 */
public interface ObservableFuture<T> extends Future<T> {
    /**
     * Returns the result value when complete, or throws an
     * (unchecked) exception if completed exceptionally. To better
     * conform with the use of common functional forms, if a
     * computation involved in the completion of this
     * ObservableFuture threw an exception, this method throws an
     * (unchecked) {@link java.util.concurrent.CompletionException} with the underlying
     * exception as its cause.
     *
     * @return the result value
     * @throws java.util.concurrent.CancellationException if the computation was cancelled
     * @throws java.util.concurrent.CompletionException if this future completed
     * exceptionally or a completion computation threw an exception
     */
    T join();

    /**
     * Returns the result value (or throws any encountered exception)
     * if completed, else returns the given valueIfAbsent.
     *
     * @param valueIfAbsent the value to return if not completed
     * @return the result value, if completed, else the given valueIfAbsent
     * @throws java.util.concurrent.CancellationException if the computation was cancelled
     * @throws java.util.concurrent.CompletionException if this future completed
     * exceptionally or a completion computation threw an exception
     */
    T getNow(T valueIfAbsent);

    /**
     * Returns a new ObservableFuture that is completed
     * when this ObservableFuture completes, with the result of the
     * given function of this ObservableFuture's result.
     *
     * <p>If this ObservableFuture completes exceptionally, or the
     * supplied function throws an exception, then the returned
     * ObservableFuture completes exceptionally with a
     * CompletionException holding the exception as its cause.
     *
     * @param fn the function to use to compute the value of
     * the returned ObservableFuture
     * @return the new ObservableFuture
     */
    <U> ObservableFuture<U> thenApply(Function<? super T, ? extends U> fn);

    /**
     * Returns a new ObservableFuture that is asynchronously completed
     * when this ObservableFuture completes, with the result of the
     * given function of this ObservableFuture's result from a
     * task running in the {@link java.util.concurrent.ForkJoinPool#commonPool()}.
     *
     * <p>If this ObservableFuture completes exceptionally, or the
     * supplied function throws an exception, then the returned
     * ObservableFuture completes exceptionally with a
     * CompletionException holding the exception as its cause.
     *
     * @param fn the function to use to compute the value of
     * the returned ObservableFuture
     * @return the new ObservableFuture
     */
    <U> ObservableFuture<U> thenApplyAsync
    (Function<? super T, ? extends U> fn);

    /**
     * Returns a new ObservableFuture that is asynchronously completed
     * when this ObservableFuture completes, with the result of the
     * given function of this ObservableFuture's result from a
     * task running in the given executor.
     *
     * <p>If this ObservableFuture completes exceptionally, or the
     * supplied function throws an exception, then the returned
     * ObservableFuture completes exceptionally with a
     * CompletionException holding the exception as its cause.
     *
     * @param fn the function to use to compute the value of
     * the returned ObservableFuture
     * @param executor the executor to use for asynchronous execution
     * @return the new ObservableFuture
     */
    <U> ObservableFuture<U> thenApplyAsync
    (Function<? super T, ? extends U> fn,
     Executor executor);

    /**
     * Returns a new ObservableFuture that is completed
     * when this ObservableFuture completes, after performing the given
     * action with this ObservableFuture's result.
     *
     * <p>If this ObservableFuture completes exceptionally, or the
     * supplied action throws an exception, then the returned
     * ObservableFuture completes exceptionally with a
     * CompletionException holding the exception as its cause.
     *
     * @param block the action to perform before completing the
     * returned ObservableFuture
     * @return the new ObservableFuture
     */
    ObservableFuture<Void> thenAccept(Consumer<? super T> block);

    /**
     * Returns a new ObservableFuture that is asynchronously completed
     * when this ObservableFuture completes, after performing the given
     * action with this ObservableFuture's result from a task running
     * in the {@link java.util.concurrent.ForkJoinPool#commonPool()}.
     *
     * <p>If this ObservableFuture completes exceptionally, or the
     * supplied action throws an exception, then the returned
     * ObservableFuture completes exceptionally with a
     * CompletionException holding the exception as its cause.
     *
     * @param block the action to perform before completing the
     * returned ObservableFuture
     * @return the new ObservableFuture
     */
    ObservableFuture<Void> thenAcceptAsync(Consumer<? super T> block);

    /**
     * Returns a new ObservableFuture that is asynchronously completed
     * when this ObservableFuture completes, after performing the given
     * action with this ObservableFuture's result from a task running
     * in the given executor.
     *
     * <p>If this ObservableFuture completes exceptionally, or the
     * supplied action throws an exception, then the returned
     * ObservableFuture completes exceptionally with a
     * CompletionException holding the exception as its cause.
     *
     * @param block the action to perform before completing the
     * returned ObservableFuture
     * @param executor the executor to use for asynchronous execution
     * @return the new ObservableFuture
     */
    ObservableFuture<Void> thenAcceptAsync(Consumer<? super T> block,
                                            Executor executor);

    /**
     * Returns a new ObservableFuture that is completed
     * when this ObservableFuture completes, after performing the given
     * action.
     *
     * <p>If this ObservableFuture completes exceptionally, or the
     * supplied action throws an exception, then the returned
     * ObservableFuture completes exceptionally with a
     * CompletionException holding the exception as its cause.
     *
     * @param action the action to perform before completing the
     * returned ObservableFuture
     * @return the new ObservableFuture
     */
    ObservableFuture<Void> thenRun(Runnable action);

    /**
     * Returns a new ObservableFuture that is asynchronously completed
     * when this ObservableFuture completes, after performing the given
     * action from a task running in the {@link java.util.concurrent.ForkJoinPool#commonPool()}.
     *
     * <p>If this ObservableFuture completes exceptionally, or the
     * supplied action throws an exception, then the returned
     * ObservableFuture completes exceptionally with a
     * CompletionException holding the exception as its cause.
     *
     * @param action the action to perform before completing the
     * returned ObservableFuture
     * @return the new ObservableFuture
     */
    ObservableFuture<Void> thenRunAsync(Runnable action);

    /**
     * Returns a new ObservableFuture that is asynchronously completed
     * when this ObservableFuture completes, after performing the given
     * action from a task running in the given executor.
     *
     * <p>If this ObservableFuture completes exceptionally, or the
     * supplied action throws an exception, then the returned
     * ObservableFuture completes exceptionally with a
     * CompletionException holding the exception as its cause.
     *
     * @param action the action to perform before completing the
     * returned ObservableFuture
     * @param executor the executor to use for asynchronous execution
     * @return the new ObservableFuture
     */
    ObservableFuture<Void> thenRunAsync(Runnable action,
                                         Executor executor);

    /**
     * Returns a new ObservableFuture that is completed
     * when both this and the other given ObservableFuture complete,
     * with the result of the given function of the results of the two
     * ObservableFutures.
     *
     * <p>If this and/or the other ObservableFuture complete
     * exceptionally, or the supplied function throws an exception,
     * then the returned ObservableFuture completes exceptionally
     * with a CompletionException holding the exception as its cause.
     *
     * @param other the other ObservableFuture
     * @param fn the function to use to compute the value of
     * the returned ObservableFuture
     * @return the new ObservableFuture
     */
    <U,V> ObservableFuture<V> thenCombine
    (ObservableFuture<? extends U> other,
     BiFunction<? super T, ? super U, ? extends V> fn);

    /**
     * Returns a new ObservableFuture that is asynchronously completed
     * when both this and the other given ObservableFuture complete,
     * with the result of the given function of the results of the two
     * ObservableFutures from a task running in the
     * {@link java.util.concurrent.ForkJoinPool#commonPool()}.
     *
     * <p>If this and/or the other ObservableFuture complete
     * exceptionally, or the supplied function throws an exception,
     * then the returned ObservableFuture completes exceptionally
     * with a CompletionException holding the exception as its cause.
     *
     * @param other the other ObservableFuture
     * @param fn the function to use to compute the value of
     * the returned ObservableFuture
     * @return the new ObservableFuture
     */
    <U,V> ObservableFuture<V> thenCombineAsync
    (ObservableFuture<? extends U> other,
     BiFunction<? super T, ? super U, ? extends V> fn);

    /**
     * Returns a new ObservableFuture that is asynchronously completed
     * when both this and the other given ObservableFuture complete,
     * with the result of the given function of the results of the two
     * ObservableFutures from a task running in the given executor.
     *
     * <p>If this and/or the other ObservableFuture complete
     * exceptionally, or the supplied function throws an exception,
     * then the returned ObservableFuture completes exceptionally
     * with a CompletionException holding the exception as its cause.
     *
     * @param other the other ObservableFuture
     * @param fn the function to use to compute the value of
     * the returned ObservableFuture
     * @param executor the executor to use for asynchronous execution
     * @return the new ObservableFuture
     */
    <U,V> ObservableFuture<V> thenCombineAsync
    (ObservableFuture<? extends U> other,
     BiFunction<? super T, ? super U, ? extends V> fn,
     Executor executor);

    /**
     * Returns a new ObservableFuture that is completed
     * when both this and the other given ObservableFuture complete,
     * after performing the given action with the results of the two
     * ObservableFutures.
     *
     * <p>If this and/or the other ObservableFuture complete
     * exceptionally, or the supplied action throws an exception,
     * then the returned ObservableFuture completes exceptionally
     * with a CompletionException holding the exception as its cause.
     *
     * @param other the other ObservableFuture
     * @param block the action to perform before completing the
     * returned ObservableFuture
     * @return the new ObservableFuture
     */
    <U> ObservableFuture<Void> thenAcceptBoth
    (ObservableFuture<? extends U> other,
     BiConsumer<? super T, ? super U> block);

    /**
     * Returns a new ObservableFuture that is asynchronously completed
     * when both this and the other given ObservableFuture complete,
     * after performing the given action with the results of the two
     * ObservableFutures from a task running in the {@link
     * java.util.concurrent.ForkJoinPool#commonPool()}.
     *
     * <p>If this and/or the other ObservableFuture complete
     * exceptionally, or the supplied action throws an exception,
     * then the returned ObservableFuture completes exceptionally
     * with a CompletionException holding the exception as its cause.
     *
     * @param other the other ObservableFuture
     * @param block the action to perform before completing the
     * returned ObservableFuture
     * @return the new ObservableFuture
     */
    <U> ObservableFuture<Void> thenAcceptBothAsync
    (ObservableFuture<? extends U> other,
     BiConsumer<? super T, ? super U> block);

    /**
     * Returns a new ObservableFuture that is asynchronously completed
     * when both this and the other given ObservableFuture complete,
     * after performing the given action with the results of the two
     * ObservableFutures from a task running in the given executor.
     *
     * <p>If this and/or the other ObservableFuture complete
     * exceptionally, or the supplied action throws an exception,
     * then the returned ObservableFuture completes exceptionally
     * with a CompletionException holding the exception as its cause.
     *
     * @param other the other ObservableFuture
     * @param block the action to perform before completing the
     * returned ObservableFuture
     * @param executor the executor to use for asynchronous execution
     * @return the new ObservableFuture
     */
    <U> ObservableFuture<Void> thenAcceptBothAsync
    (ObservableFuture<? extends U> other,
     BiConsumer<? super T, ? super U> block,
     Executor executor);

    /**
     * Returns a new ObservableFuture that is completed
     * when both this and the other given ObservableFuture complete,
     * after performing the given action.
     *
     * <p>If this and/or the other ObservableFuture complete
     * exceptionally, or the supplied action throws an exception,
     * then the returned ObservableFuture completes exceptionally
     * with a CompletionException holding the exception as its cause.
     *
     * @param other the other ObservableFuture
     * @param action the action to perform before completing the
     * returned ObservableFuture
     * @return the new ObservableFuture
     */
    ObservableFuture<Void> runAfterBoth(ObservableFuture<?> other,
                                         Runnable action);

    /**
     * Returns a new ObservableFuture that is asynchronously completed
     * when both this and the other given ObservableFuture complete,
     * after performing the given action from a task running in the
     * {@link java.util.concurrent.ForkJoinPool#commonPool()}.
     *
     * <p>If this and/or the other ObservableFuture complete
     * exceptionally, or the supplied action throws an exception,
     * then the returned ObservableFuture completes exceptionally
     * with a CompletionException holding the exception as its cause.
     *
     * @param other the other ObservableFuture
     * @param action the action to perform before completing the
     * returned ObservableFuture
     * @return the new ObservableFuture
     */
    ObservableFuture<Void> runAfterBothAsync(ObservableFuture<?> other,
                                              Runnable action);

    /**
     * Returns a new ObservableFuture that is asynchronously completed
     * when both this and the other given ObservableFuture complete,
     * after performing the given action from a task running in the
     * given executor.
     *
     * <p>If this and/or the other ObservableFuture complete
     * exceptionally, or the supplied action throws an exception,
     * then the returned ObservableFuture completes exceptionally
     * with a CompletionException holding the exception as its cause.
     *
     * @param other the other ObservableFuture
     * @param action the action to perform before completing the
     * returned ObservableFuture
     * @param executor the executor to use for asynchronous execution
     * @return the new ObservableFuture
     */
    ObservableFuture<Void> runAfterBothAsync(ObservableFuture<?> other,
                                              Runnable action,
                                              Executor executor);

    /**
     * Returns a new ObservableFuture that is completed
     * when either this or the other given ObservableFuture completes,
     * with the result of the given function of either this or the other
     * ObservableFuture's result.
     *
     * <p>If this and/or the other ObservableFuture complete
     * exceptionally, then the returned ObservableFuture may also do so,
     * with a CompletionException holding one of these exceptions as its
     * cause.  No guarantees are made about which result or exception is
     * used in the returned ObservableFuture.  If the supplied function
     * throws an exception, then the returned ObservableFuture completes
     * exceptionally with a CompletionException holding the exception as
     * its cause.
     *
     * @param other the other ObservableFuture
     * @param fn the function to use to compute the value of
     * the returned ObservableFuture
     * @return the new ObservableFuture
     */
    <U> ObservableFuture<U> applyToEither
    (ObservableFuture<? extends T> other,
     Function<? super T, U> fn);

    /**
     * Returns a new ObservableFuture that is asynchronously completed
     * when either this or the other given ObservableFuture completes,
     * with the result of the given function of either this or the other
     * ObservableFuture's result from a task running in the
     * {@link java.util.concurrent.ForkJoinPool#commonPool()}.
     *
     * <p>If this and/or the other ObservableFuture complete
     * exceptionally, then the returned ObservableFuture may also do so,
     * with a CompletionException holding one of these exceptions as its
     * cause.  No guarantees are made about which result or exception is
     * used in the returned ObservableFuture.  If the supplied function
     * throws an exception, then the returned ObservableFuture completes
     * exceptionally with a CompletionException holding the exception as
     * its cause.
     *
     * @param other the other ObservableFuture
     * @param fn the function to use to compute the value of
     * the returned ObservableFuture
     * @return the new ObservableFuture
     */
    <U> ObservableFuture<U> applyToEitherAsync
    (ObservableFuture<? extends T> other,
     Function<? super T, U> fn);

    /**
     * Returns a new ObservableFuture that is asynchronously completed
     * when either this or the other given ObservableFuture completes,
     * with the result of the given function of either this or the other
     * ObservableFuture's result from a task running in the
     * given executor.
     *
     * <p>If this and/or the other ObservableFuture complete
     * exceptionally, then the returned ObservableFuture may also do so,
     * with a CompletionException holding one of these exceptions as its
     * cause.  No guarantees are made about which result or exception is
     * used in the returned ObservableFuture.  If the supplied function
     * throws an exception, then the returned ObservableFuture completes
     * exceptionally with a CompletionException holding the exception as
     * its cause.
     *
     * @param other the other ObservableFuture
     * @param fn the function to use to compute the value of
     * the returned ObservableFuture
     * @param executor the executor to use for asynchronous execution
     * @return the new ObservableFuture
     */
    <U> ObservableFuture<U> applyToEitherAsync
    (ObservableFuture<? extends T> other,
     Function<? super T, U> fn,
     Executor executor);

    /**
     * Returns a new ObservableFuture that is completed
     * when either this or the other given ObservableFuture completes,
     * after performing the given action with the result of either this
     * or the other ObservableFuture's result.
     *
     * <p>If this and/or the other ObservableFuture complete
     * exceptionally, then the returned ObservableFuture may also do so,
     * with a CompletionException holding one of these exceptions as its
     * cause.  No guarantees are made about which result or exception is
     * used in the returned ObservableFuture.  If the supplied action
     * throws an exception, then the returned ObservableFuture completes
     * exceptionally with a CompletionException holding the exception as
     * its cause.
     *
     * @param other the other ObservableFuture
     * @param block the action to perform before completing the
     * returned ObservableFuture
     * @return the new ObservableFuture
     */
    ObservableFuture<Void> acceptEither
    (ObservableFuture<? extends T> other,
     Consumer<? super T> block);

    /**
     * Returns a new ObservableFuture that is asynchronously completed
     * when either this or the other given ObservableFuture completes,
     * after performing the given action with the result of either this
     * or the other ObservableFuture's result from a task running in
     * the {@link java.util.concurrent.ForkJoinPool#commonPool()}.
     *
     * <p>If this and/or the other ObservableFuture complete
     * exceptionally, then the returned ObservableFuture may also do so,
     * with a CompletionException holding one of these exceptions as its
     * cause.  No guarantees are made about which result or exception is
     * used in the returned ObservableFuture.  If the supplied action
     * throws an exception, then the returned ObservableFuture completes
     * exceptionally with a CompletionException holding the exception as
     * its cause.
     *
     * @param other the other ObservableFuture
     * @param block the action to perform before completing the
     * returned ObservableFuture
     * @return the new ObservableFuture
     */
    ObservableFuture<Void> acceptEitherAsync
    (ObservableFuture<? extends T> other,
     Consumer<? super T> block);

    /**
     * Returns a new ObservableFuture that is asynchronously completed
     * when either this or the other given ObservableFuture completes,
     * after performing the given action with the result of either this
     * or the other ObservableFuture's result from a task running in
     * the given executor.
     *
     * <p>If this and/or the other ObservableFuture complete
     * exceptionally, then the returned ObservableFuture may also do so,
     * with a CompletionException holding one of these exceptions as its
     * cause.  No guarantees are made about which result or exception is
     * used in the returned ObservableFuture.  If the supplied action
     * throws an exception, then the returned ObservableFuture completes
     * exceptionally with a CompletionException holding the exception as
     * its cause.
     *
     * @param other the other ObservableFuture
     * @param block the action to perform before completing the
     * returned ObservableFuture
     * @param executor the executor to use for asynchronous execution
     * @return the new ObservableFuture
     */
    ObservableFuture<Void> acceptEitherAsync
    (ObservableFuture<? extends T> other,
     Consumer<? super T> block,
     Executor executor);

    /**
     * Returns a new ObservableFuture that is completed
     * when either this or the other given ObservableFuture completes,
     * after performing the given action.
     *
     * <p>If this and/or the other ObservableFuture complete
     * exceptionally, then the returned ObservableFuture may also do so,
     * with a CompletionException holding one of these exceptions as its
     * cause.  No guarantees are made about which result or exception is
     * used in the returned ObservableFuture.  If the supplied action
     * throws an exception, then the returned ObservableFuture completes
     * exceptionally with a CompletionException holding the exception as
     * its cause.
     *
     * @param other the other ObservableFuture
     * @param action the action to perform before completing the
     * returned ObservableFuture
     * @return the new ObservableFuture
     */
    ObservableFuture<Void> runAfterEither(ObservableFuture<?> other,
                                           Runnable action);

    /**
     * Returns a new ObservableFuture that is asynchronously completed
     * when either this or the other given ObservableFuture completes,
     * after performing the given action from a task running in the
     * {@link java.util.concurrent.ForkJoinPool#commonPool()}.
     *
     * <p>If this and/or the other ObservableFuture complete
     * exceptionally, then the returned ObservableFuture may also do so,
     * with a CompletionException holding one of these exceptions as its
     * cause.  No guarantees are made about which result or exception is
     * used in the returned ObservableFuture.  If the supplied action
     * throws an exception, then the returned ObservableFuture completes
     * exceptionally with a CompletionException holding the exception as
     * its cause.
     *
     * @param other the other ObservableFuture
     * @param action the action to perform before completing the
     * returned ObservableFuture
     * @return the new ObservableFuture
     */
    ObservableFuture<Void> runAfterEitherAsync
    (ObservableFuture<?> other,
     Runnable action);

    /**
     * Returns a new ObservableFuture that is asynchronously completed
     * when either this or the other given ObservableFuture completes,
     * after performing the given action from a task running in the
     * given executor.
     *
     * <p>If this and/or the other ObservableFuture complete
     * exceptionally, then the returned ObservableFuture may also do so,
     * with a CompletionException holding one of these exceptions as its
     * cause.  No guarantees are made about which result or exception is
     * used in the returned ObservableFuture.  If the supplied action
     * throws an exception, then the returned ObservableFuture completes
     * exceptionally with a CompletionException holding the exception as
     * its cause.
     *
     * @param other the other ObservableFuture
     * @param action the action to perform before completing the
     * returned ObservableFuture
     * @param executor the executor to use for asynchronous execution
     * @return the new ObservableFuture
     */
    ObservableFuture<Void> runAfterEitherAsync
    (ObservableFuture<?> other,
     Runnable action,
     Executor executor);

    /**
     * Returns a ObservableFuture that upon completion, has the same
     * value as produced by the given function of the result of this
     * ObservableFuture.
     *
     * <p>If this ObservableFuture completes exceptionally, then the
     * returned ObservableFuture also does so, with a
     * CompletionException holding this exception as its cause.
     * Similarly, if the computed ObservableFuture completes
     * exceptionally, then so does the returned ObservableFuture.
     *
     * @param fn the function returning a new ObservableFuture
     * @return the ObservableFuture
     */
    <U> ObservableFuture<U> thenCompose
    (Function<? super T, ObservableFuture<U>> fn);

    /**
     * Returns a ObservableFuture that upon completion, has the same
     * value as that produced asynchronously using the {@link
     * java.util.concurrent.ForkJoinPool#commonPool()} by the given function of the result
     * of this ObservableFuture.
     *
     * <p>If this ObservableFuture completes exceptionally, then the
     * returned ObservableFuture also does so, with a
     * CompletionException holding this exception as its cause.
     * Similarly, if the computed ObservableFuture completes
     * exceptionally, then so does the returned ObservableFuture.
     *
     * @param fn the function returning a new ObservableFuture
     * @return the ObservableFuture
     */
    <U> ObservableFuture<U> thenComposeAsync
    (Function<? super T, ObservableFuture<U>> fn);

    /**
     * Returns a ObservableFuture that upon completion, has the same
     * value as that produced asynchronously using the given executor
     * by the given function of this ObservableFuture.
     *
     * <p>If this ObservableFuture completes exceptionally, then the
     * returned ObservableFuture also does so, with a
     * CompletionException holding this exception as its cause.
     * Similarly, if the computed ObservableFuture completes
     * exceptionally, then so does the returned ObservableFuture.
     *
     * @param fn the function returning a new ObservableFuture
     * @param executor the executor to use for asynchronous execution
     * @return the ObservableFuture
     */
    <U> ObservableFuture<U> thenComposeAsync
    (Function<? super T, ObservableFuture<U>> fn,
     Executor executor);

    /**
     * Returns a new ObservableFuture that is completed when this
     * ObservableFuture completes, with the result of the given
     * function of the exception triggering this ObservableFuture's
     * completion when it completes exceptionally; otherwise, if this
     * ObservableFuture completes normally, then the returned
     * ObservableFuture also completes normally with the same value.
     *
     * @param fn the function to use to compute the value of the
     * returned ObservableFuture if this ObservableFuture completed
     * exceptionally
     * @return the new ObservableFuture
     */
    ObservableFuture<T> exceptionally
    (Function<Throwable, ? extends T> fn);

    /**
     * Returns a new ObservableFuture that is completed when this
     * ObservableFuture completes, with the result of the given
     * function of the result and exception of this ObservableFuture's
     * completion.  The given function is invoked with the result (or
     * {@code null} if none) and the exception (or {@code null} if none)
     * of this ObservableFuture when complete.
     *
     * @param fn the function to use to compute the value of the
     * returned ObservableFuture
     * @return the new ObservableFuture
     */
    <U> ObservableFuture<U> handle
    (BiFunction<? super T, Throwable, ? extends U> fn);

    /**
     * Returns a new ObservableFuture that is completed when all of
     * the given ObservableFutures complete.  If any of the given
     * ObservableFutures complete exceptionally, then the returned
     * ObservableFuture also does so, with a CompletionException
     * holding this exception as its cause.  Otherwise, the results,
     * if any, of the given ObservableFutures are not reflected in
     * the returned ObservableFuture, but may be obtained by
     * inspecting them individually. If no ObservableFutures are
     * provided, returns a ObservableFuture completed with the value
     * {@code null}.
     *
     * <p>Among the applications of this method is to await completion
     * of a set of independent ObservableFutures before continuing a
     * program, as in: {@code ObservableFuture.allOf(c1, c2,
     * c3).join();}.
     *
     * @param cfs the ObservableFutures
     * @return a new ObservableFuture that is completed when all of the
     * given ObservableFutures complete
     * @throws NullPointerException if the array or any of its elements are
     * {@code null}
     */
    public static ObservableFuture<Void> allOf(ObservableFuture<?>... cfs) {
        return null;
    }

    /**
     * Returns a new ObservableFuture that is completed when any of
     * the given ObservableFutures complete, with the same result.
     * Otherwise, if it completed exceptionally, the returned
     * ObservableFuture also does so, with a CompletionException
     * holding this exception as its cause.  If no ObservableFutures
     * are provided, returns an incomplete ObservableFuture.
     *
     * @param cfs the ObservableFutures
     * @return a new ObservableFuture that is completed with the
     * result or exception of any of the given ObservableFutures when
     * one completes
     * @throws NullPointerException if the array or any of its elements are
     * {@code null}
     */
    public static ObservableFuture<Object> anyOf(ObservableFuture<?>... cfs) {
        return null;
    }
}
