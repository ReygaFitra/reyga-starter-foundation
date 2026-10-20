package reyga.starter.foundation.common_http.client;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import reyga.starter.foundation.common_http.config.HttpClientProperties;

/**
 * Default {@link StarterHttpClient} backed by a reusable {@link OkHttpClient}.
 *
 * <p>The underlying client is shared across calls so its connection pool and dispatcher
 * can be reused. This class is thread-safe when the supplied OkHttp client is used according
 * to its contract.</p>
 */
public class DefaultStarterHttpClient implements StarterHttpClient {

    private final OkHttpClient client;
    private final HttpOperationRegistry registry;

    /**
     * Creates a client with OkHttp's default configuration.
     */
    public DefaultStarterHttpClient() {
        this(new OkHttpClient());
    }

    /**
     * Creates a client that delegates to the supplied OkHttp client.
     *
     * @param client reusable OkHttp client; must not be {@code null}
     * @throws NullPointerException if {@code client} is {@code null}
     */
    public DefaultStarterHttpClient(OkHttpClient client) {
        this(client, new HttpOperationRegistry(
                new HttpClientProperties(null, null, null, null, null, null, null),
                new DefaultHttpBodyCodec()));
    }

    /**
     * Creates a client with registered API defaults.
     * @param client reusable transport
     * @param registry validated operation registry
     */
    public DefaultStarterHttpClient(OkHttpClient client, HttpOperationRegistry registry) {
        this.client = Objects.requireNonNull(client, "client must not be null");
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
    }

    /** {@inheritDoc} */
    @Override
    public <T, R> HttpOperations.Builder<T, R> operationBuilder() {
        return registry.builder();
    }

    /** {@inheritDoc} */
    @Override
    public Response execute(HttpOperations<?, ?> operation) throws IOException {
        return newCall(operation).execute();
    }

    /** {@inheritDoc} */
    @Override
    public Call executeAsync(HttpOperations<?, ?> operation, Callback callback) {
        Objects.requireNonNull(callback, "callback must not be null");
        Call call = newCall(operation);
        call.enqueue(callback);
        return call;
    }

    private Call newCall(HttpOperations<?, ?> operation) {
        Objects.requireNonNull(operation, "operation must not be null");
        Call call = client.newCall(operation.request());
        if (operation.callTimeout() != null) {
            call.timeout().timeout(operation.callTimeout().toNanos(), java.util.concurrent.TimeUnit.NANOSECONDS);
        }
        return call;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response call(Request request) throws IOException {
        return client.newCall(requireRequest(request)).execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Call asynchronousCall(Request request, Callback callback) {
        Request requiredRequest = requireRequest(request);
        Callback requiredCallback = Objects.requireNonNull(callback, "callback must not be null");
        Call call = client.newCall(requiredRequest);
        call.enqueue(requiredCallback);
        return call;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response callWithResponseCaching(Request request, Cache cache) throws IOException {
        Request requiredRequest = requireRequest(request);
        Cache requiredCache = Objects.requireNonNull(cache, "cache must not be null");
        OkHttpClient cachingClient = client.newBuilder()
                .cache(requiredCache)
                .build();
        return cachingClient.newCall(requiredRequest).execute();
    }

    private Request requireRequest(Request request) {
        return Objects.requireNonNull(request, "request must not be null");
    }
}
