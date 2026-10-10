import java.util.*;


public class Tests {
    public static void main(String[] args) {
        Random rnd = new Random(1);
        for (int round = 0; round < 50; round++) {
            IntMap[] maps = { new BST(), new MyHashTable(), new MyHashTable(true) };
            Set<Integer> ref = new HashSet<>();
            for (int op = 0; op < 5000; op++) {
                int key = rnd.nextInt(500);
                int type = rnd.nextInt(3);
                if (type == 0) {
                    ref.add(key);
                    for (IntMap m : maps) m.put(key, key);
                } else if (type == 1) {
                    boolean expected = ref.contains(key);
                    for (IntMap m : maps) check(m.contains(key) == expected, "contains");
                } else {
                    boolean expected = ref.remove(key);
                    for (IntMap m : maps) check(m.remove(key) == expected, "remove");
                }
                for (IntMap m : maps) check(m.size() == ref.size(), "size");
            }
        }
        System.out.println("Все тесты пройдены");
    }

    static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError("Ошибка в: " + what);
    }
}
