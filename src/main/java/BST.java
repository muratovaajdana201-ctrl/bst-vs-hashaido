
public class BST implements IntMap {

    private static class Node {
        int key, value;
        Node left, right;
        Node(int key, int value) { this.key = key; this.value = value; }
    }

    private Node root;
    private int size;

    @Override
    public void put(int key, int value) {
        if (root == null) { root = new Node(key, value); size++; return; }
        Node cur = root;
        while (true) {
            if (key == cur.key) { cur.value = value; return; }      // ключ уже есть
            if (key < cur.key) {
                if (cur.left == null) { cur.left = new Node(key, value); size++; return; }
                cur = cur.left;
            } else {
                if (cur.right == null) { cur.right = new Node(key, value); size++; return; }
                cur = cur.right;
            }
        }
    }

    @Override
    public boolean contains(int key) {
        Node cur = root;
        while (cur != null) {
            if (key == cur.key) return true;
            cur = (key < cur.key) ? cur.left : cur.right;
        }
        return false;
    }

    @Override
    public boolean remove(int key) {

        Node parent = null, cur = root;
        while (cur != null && cur.key != key) {
            parent = cur;
            cur = (key < cur.key) ? cur.left : cur.right;
        }
        if (cur == null) return false;

        if (cur.left != null && cur.right != null) {
            Node succParent = cur, succ = cur.right;
            while (succ.left != null) { succParent = succ; succ = succ.left; }
            cur.key = succ.key;
            cur.value = succ.value;
            parent = succParent;
            cur = succ;
        }


        Node child = (cur.left != null) ? cur.left : cur.right;
        if (parent == null) root = child;
        else if (parent.left == cur) parent.left = child;
        else parent.right = child;
        size--;
        return true;
    }

    @Override
    public int size() { return size; }


    public int height() {
        if (root == null) return 0;
        java.util.ArrayDeque<Node> q = new java.util.ArrayDeque<>();
        q.add(root);
        int h = 0;
        while (!q.isEmpty()) {
            for (int i = q.size(); i > 0; i--) {
                Node n = q.poll();
                if (n.left != null) q.add(n.left);
                if (n.right != null) q.add(n.right);
            }
            h++;
        }
        return h;
    }
}