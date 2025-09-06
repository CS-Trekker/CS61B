package deque;

public interface Deque<T> {
    void addLast(T item);
    void addFirst(T item);
    T removeLast();
    T removeFirst();
    T get(int index);
    boolean isEmpty();
    int size();
    void printDeque();
}
