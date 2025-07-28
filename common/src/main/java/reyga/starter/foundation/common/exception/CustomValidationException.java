package reyga.starter.foundation.common.exception;

import java.util.*;

public class CustomValidationException extends RuntimeException {

    private Set<String> errorSet;
    private Map<String, String> errorHashMap = new HashMap<>();
    private List< Map<String, String>> errorMapList = new LinkedList<>();


    public CustomValidationException(Set<String> errorMessage) {
        super(errorMessage.toString());
        this.errorSet = errorMessage;
    }

    public CustomValidationException(Map<String, String> errorMessage) {
        super(errorMessage.toString());
        this.errorHashMap = errorMessage;
    }

    public CustomValidationException(List<Map<String, String>> errorMapList) {
        super(errorMapList.toString());
        this.errorMapList = errorMapList;
    }

    public Set<String> getErrorSet() {
        return errorSet;
    }

    public void setErrorSet(Set<String> errorSet) {
        this.errorSet = errorSet;
    }

    public Map<String, String> getErrorHashMap() {
        return errorHashMap;
    }

    public void setErrorHashMap(Map<String, String> errorHashMap) {
        this.errorHashMap = errorHashMap;
    }

    public List<Map<String, String>> getErrorMapList() {
        return errorMapList;
    }

    public void setErrorMapList(List<Map<String, String>> errorMapList) {
        this.errorMapList = errorMapList;
    }
}
