package tester;

import static org.junit.Assert.*;
import org.junit.Test;
import student.StudentArrayDeque;
import edu.princeton.cs.introcs.StdRandom;

public class TestArrayDequeEC {
    @Test
    public void test1() {
        StudentArrayDeque<Integer> sad = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> ads = new ArrayDequeSolution<>();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 100; i += 1) {
            double numberBetweenZeroAndOne = StdRandom.uniform();

            if (numberBetweenZeroAndOne < 0.5) {
                sad.addLast(i);
                ads.addLast(i);
            } else {
                sad.addFirst(i);
                ads.addFirst(i);
            }
        }

        for (int i = 0; i < 10000; i++) {
            double numBetweenZeroAndOne = StdRandom.uniform();
            int intBetweenZeroAndHundred = StdRandom.uniform(100); // 0到99的随机整数
            if (numBetweenZeroAndOne < 0.25) {
                sad.addFirst(intBetweenZeroAndHundred);
                ads.addFirst(intBetweenZeroAndHundred);

                sb.append("addFirst(" + intBetweenZeroAndHundred + ")");
                sb.append("\n");
            } else if (numBetweenZeroAndOne < 0.5) {
                sad.addLast(intBetweenZeroAndHundred);
                ads.addLast(intBetweenZeroAndHundred);

                sb.append("addLast(" + intBetweenZeroAndHundred + ")");
                sb.append("\n");
            } else if (numBetweenZeroAndOne < 0.75) {
                if (!sad.isEmpty() && !ads.isEmpty()) {
                    Integer sadNum = sad.removeFirst();
                    Integer adsNum = ads.removeFirst();

                    sb.append("removeFirst()");
                    sb.append("\n");

                    assertEquals(sb.toString(), adsNum, sadNum);
                }
            } else {
                if (!sad.isEmpty() && !ads.isEmpty()) {
                    Integer sadNum = sad.removeLast();
                    Integer adsNum = ads.removeLast();

                    sb.append("removeLast()");
                    sb.append("\n");

                    assertEquals(sb.toString(), adsNum, sadNum);
                }
            }
        }
    }
}
