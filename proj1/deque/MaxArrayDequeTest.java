package deque;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Comparator;

public class MaxArrayDequeTest {
    public static class intComp implements Comparator<Integer> {
        @Override
        public int compare(Integer o1, Integer o2) {
            return (o1 - o2);
        }
    }

    @Test
    public void maxTest() {
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<>(new intComp()); // 这里要传入一个Comparator的实例而不是整个类
        mad.addLast(1);
        mad.addLast(2);
        mad.addLast(3);
        mad.addLast(2);

        assertEquals(3, (int) mad.max());
    }
}
