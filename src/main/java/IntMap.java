
public interface IntMap {
    void put(int key, int value);
    boolean contains(int key);
    boolean remove(int key);
    int size();
}