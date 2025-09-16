package reyga.starter.foundation.core.process.node;

import java.util.*;

public abstract class AbstractProcessBuilder {
    protected final Map<String, Object> contentService = new HashMap<>();
    protected final List<String> processIdList = new ArrayList<>();
    protected final Map<String, Map<String, ProcessNode>> subProcessMap = new HashMap<>();
    protected final List<ProcessNode> mainProcesses = new ArrayList<>();

    protected void addNode(ProcessNode node) {
        if (processIdList.isEmpty()) {
            mainProcesses.add(node);
        } else {
            String parentId = processIdList.get(processIdList.size() - 1);
            subProcessMap.get(parentId).put(node.getProcessId(), node);
        }
    }

    protected void initializeSubProcess(String processId) {
        subProcessMap.put(processId, new LinkedHashMap<>());
    }

    protected void closeLastProcess(String type) {
        if (processIdList.isEmpty()) {
            throw new IllegalStateException(type + " must be started before ending");
        }
        String lastId = processIdList.remove(processIdList.size() - 1);
        Map<String, ProcessNode> subNodes = subProcessMap.get(lastId);
        ProcessNode parentNode = getNode(lastId);
        parentNode.setSubProcesses(subNodes);
        subProcessMap.remove(lastId);
    }

    protected ProcessNode getNode(String processId) {
        return mainProcesses.stream()
                .filter(n -> n.getProcessId().equals(processId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Node not found: " + processId));
    }

    public void end() {
        for (ProcessNode node : mainProcesses) {
            node.execute(contentService);
        }
    }
}
