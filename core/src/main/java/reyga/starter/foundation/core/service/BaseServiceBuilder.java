package reyga.starter.foundation.core.service;

import reyga.starter.foundation.common.logging.BaseLogging;
import reyga.starter.foundation.core.dto.content.BaseContent;
import reyga.starter.foundation.core.dto.request.BaseRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class BaseServiceBuilder<T extends BaseServiceBuilder<T, Q, R, C>, Q extends BaseRequest, R, C extends BaseContent> extends BaseLogging implements FoundationService<Q, R, C> {

    @Override
    public R execute(Q req, C content) {
        logInformation(req);
        return processFlow(req, content);
    };

    protected abstract R processFlow(Q req, C content);

    protected void logInformation(Q req) {
        log.info("Executing Service...");
        log.info("Request : ", req.toString());
    }

    private final List<Supplier<?>> processes = new ArrayList<>();
    private Q request;
    private C content;
    private Supplier<R> endProcess; // terminal process

    @SuppressWarnings("unchecked")
    public T service(Q request, C content) {
        this.request = request;
        this.content = content;
        processes.clear();
        registerProcesses(request, content);
        return (T) this;
    }

    protected abstract void registerProcesses(Q request, C content);

    protected void addProcess(Runnable process) {
        processes.add(() -> {
            process.run();
            return null;
        });
    }

    protected <X> void addProcess(Supplier<X> process) {
        processes.add(process);
    }

    protected void endProcess(Runnable process) {
        this.endProcess = () -> {
            process.run();
            return null;
        };
    }

    protected void endProcess(Supplier<R> process) {
        this.endProcess = process;
    }

    public R build() {
        for (Supplier<?> process : processes) {
            process.get();
        }

        if (endProcess == null) {
            throw new IllegalStateException("endProcess must be defined");
        }
        return endProcess.get();
    }

    protected Q getRequest() {
        return request;
    }

    protected C getContent() {
        return content;
    }
}
