package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private int size;
    private T[] items;
    private int nextFirst;
    private int nextLast;

    public ArrayDeque() {
        size = 0;
        items = (T[]) new Object[8]; // 这个地方不能直接用new T[8],会报错
        nextFirst = items.length - 1;
        nextLast = 0;
    }

    private void resize(int cap) {
        int oldItemsLength = items.length;
        T[] a = (T[]) new Object[cap];
        for (int i = 0; i < size; i++) {
            a[i] = items[realIndex(i)];
        }
        items = a;
        nextFirst = cap - 1;
        nextLast = oldItemsLength;
    }

    private int realIndex(int index) {
        return (index % items.length + items.length) % items.length;
    }

    @Override
    public void addLast(T item) {
        if (size == items.length) {
            resize(size * 2);
        }
        items[nextLast] = item;
        nextLast = realIndex(nextLast + 1);
        size += 1;
    }

    @Override
    public void addFirst(T item) {
        if (size == items.length) {
            resize(size * 2);
        }
        items[nextFirst] = item;
        nextFirst = realIndex(nextFirst - 1);
        size += 1;
    }

    @Override
    public T removeLast() {
        if (!isEmpty()) {
            T returnItem = items[realIndex(nextLast - 1)];
            items[realIndex(nextLast - 1)] = null; // 让垃圾回收器回收掉被移除的对象，防止“对象游离”（loitering）
            nextLast = realIndex(nextLast - 1);
            size -= 1;
            if (items.length >= 16 && ((double) size / items.length) < 0.25) {
                resize(items.length / 2);
            }
            return returnItem;
        } else {
            return null;
        }
    }

    @Override
    public T removeFirst() {
        if (!isEmpty()) {
            T returnItem = items[realIndex(nextFirst + 1)];
            items[realIndex(nextFirst + 1)] = null;
            nextFirst = realIndex(nextFirst + 1);
            size -= 1;
            if (items.length >= 16 && ((double) size / items.length) < 0.25) {
                resize(items.length / 2);
            }
            return returnItem;
        } else {
            return null;
        }
    }

    @Override
    public T get(int index) {
        if (index >= size) {
            return null;
        }
        return items[realIndex(nextFirst + 1 + index)];
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            T item = get(i);
            sb.append(item);
            sb.append(" ");
        }
        String result = sb.toString();
        System.out.println(result);
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int currentIndex;
        private int iteratorTimes;
        private int oldSize = size();

        public ArrayDequeIterator() {
            currentIndex = realIndex(nextFirst + 1);
            iteratorTimes = 0;
        }

        @Override
        public boolean hasNext() {
            return iteratorTimes < oldSize;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException("No more elements to iterate over.");
            }
            T returnItem = items[currentIndex];
            currentIndex = realIndex(currentIndex + 1);
            iteratorTimes += 1;
            return returnItem;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof Deque)) {
            return false;
        }

        Deque deqO = (Deque) o;
        if (size() != deqO.size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (!get(i).equals(deqO.get(i))) {
                return false;
            }
        }
        return true;
    }
}
