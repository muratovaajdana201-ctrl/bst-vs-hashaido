# BST vs Hash Table
Aidana  Muratova 
SE-2539

## What I did

I wrote a binary search tree and a hash table in Java without using the standard collections. Then I tested that they work correctly, measured insert, search and delete on 100, 1,000 and 10,000 elements, and compared the times with the theory from the lectures.

## Files

- src/main/java/
- IntMap.java 
- BST.java 
- MyHashTable.java 
- Tests.java 
- Benchmark.java

- results.csv 

## How to run

Run Tests.main() first, it should print that all tests passed. Then run Benchmark.main(). It prints the results and creates results.csv in the project folder.

## How the code works

1)BST. It is a normal tree with no balancing. Smaller keys go to the left, bigger keys go to the right. Insert and search walk down from the root. For delete there are three cases: the node is a leaf (just remove it), the node has one child (the child takes its place), or the node has two children. In the last case I copy the smallest key from the right subtree into the node and then remove that smallest node instead. I used loops and not recursion, because with sorted input the tree gets 10,000 levels deep and recursion could give a StackOverflowError.

2)Hash table. It uses chaining: an array of buckets and each bucket is a linked list. The array size is a power of two and the index is mix(key) & (size - 1). I mix the bits first and then apply the mask, because using the raw key would only use the low bits. When the number of elements is bigger than 0.75 of the array size, the array is doubled and all nodes are moved to new buckets. I also added a "bad hash" mode where every key goes to bucket 0. It is only for the experiment, to see the worst case.

3)Tests. Tests.java does 50 rounds of 5,000 random put, contains and remove operations and compares both structures with Java's HashSet after each step. Everything passed.

## Complexity

Here n is the number of elements and h is the height of the tree.

For the BST, every operation costs about h steps, because we walk one path from the root down.
Insert, search and delete have a best-case time complexity of O(1). For example, searching for the root takes constant time, and inserting into an empty tree also takes constant time. The actual time depends on the tree structure and the position of the key.

Average case: O(log n), if the keys come in random order and the tree stays more or less balanced.

 Worst case: O(n). If the keys come sorted, every new key goes to the right and the tree becomes a linked list with height n.

For the hash table, an operation costs one index calculation plus walking along one chain.

 Insert, search and delete, best case: O(1).

 Average case: O(1). With a good hash function the chains are short, and resize keeps the load factor under 0.75. Insert is O(1) amortized, because one resize costs O(n) but happens rarely since the array doubles each time.

Worst case: O(n), when many keys land in the same bucket and the chain is as long as the whole table. Insert also checks for duplicates by walking the chain, so insert becomes O(n) too.

Both structures use O(n) memory.

## Experiments

For each size n (100, 1,000, 10,000) I used keys 0 to n-1 in two orders: random (shuffled with seed 42) and sorted. I measured three phases, each for all n elements one after another: insert all, search all, delete all. Delete goes in reverse order of insertion. If I deleted sorted keys in the same order, the BST would always remove the root with no left child and look fast, which is not fair.

Every measurement starts with an empty structure. I performed 300 warm-up runs to give the Java JIT compiler time to optimize the code. Then I performed 51 measured runs and took the median time. The times are in microseconds for the whole phase, not for one operation. I tested three versions: the BST, the hash table, and the hash table with the bad hash.

### Results for n = 10,000

Random keys. BST: insert 1,283 µs, search 805 µs, delete 912 µs. Hash table: insert 535 µs, search 131 µs, delete 138 µs.

Sorted keys. BST: insert 141,072 µs, search 165,958 µs, delete 164,350 µs. Hash table: insert 537 µs, search 128 µs, delete 138 µs.

Hash table with the bad hash (random keys): insert 121,449 µs, search 112,357 µs, delete 33,586 µs. Sorted keys gave almost the same numbers.

Structure at n = 10,000: the BST height was 30 for random keys and 10,000 for sorted keys. The hash table had 16,384 buckets and the longest chain was 6. With the bad hash the longest chain was 10,000.

### Results for the smaller sizes

At n = 1,000 the BST with random keys took 40, 49 and 49 µs for insert, search and delete. With sorted keys it took 1,360, 1,629 and 1,630 µs. The normal hash table with sorted keys took 24, 4 and 7 µs. The bad hash table took about 1,200, 1,100 and 525 µs.

At n = 100 everything was between 2 and about 25 µs, and the differences between structures were small.

All the numbers are in results.csv.

## What I noticed

When n grew from 1,000 to 10,000 (10 times more), the sorted BST became about 100 times slower. For example, insert went from 1,360 to 141,072 µs. That is what O(n squared) looks like: n operations and each one costs O(n). The hash table with the bad hash also became about 100 times slower for insert (1,228 to 121,449 µs). The growth was different for each structure and operation. The sorted BST and the hash table with the bad hash function showed a much larger increase in time than the normal hash table. Small measurements were less stable, so I focused more on the general trend than on tiny differences.

At n = 10,000 the sorted BST was about 110 times slower than the random BST for insert, and about 200 times slower for search. It is the same code and the same keys, only the order is different.

The hash table did not care about the order of keys. Insert was 535 µs for random keys and 537 µs for sorted keys.

On random data the hash table was faster than the BST: about 2.4 times for insert and about 6 times for search and delete. Dividing the total search time by 10,000 gives an estimated average of about 13 ns per search for the normal hash table and about 81 ns for the random BST. These values are estimates based on the total time, not separate measurements of individual searches.

Some results look strange, for example at n = 1,000 the hash table insert with random keys was 94 µs and with sorted keys 24 µs, although the work is the same. I think this is noise from the JIT compiler and the garbage collector. Also the BST random insert at n = 100 was 9.1 µs in this run and 2.4 µs in a run I did before. Because of this I do not trust small differences at n = 100.

In the bad hash table delete was cheaper than insert and search. I think it is because new nodes go to the front of the chain and I delete in reverse order, so keys are found earlier than the average. I did not check this, so it is only my guess. It is still much slower than the normal table.

## Why the performance is different in practice

- The shape of the BST depends on the input order. A tree without balancing has no protection against sorted data, which is very common (ids, dates). AVL or Red-Black trees fix this and keep the height O(log n).
- The hash table is only fast if the hash function spreads the keys well. With a bad function it is just a linked list.
- The constants are different. A hash operation looks at one or two nodes. A BST follows many pointers and the nodes are in different places in memory, so the CPU cache helps less.
- Resize makes some single inserts slow, but on average the cost is small.
- Measuring time in Java is noisy because of the JIT and the garbage collector, so I used warm-up and the median.
- The BST can do things the hash table cannot: sorted order, minimum and maximum, ranges. So the faster structure is not always the better choice.

## Conclusion

After this project I understand much better why people say "hash table is O(1)" and "BST is O(log n)", and also when these sentences are not true.

The normal hash table was the fastest structure in my large tests. However, the version with the bad hash function was much slower because all keys were stored in one bucket. At 10,000 elements it did a search in about 13 ns, and the BST needed about 80 ns on random data. It also did not care if the keys were sorted or random, because the mix step breaks any pattern. But the fast speed depends on two things: a good hash function and a table that is not too full. When I made all keys go to one bucket, the same hash table became about 100 times slower when n grew 10 times, so it behaved like a linked list.

The BST without balancing is good only when the keys come in a random order. With random keys its height was 30 for 10,000 elements and the times were fine. With sorted keys the height was 10,000 and the tree was just a linked list, so one search took about 16 µs instead of 80 ns. This was the biggest surprise for me, because the code is exactly the same and only the order of the keys is different. In real programs sorted input is very common (ids, dates), so a plain BST is risky. A balanced tree like AVL or Red-Black would solve this problem, and I think it is the next step if I wanted to improve this project.

So my answer to the question "which one is better" is: it depends on what I need. If I only need to find, add and remove by key, I would choose a hash table. If I need the keys in sorted order, the minimum, or a range of keys, I would choose a balanced BST, because a hash table cannot do this. And I would never trust big-O alone: the measurements showed that constants, memory and the input order change the real time a lot.

About the work itself: the trickiest parts of the code were the delete in the BST and the resize in the hash table. In the BST delete there are three cases, and the case with two children is the hardest, because I have to find the smallest node in the right subtree and also fix the link of its parent. In the resize I must save the next node before I move the current one to the new array, otherwise the rest of the chain is lost. The tests with HashSet were useful to check that these two parts work. If I did this project again, I would test more sizes than three and add a balanced tree to the comparison.



## Limitations

I tested on one computer, so only the comparison between structures and the growth are important, not the exact times. The keys are int. I used only three sizes, so the growth is estimated from two jumps of ten times. The sorted case is the extreme worst case for the BST, real data is usually somewhere between sorted and random.
