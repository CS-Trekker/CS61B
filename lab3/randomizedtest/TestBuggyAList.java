package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> lst1 = new AListNoResizing<>();
        BuggyAList<Integer> lst2 = new BuggyAList<>();

        lst1.addLast(4);
        lst2.addLast(4);
        lst1.addLast(5);
        lst2.addLast(5);
        lst1.addLast(6);
        lst2.addLast(6);
        for (int i = 0; i < lst1.size(); i++) {
            assertEquals(lst1.get(i), lst2.get(i));
        }

       assertEquals(lst1.removeLast(), lst2.removeLast());
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> broken = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                broken.addLast(randVal);
                assertEquals(L.size(), broken.size());
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                assertEquals(L.size(), broken.size());
                System.out.println("size: " + size);
            } else if (operationNumber == 2) {
                // getLast
                if (L.size() == 0) {
                    continue;
                }
                assertEquals(L.getLast(), broken.getLast());
                System.out.println("getLast(" + L.getLast() + ")");
            } else if (operationNumber == 3) {
                // removeLast
                if (L.size() == 0) {
                    continue;
                }
                int L_rmlast = L.removeLast();
                assertEquals(L_rmlast, (int) broken.removeLast());
                System.out.println("removeLast(" + L_rmlast + ")");
            }
        }
    }
}
