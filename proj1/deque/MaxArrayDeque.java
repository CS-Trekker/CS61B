package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    public Comparator<T> comp;

    public MaxArrayDeque(Comparator<T> c) {
        super();
        comp = c;
    }

    public T max() {
        if (isEmpty()) {
            return null;
        }
        int max_index = 0;
        for (int i = 0; i < size(); i++) {
            if (comp.compare(get(max_index),get(i)) < 0) {
                max_index = i;
            }
        }
        return get(max_index);
    }

    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }
        int max_index = 0;
        for (int i = 0; i <size(); i++) {
            if (c.compare(get(max_index), get(i)) < 0) {
                max_index = i;
            }
        }
        return get(max_index);
    }
}
