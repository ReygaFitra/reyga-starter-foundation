package reyga.starter.foundation.core.process.node;

import java.util.*;

public abstract class AbstractProcessBuilder {
    protected final Map<String, Object> contentService = new HashMap<>();
    protected final List<String> processIdStack = new ArrayList<>();
    protected final Map<String, Map<String, ProcessNode>> subProcessMap = new HashMap<>();
    protected final LinkedHashMap<String, ProcessNode> mainProcesses = new LinkedHashMap<>();

    public <R> R getContent(String processId, Class<R> type) {
        Object value = contentService.get(processId);
        if (value == null) return null;
        if (!type.isInstance(value)) {
            throw new ClassCastException("Process [" + processId + "] result is not of type " + type.getName());
        }
        return type.cast(value);
    }

    @SuppressWarnings("unchecked")
    public <R> R getContent(String processId) {
        return (R) contentService.get(processId);
    }

    protected void addNode(ProcessNode node) {
        if (processIdStack.isEmpty()) {
            mainProcesses.put(node.getProcessId(), node);
        } else {
            String parentId = processIdStack.get(processIdStack.size() - 1);
            subProcessMap.get(parentId).put(node.getProcessId(), node);
        }
    }

    protected void initializeSubProcess(String processId) {
        subProcessMap.put(processId, new LinkedHashMap<>());
    }

    public void closeLastProcess(String type) {
        if (processIdStack.isEmpty()) {
            throw new IllegalStateException(type + " must be started before ending");
        }
        String lastId = processIdStack.remove(processIdStack.size() - 1);
        Map<String, ProcessNode> subNodes = subProcessMap.get(lastId);
        ProcessNode parentNode = getNode(lastId);
        parentNode.setSubProcesses(subNodes);
    }

    public void executeSubProcesses(String processId) {
        Map<String, ProcessNode> subNodes = subProcessMap.get(processId);
        if (subNodes != null) {
            for (ProcessNode sub : subNodes.values()) {
                sub.execute(contentService);
            }
        }
    }

    public ProcessNode getNode(String processId) {
        if (mainProcesses.containsKey(processId)) {
            return mainProcesses.get(processId);
        }
        for (Map<String, ProcessNode> subMap : subProcessMap.values()) {
            if (subMap.containsKey(processId)) {
                return subMap.get(processId);
            }
        }
        throw new IllegalStateException("Node not found: " + processId);
    }

}
