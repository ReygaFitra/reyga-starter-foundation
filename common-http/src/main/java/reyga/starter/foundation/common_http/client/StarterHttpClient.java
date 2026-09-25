package reyga.starter.foundation.common_http.client;

import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Executes synchronous and asynchronous HTTP requests using OkHttp contracts.
 *
 * <p>Callers must close every successful synchronous {@link Response}, preferably
 * with a try-with-resources statement. Implementations are expected to be safe for
 * concurrent use.</p>
 */
public interface StarterHttpClient {

    /**
     * Executes a request synchronously.
     *
     * @param request request to execute; must not be {@code null}
     * @return the HTTP response, which must be closed by the caller
     * @throws IOException if the request cannot be executed because of a connectivity,
     *                     protocol, or cancellation failure
     * @throws NullPointerException if {@code request} is {@code null}
     */
    Response call(Request request) throws IOException;

    /**
     * Schedules a request for asynchronous execution.
     *
     * <p>The supplied callback receives either the failure or a response. The callback
     * is responsible for closing a received response.</p>
     *
     * @param request request to execute; must not be {@code null}
     * @param callback callback notified when the request completes or fails; must not be
     *                 {@code null}
     * @return the scheduled call, which may be used to inspect or cancel the request
     * @throws NullPointerException if {@code request} or {@code callback} is {@code null}
     */
    Call asynchronousCall(Request request, Callback callback);

    /**
     * Executes a request synchronously using the supplied response cache.
     *
     * <p>HTTP caching rules still apply; providing a cache does not make an otherwise
     * non-cacheable response cacheable. This client does not close the cache because its
     * lifecycle remains owned by the caller.</p>
     *
     * @param request request to execute; must not be {@code null}
     * @param cache response cache to use; must not be {@code null}
     * @return the HTTP response, which must be closed by the caller
     * @throws IOException if the request cannot be executed because of a connectivity,
     *                     protocol, cache, or cancellation failure
     * @throws NullPointerException if {@code request} or {@code cache} is {@code null}
     */
    Response callWithResponseCaching(Request request, Cache cache) throws IOException;

}
