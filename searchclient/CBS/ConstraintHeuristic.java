package searchclient.CBS;

import java.util.Comparator;

import searchclient.Distances;
import searchclient.EnhancedHammingDistance;
import searchclient.ManhattanDistance;


public abstract class ConstraintHeuristic implements Comparator<ConstraintState> {
	public ConstraintHeuristic() {
// Here's a chance to pre-process the static parts of the level.
	}

	public int h(ConstraintState s) {
		Distances d = new ManhattanDistance(s.agentRows, s.agentCols, s.goals, s.boxes);
//System.out.println("d=" );
//System.err.println(d.calculate());
		return d.calculate();
	}

	public abstract int f(ConstraintState s);

	@Override
	public int compare(ConstraintState s1, ConstraintState s2) {
		return this.f(s1) - this.f(s2);
	}
}

class ConstraintHeuristicAStar extends ConstraintHeuristic {
	public ConstraintHeuristicAStar() {
		super();
	}

	@Override
	public int f(ConstraintState s) {
		return s.g() + this.h(s);
	}

	@Override
	public String toString() {
		return "A* evaluation";
	}
}

class ConstraintHeuristicWeightedAStar extends ConstraintHeuristic {
	private int w;

	public ConstraintHeuristicWeightedAStar(ConstraintState initialState, int w) {
		super();
		this.w = w;
	}

	@Override
	public int f(ConstraintState s) {
		return s.g() + this.w * this.h(s);
	}

	@Override
	public String toString() {
		return String.format("WA*(%d) evaluation", this.w);
	}
}

class ConstraintHeuristicGreedy extends ConstraintHeuristic {
	public ConstraintHeuristicGreedy(ConstraintState initialState) {
		super();
	}

	@Override
	public int f(ConstraintState s) {
		return this.h(s);
	}

	@Override
	public String toString() {
		return "greedy evaluation";
	}
}
