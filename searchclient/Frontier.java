package searchclient;

import java.util.ArrayDeque;
// import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Stack;

public interface Frontier {
    void add(CBSNode node);

    CBSNode pop();

    boolean isEmpty();

    int size();

    boolean contains(CBSNode node);

    CBSNode getNode(CBSNode node); // New method to get node by state

    void remove(CBSNode node);

    String getName();
}

class FrontierBFS implements Frontier {
    private final ArrayDeque<CBSNode> queue = new ArrayDeque<>(65536);
    private final HashSet<State> set = new HashSet<>(65536);

    @Override
    public void add(CBSNode node) {
        System.out.println("Adding state to BFS queue: " + node.getState());
        System.out.println("Queue size: " + this.queue.size());

        this.queue.addLast(node);
        this.set.add(node.getState());
    }

    @Override
    public CBSNode pop() {
        CBSNode node = this.queue.pollFirst();
        System.out.println("Removing state from BFS queue: " + node.getState());
        System.out.println("Queue size: " + this.queue.size());

        
        this.set.remove(node.getState());
        return node;
    }

    @Override
    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    @Override
    public int size() {
        return this.queue.size();
    }

    @Override
    public boolean contains(CBSNode node) {
        return this.set.contains(node.getState());
    }

    @Override
    public String getName() {
        return "breadth-first search";
    }

    @Override
    public CBSNode getNode(CBSNode state) {
        for (CBSNode node : this.queue) {
            if (node.equals(state)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public void remove(CBSNode node) {
        this.queue.remove(node);
        this.set.remove(node.getState());
    }
}

class FrontierDFS implements Frontier {
    private final Stack<CBSNode> stack = new Stack<>();
    private final HashSet<State> set = new HashSet<>(65536);

    @Override
    public void add(CBSNode node) {
        this.stack.add(node);
        this.set.add(node.getState());
    }

    @Override
    public CBSNode pop() {
        CBSNode node = this.stack.pop();
        this.set.remove(node.getState());
        return node;
    }

    @Override
    public boolean isEmpty() {
        return this.stack.isEmpty();
    }

    @Override
    public int size() {
        return this.stack.size();
    }

    @Override
    public boolean contains(CBSNode node) {
        return this.set.contains(node.getState());
    }

    @Override
    public String getName() {
        return "depth-first search";
    }

    @Override
    public CBSNode getNode(CBSNode state) {
        for (CBSNode node : this.stack) {
            if (node.equals(state)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public void remove(CBSNode node) {
        this.stack.remove(node);
        this.set.remove(node.getState());
    }
}

class FrontierBestFirst implements Frontier {
    private Heuristic heuristic;
    private final PriorityQueue<CBSNode> priorityQueue;
    private final HashSet<State> set;
    
    public FrontierBestFirst(Heuristic h) {
        this.heuristic = h;
        this.priorityQueue = new PriorityQueue<CBSNode>((CBSNode n1, CBSNode n2) -> {
            int f1 = n1.getState().g() + this.heuristic.h(n1);
            int f2 = n2.getState().g() + this.heuristic.h(n2);
            return Integer.compare(f1, f2);
        });
        this.set = new HashSet<>(65536);
    }
    
    

    @Override
    public void add(CBSNode node) {
        this.priorityQueue.add(node);
        this.set.add(node.state);
    }

    @Override
    public CBSNode pop() {
        CBSNode node = this.priorityQueue.poll();
        this.set.remove(node.getState());
        return node;
    }

    @Override
    public boolean isEmpty() {
        return this.priorityQueue.isEmpty();
    }

    @Override
    public int size() {
        return this.priorityQueue.size();
    }

    @Override
    public boolean contains(CBSNode node) {
        return this.set.contains(node.getState());
    }

    @Override
    public String getName() {
        return "best-first search";
    }

    @Override
    public CBSNode getNode(CBSNode state) {
        for (CBSNode node : this.priorityQueue) {
            if (node.equals(state)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public void remove(CBSNode node) {
        this.priorityQueue.remove(node);
        this.set.remove(node.getState());
    }
}