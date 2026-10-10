
public class MyHashTable implements IntMap {

    private static class Node {
        final int key;
        int value;
        Node next;
        Node(int key, int value, Node next) { this.key = key; this.value = value; this.next = next; }
    }

    private static final double MAX_LOAD = 0.75;

    private Node[] table = new Node[16];
    private int size;
    private final boolean badHash;

    public MyHashTable() { this(false); }

    public MyHashTable(boolean badHash) { this.badHash = badHash; }


    private int index(int key, int length) {
        if (badHash) return 0;
        int h = key;
        h ^= (h >>> 16);
        h *= 0x85ebca6b;
        h ^= (h >>> 13);
        h *= 0xc2b2ae35;
        h ^= (h >>> 16);
        return h & (length - 1);
    }

    @Override
    public void put(int key, int value) {
        int i = index(key, table.length);
        for (Node n = table[i]; n != null; n = n.next) {
            if (n.key == key) { n.value = value; return; }
        }
        table[i] = new Node(key, value, table[i]);   // вставка в голову цепочки: O(1)
        size++;
        if (size > MAX_LOAD * table.length) resize();
    }

    @Override
    public boolean contains(int key) {
        for (Node n = table[index(key, table.length)]; n != null; n = n.next) {
            if (n.key == key) return true;
        }
        return false;
    }

    @Override
    public boolean remove(int key) {
        int i = index(key, table.length);
        Node prev = null;
        for (Node n = table[i]; n != null; prev = n, n = n.next) {
            if (n.key == key) {
                if (prev == null) table[i] = n.next; else prev.next = n.next;
                size--;
                return true;
            }
        }
        return false;
    }


    private void resize() {
        Node[] old = table;
        Node[] bigger = new Node[old.length * 2];
        for (Node head : old) {
            for (Node n = head; n != null; ) {
                Node next = n.next;                  // сохраняем ДО перепривязки
                int j = index(n.key, bigger.length);
                n.next = bigger[j];
                bigger[j] = n;
                n = next;
            }
        }
        table = bigger;
    }

    @Override
    public int size() { return size; }


    public int longestChain() {
        int max = 0;
        for (Node head : table) {
            int len = 0;
            for (Node n = head; n != null; n = n.next) len++;
            max = Math.max(max, len);
        }
        return max;
    }

    public int capacity() { return table.length; }
}
