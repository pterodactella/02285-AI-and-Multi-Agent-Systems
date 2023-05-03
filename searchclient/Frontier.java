package searchclient;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Stack;

public interface Frontier {
    void add(CBSNode node);
    CBSNode pop();
    boolean isEmpty();
    int size();
    boolean contains(CBSNode node);
    String getName();
}

class FrontierBFS implements Frontier {
    private final ArrayDeque<CBSNode> queue = new ArrayDeque<>(65536);
    private final HashSet<CBSNode> set = new HashSet<>(65536);

    @Override
    public void add(CBSNode node) {
        this.queue.addLast(node);
        this.set.add(node);
    }

    @Override
    public CBSNode pop() {
        CBSNode node = this.queue.pollFirst();
        this.set.remove(node);
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
        return this.set.contains(node);
    }

    @Override
    public String getName() {
        return "breadth-first search";
    }
}

class FrontierDFS
        implements Frontier
{
    private final  Stack<CBSNode> stack = new Stack<>();
    private final  HashSet<CBSNode> set = new HashSet<>(65536);
    
    @Override
    public void add(CBSNode state)
    {
        this.stack.add(state);
        this.set.add(state);
    }

    @Override
    public CBSNode pop()
    {
        CBSNode n = this.stack.pop();
        this.set.remove(n);
        return n;
    }

    @Override
    public boolean isEmpty()
    {
    	return this.stack.isEmpty();
    }

    @Override
    public int size()
    {
    	return this.stack.size();
    }

    @Override
    public boolean contains(CBSNode state)
    {
    	return this.set.contains(state);
    }

    @Override
    public String getName()
    {
        return "depth-first search";
    }
}
class FrontierBestFirst implements Frontier {
    private Heuristic heuristic;
    private final PriorityQueue<CBSNode> priorityQueue;
    private final HashSet<CBSNode> set;

    public FrontierBestFirst(Heuristic h) {
        this.heuristic = h;
        this.priorityQueue = new PriorityQueue<CBSNode>(this.heuristic);
        this.set = new HashSet<CBSNode>(65536);
    }

    @Override
    public void add(CBSNode node) {
        this.priorityQueue.add(node);
        this.set.add(node);
    }

    @Override
    public CBSNode pop() {
        CBSNode node = this.priorityQueue.poll();
        this.set.remove(node);
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
        return this.set.contains(node);
    }

    @Override
    public String getName() {
        return String.format("best-first search using %s", this.heuristic.toString());
    }
}
