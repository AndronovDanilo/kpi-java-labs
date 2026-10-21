package ua.kpi.comsys.collections;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ActionHistoryManager {

    private final int maxCapacity;
    private final Deque<String> history;

    public ActionHistoryManager(int maxCapacity) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("maxCapacity must be greater than 0");
        }
        this.maxCapacity = maxCapacity;
        this.history = new ArrayDeque<>(maxCapacity);
    }

    public void performAction(String action) {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action cannot be null or blank");
        }
        if (history.size() == maxCapacity) {
            history.removeLast();
        }
        history.addFirst(action);
    }

    public String undo() {
        return history.pollFirst();
    }

    public String peekRecentAction() {
        return history.peekFirst();
    }

    public List<String> getHistory() {
        return new ArrayList<>(history);
    }

    public int getHistorySize() {
        return history.size();
    }

    public void clear() {
        history.clear();
    }
}
