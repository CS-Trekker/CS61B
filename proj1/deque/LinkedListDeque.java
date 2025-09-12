package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {

    private int size;
    private Node sentinel;

    public LinkedListDeque() {
        size = 0;
        sentinel = new Node();
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
    }

    private class Node {
        T item;
        Node prev;
        Node next;

        Node(T i) {
            item = i;
        }

        Node() {}
    }

    @Override
    public void addLast(T item) {
        Node oldLast = sentinel.prev;
        
        sentinel.prev = new Node(item);
        sentinel.prev.next = sentinel;
        sentinel.prev.prev = oldLast;
        oldLast.next = sentinel.prev;

        size += 1;
    }

    @Override
    public void addFirst(T item) {
        Node oldFirst = sentinel.next;

        sentinel.next = new Node(item);
        sentinel.next.prev = sentinel;
        sentinel.next.next = oldFirst;
        oldFirst.prev = sentinel.next;

        size += 1;
    }

    @Override
    public T removeLast() {
        if (!isEmpty()) {
            T oldLastItem = sentinel.prev.item;
            sentinel.prev = sentinel.prev.prev;
            sentinel.prev.next = sentinel;
            size -= 1;
            return oldLastItem;
        } else {
            return null;
        }
    }

    @Override
    public T removeFirst() {
        if (!isEmpty()) {
            T oldFirstItem = sentinel.next.item;
            sentinel.next = sentinel.next.next;
            sentinel.next.prev = sentinel;
            size -= 1;
            return oldFirstItem;
        } else {
            return null;
        }
    }

    @Override
    public T get(int index) {
        if (index >= size) {
            return null;
        }
        Node p = sentinel.next;
        for (int i = 0; i < index; i++) {
            p = p.next;
        }
        return p.item;
    }

    private T helper(int feet, Node beginNode) {
        if (feet == 0) {
            return beginNode.item;
        } else {
            return helper(feet - 1, beginNode.next);
        }
    }

    public T getRecursive(int index) {
        if (index >= size) {
            return null;
        }
        return helper(index, sentinel.next);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        StringBuilder sb = new StringBuilder();
        Node p = sentinel.next;
        while (p != sentinel) {
            sb.append(p.item);
            sb.append(" ");
            p = p.next;
        }
        String result = sb.toString();
        System.out.println(result);
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private Node currentNode;

        public LinkedListDequeIterator() {
            currentNode = sentinel.next;
        }

        @Override
        public boolean hasNext() {
            return currentNode != sentinel;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException("No more elements to iterate over.");
            }
            T returnItem = currentNode.item;
            currentNode = currentNode.next;
            return returnItem;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
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
