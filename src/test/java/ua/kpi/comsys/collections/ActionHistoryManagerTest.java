package ua.kpi.comsys.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ActionHistoryManager Test Suite")
class ActionHistoryManagerTest {

    @Nested
    @DisplayName("Constructor Validation Tests")
    class ConstructorTests {

        @ParameterizedTest(name = "Invalid capacity: {0}")
        @ValueSource(ints = {0, -1, -5, -100})
        void testInvalidCapacityThrows(int capacity) {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> new ActionHistoryManager(capacity)
            );
            assertEquals("maxCapacity must be greater than 0", ex.getMessage());
        }

        @Test
        @DisplayName("Valid capacity creates empty manager")
        void testValidCapacity() {
            ActionHistoryManager manager = new ActionHistoryManager(5);
            assertEquals(0, manager.getHistorySize());
            assertTrue(manager.getHistory().isEmpty());
            assertNull(manager.peekRecentAction());
            assertNull(manager.undo());
        }
    }

    @Nested
    @DisplayName("PerformAction Validation & Functionality")
    class PerformActionTests {

        @ParameterizedTest(name = "Invalid action: \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "   ", "\n"})
        void testInvalidActionThrows(String action) {
            ActionHistoryManager manager = new ActionHistoryManager(3);
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> manager.performAction(action)
            );
            assertEquals("Action cannot be null or blank", ex.getMessage());
        }

        @Test
        @DisplayName("Actions added to head in LIFO order")
        void testLIFOOrdering() {
            ActionHistoryManager manager = new ActionHistoryManager(5);
            manager.performAction("Type A");
            manager.performAction("Type B");
            manager.performAction("Type C");

            assertEquals(3, manager.getHistorySize());
            assertEquals("Type C", manager.peekRecentAction());
            assertEquals(List.of("Type C", "Type B", "Type A"), manager.getHistory());
        }

        @Test
        @DisplayName("Capacity limit evicts oldest actions when exceeded")
        void testCapacityEviction() {
            ActionHistoryManager manager = new ActionHistoryManager(3);
            manager.performAction("Action 1");
            manager.performAction("Action 2");
            manager.performAction("Action 3");

            assertEquals(3, manager.getHistorySize());
            assertEquals(List.of("Action 3", "Action 2", "Action 1"), manager.getHistory());

            manager.performAction("Action 4");
            assertEquals(3, manager.getHistorySize());
            assertEquals(List.of("Action 4", "Action 3", "Action 2"), manager.getHistory());

            manager.performAction("Action 5");
            assertEquals(3, manager.getHistorySize());
            assertEquals(List.of("Action 5", "Action 4", "Action 3"), manager.getHistory());
        }

        @Test
        @DisplayName("Single capacity queue behaves as single-element history")
        void testSingleCapacity() {
            ActionHistoryManager manager = new ActionHistoryManager(1);
            manager.performAction("First");
            assertEquals(1, manager.getHistorySize());
            assertEquals("First", manager.peekRecentAction());

            manager.performAction("Second");
            assertEquals(1, manager.getHistorySize());
            assertEquals("Second", manager.peekRecentAction());
            assertEquals(List.of("Second"), manager.getHistory());
        }
    }

    @Nested
    @DisplayName("Undo and Peek Operations")
    class UndoAndPeekTests {

        @Test
        @DisplayName("Undo removes and returns most recent action")
        void testUndo() {
            ActionHistoryManager manager = new ActionHistoryManager(4);
            manager.performAction("Step 1");
            manager.performAction("Step 2");
            manager.performAction("Step 3");

            assertEquals("Step 3", manager.undo());
            assertEquals(2, manager.getHistorySize());
            assertEquals("Step 2", manager.peekRecentAction());

            assertEquals("Step 2", manager.undo());
            assertEquals(1, manager.getHistorySize());
            assertEquals("Step 1", manager.peekRecentAction());

            assertEquals("Step 1", manager.undo());
            assertEquals(0, manager.getHistorySize());
            assertNull(manager.peekRecentAction());

            assertNull(manager.undo());
            assertEquals(0, manager.getHistorySize());
        }

        @Test
        @DisplayName("Peek returns action without removing it")
        void testPeekDoesNotMutate() {
            ActionHistoryManager manager = new ActionHistoryManager(3);
            manager.performAction("Persist");

            assertEquals("Persist", manager.peekRecentAction());
            assertEquals("Persist", manager.peekRecentAction());
            assertEquals(1, manager.getHistorySize());
        }

        @Test
        @DisplayName("Peek on empty returns null")
        void testPeekEmpty() {
            ActionHistoryManager manager = new ActionHistoryManager(3);
            assertNull(manager.peekRecentAction());
        }
    }

    @Nested
    @DisplayName("GetHistory Immutability and State Query Tests")
    class HistoryStateTests {

        @Test
        @DisplayName("Modifying list from getHistory does not affect internal history")
        void testGetHistoryDefensiveCopy() {
            ActionHistoryManager manager = new ActionHistoryManager(3);
            manager.performAction("Original 1");
            manager.performAction("Original 2");

            List<String> snapshot = manager.getHistory();
            assertEquals(2, snapshot.size());

            snapshot.add("Injected");
            snapshot.clear();

            assertEquals(2, manager.getHistorySize());
            assertEquals(List.of("Original 2", "Original 1"), manager.getHistory());
        }

        @Test
        @DisplayName("Clear removes all elements")
        void testClear() {
            ActionHistoryManager manager = new ActionHistoryManager(5);
            manager.performAction("A");
            manager.performAction("B");
            manager.performAction("C");
            assertEquals(3, manager.getHistorySize());

            manager.clear();
            assertEquals(0, manager.getHistorySize());
            assertTrue(manager.getHistory().isEmpty());
            assertNull(manager.peekRecentAction());
            assertNull(manager.undo());

            manager.performAction("Fresh");
            assertEquals(1, manager.getHistorySize());
            assertEquals("Fresh", manager.peekRecentAction());
        }
    }
}
