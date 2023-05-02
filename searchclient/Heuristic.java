package searchclient;

// import java.util.ArrayList;
import java.util.Comparator;

public abstract class Heuristic
        implements Comparator<State>
{
    public Heuristic(State initialState)
    {
        // Here's a chance to pre-process the static parts of the level.
    }

    public int h(CBSNode node) {
        return h(node.getState());
    }
    
    public int h(State s)
    {
    	Distances d = new ManhattanDistance(s.getAgents(), s.getGoals(), s.getBoxes());
   	// System.err.println("d= " + d.calculate());
        return d.calculate();
    }


    public abstract int f(State s);

    
    
    @Override
    public int compare(State s1, State s2)
    {
        return this.f(s1) - this.f(s2);
    }
}

class HeuristicAStar
        extends Heuristic
{
    public HeuristicAStar(State initialState)
    {
        super(initialState);
    }

    @Override
    public int f(State s)
    {
        return s.g() + this.h(s);
    }

    @Override
    public String toString()
    {
        return "A* evaluation";
    }
}

class HeuristicWeightedAStar
        extends Heuristic
{
    private int w;

    public HeuristicWeightedAStar(State initialState, int w)
    {
        super(initialState);
        this.w = w;
    }

    @Override
    public int f(State s)
    {
        return s.g() + this.w * this.h(s);
    }

    @Override
    public String toString()
    {
        return String.format("WA*(%d) evaluation", this.w);
    }
}

class HeuristicGreedy
        extends Heuristic
{
    public HeuristicGreedy(State initialState)
    {
        super(initialState);
    }

    @Override
    public int f(State s)
    {
        return this.h(s);
    }


    @Override
    public String toString()
    {
        return "greedy evaluation";
    }
}

class HeuristicMeta extends Heuristic {

    // private int[] pathLength;

    public HeuristicMeta(State initialState) {
        super(initialState);
    }

    @Override
    public int f(State s)
    {
        int total = 0;
        for (State expanded : s.getExpandedStates()) {
            total += this.h(expanded);
            if (this.h(expanded) < this.h(s) ) {
                return total - this.h(expanded);
            }
        }
        return total - this.h(s);
    }

    @Override public String toString() {
        return "HeuristicMeta";
    }

}

// }
