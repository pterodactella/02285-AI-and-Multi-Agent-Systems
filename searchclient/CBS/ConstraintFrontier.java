package searchclient.CBS;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Stack;

import searchclient.Frontier;
import searchclient.Heuristic;


public interface ConstraintFrontier
{
    void add(ConstraintState state);
    ConstraintState pop();
    boolean isEmpty();
    int size();
    boolean contains(ConstraintState state);
    String getName();
}

class FrontierBFS
        implements ConstraintFrontier
{
    private final ArrayDeque<ConstraintState> queue = new ArrayDeque<>(65536);
    private final HashSet<ConstraintState> set = new HashSet<>(65536);

    @Override
    public void add(ConstraintState state)
    {
        this.queue.addLast(state);
        this.set.add(state);
    }

    @Override
    public ConstraintState pop()
    {
    	ConstraintState state = this.queue.pollFirst();
        this.set.remove(state);
        return state;
    }

    @Override
    public boolean isEmpty()
    {
        return this.queue.isEmpty();
    }

    @Override
    public int size()
    {
        return this.queue.size();
    }

    @Override
    public boolean contains(ConstraintState state)
    {
        return this.set.contains(state);
    }

    @Override
    public String getName()
    {
        return "breadth-first search";
    }
}

class FrontierDFS
        implements ConstraintFrontier
{
    private final  Stack<ConstraintState> stack = new Stack<>();
    private final  HashSet<ConstraintState> set = new HashSet<>(65536);
    
    @Override
    public void add(ConstraintState state)
    {
        this.stack.add(state);
        this.set.add(state);
    }

    @Override
    public ConstraintState pop()
    {
    	ConstraintState n = this.stack.pop();
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
    public boolean contains(ConstraintState state)
    {
    	return this.set.contains(state);
    }

    @Override
    public String getName()
    {
        return "depth-first search";
    }
}

class ConstraintFrontierBestFirst
        implements ConstraintFrontier
{
    private ConstraintHeuristic heuristic;
    private final  PriorityQueue<ConstraintState> priorityQueue;
    private final  HashSet<ConstraintState> set;

    public ConstraintFrontierBestFirst(ConstraintHeuristic h)
    {
        this.heuristic = h;
        this.priorityQueue = new PriorityQueue<ConstraintState>(this.heuristic);
        this.set = new HashSet<ConstraintState>(65536);
    }

    @Override
    public void add(ConstraintState state)
    {
        this.priorityQueue.add(state);
        this.set.add(state);
    }

    @Override
    public ConstraintState pop()
    {
    	ConstraintState state = this.priorityQueue.poll();
        this.set.remove(state);
        return state;
    }

    @Override
    public boolean isEmpty()
    {
    	return this.priorityQueue.isEmpty();
    }

    @Override
    public int size()
    {
    	return this.priorityQueue.size();
    }

    @Override
    public boolean contains(ConstraintState state)
    {
    	return this.set.contains(state);
    }

    @Override
    public String getName()
    {
        return String.format("best-first search using %s", this.heuristic.toString());
    }
}