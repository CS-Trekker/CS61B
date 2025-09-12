package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comp;

    public MaxArrayDeque(Comparator<T> c) {
        super();
        comp = c;
    }

    public T max() {
        if (isEmpty()) {
            return null;
        }
        int maxIndex = 0;
        for (int i = 0; i < size(); i++) {
            if (comp.compare(get(maxIndex), get(i)) < 0) {
                maxIndex = i;
            }
        }
        return get(maxIndex);
    }

    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }
        int maxIndex = 0;
        for (int i = 0; i <size(); i++) {
            if (c.compare(get(maxIndex), get(i)) < 0) {
                maxIndex = i;
            }
        }
        return get(maxIndex);
    }
}
