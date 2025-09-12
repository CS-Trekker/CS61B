package deque;

public interface Deque<T> {
    void addLast(T item);
    void addFirst(T item);
    T removeLast();
    T removeFirst();
    T get(int index);
    default boolean isEmpty() {
        return size() == 0;
    };
    int size();
    void printDeque();
}
