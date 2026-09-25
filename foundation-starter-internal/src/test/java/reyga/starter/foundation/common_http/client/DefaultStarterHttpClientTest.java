package reyga.starter.foundation.common_http.client;

import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultStarterHttpClientTest {

    private static final Request REQUEST = new Request.Builder()
            .url("https://example.test/resource")
            .build();

    @Mock
    private OkHttpClient okHttpClient;

    @Mock
    private OkHttpClient cachingClient;

    @Mock
    private OkHttpClient.Builder clientBuilder;

    @Mock
    private Call call;

    @Mock
    private Response response;

    @Mock
    private Callback callback;

    @Mock
    private Cache cache;

    private DefaultStarterHttpClient starterHttpClient;

    @BeforeEach
    void setUp() {
        starterHttpClient = new DefaultStarterHttpClient(okHttpClient);
    }

    @Test
    void should_ReturnResponse_When_SynchronousCallSucceeds() throws IOException {
        // given
        when(okHttpClient.newCall(REQUEST)).thenReturn(call);
        when(call.execute()).thenReturn(response);

        // when
        Response result = starterHttpClient.call(REQUEST);

        // then
        assertSame(response, result);
        verify(okHttpClient).newCall(REQUEST);
        verify(call).execute();
    }

    @Test
    void should_PropagateIOException_When_SynchronousCallFails() throws IOException {
        // given
        IOException failure = new IOException("connection failed");
        when(okHttpClient.newCall(REQUEST)).thenReturn(call);
        when(call.execute()).thenThrow(failure);

        // when
        IOException result = assertThrows(IOException.class, () -> starterHttpClient.call(REQUEST));

        // then
        assertSame(failure, result);
        verify(okHttpClient).newCall(REQUEST);
        verify(call).execute();
    }

    @Test
    void should_EnqueueCallback_When_AsynchronousCallIsScheduled() {
        // given
        when(okHttpClient.newCall(REQUEST)).thenReturn(call);

        // when
        Call result = starterHttpClient.asynchronousCall(REQUEST, callback);

        // then
        assertSame(call, result);
        verify(okHttpClient).newCall(REQUEST);
        verify(call).enqueue(callback);
    }

    @Test
    void should_PropagateRuntimeException_When_AsynchronousCallCannotBeScheduled() {
        // given
        IllegalStateException failure = new IllegalStateException("dispatcher rejected call");
        when(okHttpClient.newCall(REQUEST)).thenReturn(call);
        doThrow(failure).when(call).enqueue(callback);

        // when
        IllegalStateException result = assertThrows(
                IllegalStateException.class,
                () -> starterHttpClient.asynchronousCall(REQUEST, callback)
        );

        // then
        assertSame(failure, result);
        verify(okHttpClient).newCall(REQUEST);
        verify(call).enqueue(callback);
    }

    @Test
    void should_UseDerivedClientWithCache_When_CachedCallIsExecuted() throws IOException {
        // given
        when(okHttpClient.newBuilder()).thenReturn(clientBuilder);
        when(clientBuilder.cache(cache)).thenReturn(clientBuilder);
        when(clientBuilder.build()).thenReturn(cachingClient);
        when(cachingClient.newCall(REQUEST)).thenReturn(call);
        when(call.execute()).thenReturn(response);

        // when
        Response result = starterHttpClient.callWithResponseCaching(REQUEST, cache);

        // then
        assertSame(response, result);
        verify(okHttpClient).newBuilder();
        verify(clientBuilder).cache(cache);
        verify(clientBuilder).build();
        verify(cachingClient).newCall(REQUEST);
        verify(call).execute();
        verifyNoInteractions(cache);
    }

    @Test
    void should_PropagateIOExceptionWithoutClosingCache_When_CachedCallFails() throws IOException {
        // given
        IOException failure = new IOException("cached call failed");
        when(okHttpClient.newBuilder()).thenReturn(clientBuilder);
        when(clientBuilder.cache(cache)).thenReturn(clientBuilder);
        when(clientBuilder.build()).thenReturn(cachingClient);
        when(cachingClient.newCall(REQUEST)).thenReturn(call);
        when(call.execute()).thenThrow(failure);

        // when
        IOException result = assertThrows(
                IOException.class,
                () -> starterHttpClient.callWithResponseCaching(REQUEST, cache)
        );

        // then
        assertSame(failure, result);
        verify(okHttpClient).newBuilder();
        verify(clientBuilder).cache(cache);
        verify(clientBuilder).build();
        verify(cachingClient).newCall(REQUEST);
        verify(call).execute();
        verifyNoInteractions(cache);
    }

    @Test
    void should_RejectNullClient_When_ClientIsConstructed() {
        // when
        NullPointerException result = assertThrows(
                NullPointerException.class,
                () -> new DefaultStarterHttpClient(null)
        );

        // then
        assertEquals("client must not be null", result.getMessage());
    }

    @Test
    void should_RejectNullRequestBeforeCallingDelegate_When_SynchronousCallIsInvoked() {
        // when
        NullPointerException result = assertThrows(
                NullPointerException.class,
                () -> starterHttpClient.call(null)
        );

        // then
        assertEquals("request must not be null", result.getMessage());
        verifyNoInteractions(okHttpClient);
    }

    @Test
    void should_RejectNullCallbackBeforeCallingDelegate_When_AsynchronousCallIsInvoked() {
        // when
        NullPointerException result = assertThrows(
                NullPointerException.class,
                () -> starterHttpClient.asynchronousCall(REQUEST, null)
        );

        // then
        assertEquals("callback must not be null", result.getMessage());
        verifyNoInteractions(okHttpClient);
    }

    @Test
    void should_RejectNullRequestBeforeCallbackValidation_When_AsynchronousCallIsInvoked() {
        // when
        NullPointerException result = assertThrows(
                NullPointerException.class,
                () -> starterHttpClient.asynchronousCall(null, null)
        );

        // then
        assertEquals("request must not be null", result.getMessage());
        verifyNoInteractions(okHttpClient);
    }

    @Test
    void should_RejectNullCacheBeforeCreatingClient_When_CachedCallIsInvoked() {
        // when
        NullPointerException result = assertThrows(
                NullPointerException.class,
                () -> starterHttpClient.callWithResponseCaching(REQUEST, null)
        );

        // then
        assertEquals("cache must not be null", result.getMessage());
        verifyNoInteractions(okHttpClient);
    }

    @Test
    void should_RejectNullRequestBeforeCacheValidation_When_CachedCallIsInvoked() {
        // when
        NullPointerException result = assertThrows(
                NullPointerException.class,
                () -> starterHttpClient.callWithResponseCaching(null, null)
        );

        // then
        assertEquals("request must not be null", result.getMessage());
        verifyNoInteractions(okHttpClient);
    }
}
