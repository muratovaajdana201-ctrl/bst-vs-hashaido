import java.io.*;
import java.util.*;
import java.util.function.Supplier;

public class Benchmark {

    static final int[] SIZES = {100, 1_000, 10_000};
    static final int WARMUP = 300, RUNS = 51;
    static volatile int sink;

    static long[] runOnce(Supplier<IntMap> factory, int[] keys) {
        IntMap m = factory.get();

        long t0 = System.nanoTime();
        for (int k : keys) m.put(k, k);
        long t1 = System.nanoTime();

        int hits = 0;
        for (int k : keys) {
            if (m.contains(k)) hits++;
        }
        long t2 = System.nanoTime();


        for (int i = keys.length - 1; i >= 0; i--) {
            m.remove(keys[i]);
        }
        long t3 = System.nanoTime();

        sink = hits;

        return new long[]{t1 - t0, t2 - t1, t3 - t2};
    }


    static double[] measure(Supplier<IntMap> factory, int[] keys) {

        for (int i = 0; i < WARMUP; i++) {
            runOnce(factory, keys);
        }

        long[][] res = new long[3][RUNS];

        for (int r = 0; r < RUNS; r++) {
            long[] t = runOnce(factory, keys);

            for (int op = 0; op < 3; op++) {
                res[op][r] = t[op];
            }
        }

        double[] med = new double[3];

        for (int op = 0; op < 3; op++) {
            Arrays.sort(res[op]);
            med[op] = res[op][RUNS / 2] / 1000.0;
        }

        return med;
    }

    public static void main(String[] args) throws IOException {
        Map<String, Supplier<IntMap>> structures = new LinkedHashMap<>();

        structures.put("BST", BST::new);
        structures.put("HashTable", MyHashTable::new);
        structures.put("HashTable(badHash)", () -> new MyHashTable(true));

        String[] scenarios = {"random", "sorted"};

        try (PrintWriter csv = new PrintWriter("results.csv")) {
            csv.println("structure,scenario,n,insert_us,search_us,delete_us");

            System.out.printf("%-20s %-8s %7s %12s %12s %12s%n",
                    "structure", "scenario", "n",
                    "insert(us)", "search(us)", "delete(us)");

            for (int n : SIZES) {
                for (String sc : scenarios) {
                    int[] keys = new int[n];

                    for (int i = 0; i < n; i++) {
                        keys[i] = i;
                    }

                    if (sc.equals("random")) {
                        shuffle(keys, new Random(42));
                    }

                    for (var e : structures.entrySet()) {
                        double[] t = measure(e.getValue(), keys);

                        System.out.printf(
                                "%-20s %-8s %7d %12.1f %12.1f %12.1f%n",
                                e.getKey(), sc, n, t[0], t[1], t[2]
                        );

                        csv.printf(
                                Locale.US, "%s,%s,%d,%.1f,%.1f,%.1f%n",
                                e.getKey(), sc, n, t[0], t[1], t[2]
                        );
                    }
                }
            }
        }


        System.out.println("\nStructural metrics (n = 10000):");

        int n = 10_000;
        int[] rnd = new int[n];
        int[] srt = new int[n];

        for (int i = 0; i < n; i++) {
            rnd[i] = i;
            srt[i] = i;
        }

        shuffle(rnd, new Random(42));

        BST b1 = new BST();
        for (int k : rnd) {
            b1.put(k, k);
        }

        BST b2 = new BST();
        for (int k : srt) {
            b2.put(k, k);
        }

        MyHashTable h1 = new MyHashTable();
        for (int k : rnd) {
            h1.put(k, k);
        }

        MyHashTable h2 = new MyHashTable(true);
        for (int k : rnd) {
            h2.put(k, k);
        }

        System.out.println(
                "BST random  : height = " + b1.height()
                        + "  (log2 n ≈ 13.3)"
        );

        System.out.println(
                "BST sorted  : height = " + b2.height()
                        + "  (degenerates into a linked list)"
        );

        System.out.println(
                "Hash normal : capacity = " + h1.capacity()
                        + ", longest chain = " + h1.longestChain()
        );

        System.out.println(
                "Hash badHash: longest chain = " + h2.longestChain()
        );
    }

    static void shuffle(int[] a, Random r) {
        for (int i = a.length - 1; i > 0; i--) {
            int j = r.nextInt(i + 1);

            int t = a[i];
            a[i] = a[j];
            a[j] = t;
        }
    }
}