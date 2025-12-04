package bstmap;

import java.util.*;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private Node root;
    private int size;

    public BSTMap() {
        root = null;
        size = 0;
    }

    private class Node {
        K key;
        V value;
        Node left;
        Node right;
        Node(K key, V value) {
            this.key = key;
            this.value = value;
            left = null;
            right = null;
        }
    }

    /**
     * Removes all of the mappings from this map.
     */
    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    /** Returns true if this map contains a mapping for the specified key. */
    @Override
    public boolean containsKey(K key) {
        Node cur = root;
        while (cur != null) {
            int cmp = key.compareTo(cur.key);
            if (cmp < 0) {
                cur = cur.left;
            } else if (cmp > 0) {
                cur = cur.right;
            } else {
                return true;
            }
        }
        return false;
    }

    /* Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    @Override
    public V get(K key) {
        if (getNode(key) != null) {
            return getNode(key).value;
        }
        return null;
    }

    private Node getNode(K key) {
        Node cur = root;
        while (cur != null) {
            int cmp = key.compareTo(cur.key);
            if (cmp < 0) {
                cur = cur.left;
            } else if (cmp > 0) {
                cur = cur.right;
            } else {
                return cur;
            }
        }
        return null;
    }

    /** Returns the number of key-value mappings in this map. */
    @Override
    public int size() {
        return size;
    }

    /** Associates the specified value with the specified key in this map. */
    @Override
    public void put(K key, V value) {
        root = putHelper(root, key, value);
    }

    private Node putHelper(Node node, K key, V value) {
        if (node == null) {
            size++;
            return new Node(key, value);
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = putHelper(node.left, key, value);
        } else if (cmp > 0) {
            node.right = putHelper(node.right, key, value);
        } else {
            node.value = value;
        }
        return node;
    }

    /* Returns a Set view of the keys contained in this map. Not required for Lab 7.
     * If you don't implement this, throw an UnsupportedOperationException. */
    /** Returns a Set view of the keys contained in this map. Not required for Lab 7.
     * If you don't implement this, throw an UnsupportedOperationException. */
    @Override
    public Set<K> keySet() {
        Set<K> kSet = new HashSet<>();
        keySetHelper(kSet, root);
        return kSet;
    }

    private void keySetHelper(Set<K> kSet, Node node) {
        if (node == null) {
            return;
        }
        keySetHelper(kSet, node.left);
        kSet.add(node.key);
        keySetHelper(kSet, node.right);
    }

    /* Removes the mapping for the specified key from this map if present.
     * Not required for Lab 7. If you don't implement this, throw an
     * UnsupportedOperationException. */
    @Override
    public V remove(K key) {
        if (!containsKey(key)) {
            return null;
        }
        Node nodeToBeRemoved = getNode(key);
        V result = nodeToBeRemoved.value;
        root = removeHelper(root, key);
        return result;
    }

    private Node removeHelper(Node node, K key) {
        if (node == null) {
            return null;
        }
        if (key.compareTo(node.key) == 0) {
            size--;
            if (node.left == null) {
                if (node.right == null) {
                    return null;
                }
                return node.right;
            } else {
                if (node.right == null) {
                    return node.left;
                }
                Node successor = getSuccessor(node);
                node.key = successor.key;
                node.value = successor.value;
                node.right = removeHelper(node.right, successor.key);
                return node;
            }
        } else if (key.compareTo(node.key) < 0) {
            node.left = removeHelper(node.left, key);
            return node;
        }
        node.right = removeHelper(node.right, key);
        return node;
    }

    private Node getSuccessor(Node node) {
        Node cur = node.right;
        while (cur.left != null) {
            cur = cur.left;
        }
        return cur;
    }

    /* Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 7. If you don't implement this,
     * throw an UnsupportedOperationException.*/
    @Override
    public V remove(K key, V value) {
        if (!containsKey(key)) {
            return null;
        }
        if (get(key).equals(value)) {
            return remove(key);
        } else {
            return null;
        }
    }

    /** Returns an iterator over elements of type {@code T}. */
    @Override
    public Iterator<K> iterator() {
        return new bstMapIterator();
    }

    private class bstMapIterator implements Iterator<K> {
        private final LinkedList<K> keys;

        public bstMapIterator() {
            keys = new LinkedList<>(keySet());
        }

        @Override
        public boolean hasNext() {
            return !keys.isEmpty();
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return keys.removeFirst();
        }
    }
}
