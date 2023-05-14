package searchclient;

import java.util.Comparator;

public abstract class Heuristic
        implements Comparator<State>
{
    public Heuristic(State initialState)
    {
        // Here's a chance to pre-process the static parts of the level.
    }

    
	public int h(State s) {
		int combinedHeuristic = Integer.MAX_VALUE;

		// Find the nearest goal state based on the Manhattan distance
		for (int i = 0; i < s.goals.length; i++) {
			int heuristic = h(s, i); // Compute heuristic for each goal state
			System.err.println("Main class heuristic= " + heuristic);
			combinedHeuristic = Math.min(combinedHeuristic, heuristic); // Update with the minimum heuristic
			System.err.println("Main class Combined heuristic= " + combinedHeuristic);

		}

		return combinedHeuristic;
	}
    public int h(State s, int goalIndex)
    {
    	Distances d = new ManhattanDistance(s.agentRows, s.agentCols, s.goals, s.boxes);
//    	System.out.println("d=" );
//    	System.out.println(d.calculate());
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
