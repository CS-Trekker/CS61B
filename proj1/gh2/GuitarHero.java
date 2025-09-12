package gh2;
import deque.ArrayDeque;
import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

import java.util.ArrayList;
import java.util.List;

/**
 * A client that uses the synthesizer package to replicate a plucked guitar string sound
 */
public class GuitarHero {
    public static void main(String[] args) {
        String keyboard = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";

        /* 创建一个list，存放6个deque,用来放置37个GuitarString,第i个 */
        List<ArrayDeque<GuitarString>> guitarStrings = new ArrayList<>(); // List只是个接口（抽象的），需要使用实现类
        for (int i = 0; i < 6; i++) {
            ArrayDeque<GuitarString> stringGroup = new ArrayDeque<>();
            guitarStrings.add(stringGroup);
        }

        for (int i = 0; i < 37; i++) {
            double ithFrequency = 440 * Math.pow(2, (double) (i - 24) / 12);
            GuitarString ithGs = new GuitarString(ithFrequency);
            int toWhichDeque = i % 6;
            guitarStrings.get(toWhichDeque).addLast(ithGs);
        }

        while (true) {

            /* check if the user has typed a key; if so, process it */
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                int keyIndex = keyboard.indexOf(key);
                int whichGroup = keyIndex % 6;
                int whereInGroup = keyIndex / 6;

                for (int i = 0; i < guitarStrings.get(whichGroup).size(); i++) {
                    if (i == whereInGroup) {
                        guitarStrings.get(whichGroup).get(i).pluck();
                    } else {
                        guitarStrings.get(whichGroup).get(i).mute();
                    }
                }
            }

            /* compute the superposition of samples */
            double sample = 0;
            for (int i = 0; i < 37; i++) {
                int whichGroup = i % 6;
                int whereInGroup = i / 6;

                sample += guitarStrings.get(whichGroup).get(whereInGroup).sample();
            }

            /* play the sample on standard audio */
            StdAudio.play(sample);

            /* advance the simulation of each guitar string by one step */
            for (int i = 0; i < 37; i++) {
                int whichGroup = i % 6;
                int whereInGroup = i / 6;

                guitarStrings.get(whichGroup).get(whereInGroup).tic();
            }
        }
    }
}

