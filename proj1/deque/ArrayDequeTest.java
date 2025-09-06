package deque;

import org.junit.Test;
import static org.junit.Assert.*;

public class ArrayDequeTest {
    @Test
    public void addRemoveTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        assertTrue(ad.isEmpty());
        for (int i = 0; i < 100; i++) {
            ad.addLast(i);
        }
        int adSize = ad.size();
        assertEquals(100, adSize);
        assertFalse(ad.isEmpty());
        ad.removeFirst();
        ad.removeLast();
        ad.removeFirst();
        ad.removeLast();
        for (int j = 2; j < 98; j++) {
            int actualInt = ad.get(j - 2);
            assertEquals(j, actualInt);
        }
        int adSize1 = ad.size();
        assertEquals(96, adSize1);
    }

    @Test
    public void iterationTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        for (int i = 0; i < 10; i++) {
            ad.addLast(i);
        }
        for (int item : ad) {
            System.out.print(item);
            System.out.print(" ");
        }
        System.out.println();
    }

    @Test
    public void printDequeTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        for (int i = 0; i < 10; i++) {
            ad.addLast(i);
        }
        ad.printDeque();
    }
}
