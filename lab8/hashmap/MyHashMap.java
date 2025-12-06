package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author CS-Trekker
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private int size;  // key的个数
    private int bucketNum;
    private Set<K> keySet;
    private final int initialBucketNum;
    private final double maxLoadFactor;

    /** Constructors */
    public MyHashMap() {
        size = 0;
        keySet = new HashSet<>();
        initialBucketNum = 16;
        bucketNum = initialBucketNum;
        buckets = createTable(initialBucketNum);
        maxLoadFactor = 0.75;
    }

    public MyHashMap(int initialSize) {
        size = 0;
        keySet = new HashSet<>();
        initialBucketNum = initialSize;
        bucketNum = initialBucketNum;
        buckets = createTable(initialBucketNum);
        maxLoadFactor = 0.75;
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        size = 0;
        keySet = new HashSet<>();
        initialBucketNum = initialSize;
        bucketNum = initialBucketNum;
        buckets = createTable(initialBucketNum);
        maxLoadFactor = maxLoad;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] table = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            table[i] = createBucket();
        }
        return table;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    /** Removes all of the mappings from this map. */
    @Override
    public void clear() {
        size = 0;
        keySet = new HashSet<>();
        bucketNum = initialBucketNum;
        buckets = createTable(initialBucketNum);
    }

    /** Returns true if this map contains a mapping for the specified key. */
    @Override
    public boolean containsKey(K key) {
        return keySet.contains(key);
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    @Override
    public V get(K key) {
        if (!containsKey(key)) {
            return null;
        }
        int bucketIndex = Math.floorMod(key.hashCode(), buckets.length);
        for (Node node : buckets[bucketIndex]) {
            if (key.equals(node.key)) {
                return node.value;
            }
        }
        return null;
    }

    /** Returns the number of key-value mappings in this map. */
    @Override
    public int size() {
        return size;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key,
     * the old value is replaced.
     */
    @Override
    public void put(K key, V value) {
        int bucketIndex = Math.floorMod(key.hashCode(), bucketNum);
        if (!containsKey(key)) {
            size++;
            keySet.add(key);
            buckets[bucketIndex].add(new Node(key, value));
            if ((double) size / bucketNum > maxLoadFactor) {
                resize(bucketNum * 2);
            }
        } else {
            for (Node node : buckets[bucketIndex]) {
                if (key.equals(node.key)) {
                    node.value = value;
                    break;
                }
            }
        }

    }

    private void resize(int newLength) {
        Collection<Node>[] newBuckets = createTable(newLength);
        bucketNum = newLength;
        for (Collection<Node> bucket : buckets) {
            for (Node node : bucket) {
                int newIndex = Math.floorMod(node.key.hashCode(), newLength);
                newBuckets[newIndex].add(createNode(node.key, node.value));
            }
        }
        buckets = newBuckets;
    }

    /** Returns a Set view of the keys contained in this map. */
    @Override
    public Set<K> keySet() {
        return keySet;
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     * Not required for Lab 8. If you don't implement this, throw an
     * UnsupportedOperationException.
     */
    public V remove(K key) {
        if (!containsKey(key)) {
            return null;
        }
        V result = get(key);
        int bucketIndex = Math.floorMod(key.hashCode(), bucketNum);

        Iterator<Node> it = buckets[bucketIndex].iterator();
        while (it.hasNext()) {
            Node node = it.next();
            if (key.equals(node.key)) {
                it.remove();
                break;
            }
        }
        size--;
        keySet.remove(key);
        return result;
    }

    /**
     * Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 8. If you don't implement this,
     * throw an UnsupportedOperationException.
     */
    @Override
    public V remove(K key, V value) {
        if (!value.equals(get(key))) {
            return null;
        }
        return remove(key);
    }

     /** Returns an iterator over elements of type {@code T}. */
    @Override
    public Iterator<K> iterator() {
        return keySet.iterator();
    }
}
