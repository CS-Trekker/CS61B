package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        AList<Integer> Ns = new AList<>();
        for (int N = 1000; N <= 128000; N *= 2) {
            Ns.addLast(N);
        }

        AList<Integer> opCounts = new AList<>();
        for (int i = 0; i < Ns.size(); i++) {
            opCounts.addLast(10000);
        }

        AList<Double> times = new AList<>();
        for (int j = 0; j < Ns.size(); j++) {
            SLList<Integer> slst = new SLList<>();
            for (int k = 1; k <= Ns.get(j); k++) {
                slst.addLast(k);
            }

            Stopwatch sw = new Stopwatch();
            for (int m = 0; m < opCounts.get(j); m++) {
                int foo = slst.getLast();
            }
            Double time = sw.elapsedTime();
            times.addLast(time);
        }

        printTimingTable(Ns, times, opCounts);
    }

}
