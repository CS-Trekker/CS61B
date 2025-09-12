package gh2;
import deque.ArrayDeque;
import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

/**
 * A client that uses the synthesizer package to replicate a plucked guitar string sound
 */
public class GuitarHero {
    public static void main(String[] args) {
        String keyboard = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";

        /* 创建一个deque，存放37个GuitarString */
        ArrayDeque<GuitarString> guitarStrings = new ArrayDeque<>();
        for (int i = 1; i <= 37; i++) {
            double ith_frequency = 440 * Math.pow(2, (double) (i - 24) / 12);
            GuitarString ith_gs = new GuitarString(ith_frequency);
            guitarStrings.addLast(ith_gs);
        }

        while (true) {

            /* check if the user has typed a key; if so, process it */
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                int key_index = keyboard.indexOf(key);
                guitarStrings.get(key_index).pluck();
            }

            /* compute the superposition of samples */
            double sample = 0;
            for (int i = 0; i < 37; i++) {
                sample += guitarStrings.get(i).sample();
            }

            /* play the sample on standard audio */
            StdAudio.play(sample);

            /* advance the simulation of each guitar string by one step */
            for (int i = 0; i < 37; i++) {
                guitarStrings.get(i).tic();
            }
        }
    }
}

