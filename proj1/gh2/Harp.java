package gh2;

import deque.ArrayDeque;
import deque.Deque;

//Note: This file will not compile until you complete the Deque implementations
public class Harp {
    /** Constants. Do not change. In case you're curious, the keyword final
     * means the values cannot be changed at runtime. We'll discuss this and
     * other topics in lecture on Friday. */
    private static final int SR = 44100;      // Sampling Rate
    private static final double DECAY = .996; // energy decay factor

    /* Buffer for storing sound data. */
    private Deque<Double> buffer;

    /* Create a harp string of the given frequency.  */
    public Harp(double frequency) {
        buffer = new ArrayDeque<>();
        int cap = (int) Math.round(SR / frequency) / 2;
        for (int i = 0; i < cap; i++) {
            buffer.addLast(0.0);
        }
    }


    /* Pluck the harp string by replacing the buffer with white noise. */
    public void pluck() {
        //       Make sure that your random numbers are different from each
        //       other. This does not mean that you need to check that the numbers
        //       are different from each other. It means you should repeatedly call
        //       Math.random() - 0.5 to generate new random numbers for each array index.
        int originSize = buffer.size();
        for (int i = 0; i < originSize; i++) {
            buffer.removeLast();
        }
        for (int i = 0; i < originSize; i++) {
            double r = Math.random() - 0.5;
            buffer.addLast(r);
        }
    }

    /* Advance the simulation one time step by performing one iteration of
     * the Karplus-Strong algorithm.
     */
    public void tic() {
        double oldFirst = buffer.removeFirst();
        double newFirst = buffer.get(0);
        double newLast = DECAY * 0.5 * (oldFirst + newFirst);
        buffer.addLast(-newLast);
    }

    /* Return the double at the front of the buffer. */
    public double sample() {
        return buffer.get(0);
    }
}
