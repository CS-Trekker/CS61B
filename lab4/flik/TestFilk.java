package flik;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestFilk {
    @Test
    public void testFilk() {
        int j = 0;
        for (int i = 0; i < 1000; i++) {
            assertTrue(i + " doesn't equals to " + j,Flik.isSameNumber(i, j));
            j += 1;
        }
    }
}
