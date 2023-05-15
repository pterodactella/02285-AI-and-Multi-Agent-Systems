package searchclient.CBS;

import java.util.Comparator;

import searchclient.Distances;

import searchclient.ManhattanDistance;

public abstract class ConstraintHeuristic implements Comparator<ConstraintState> {
	public ConstraintHeuristic() {
// Here's a chance to pre-process the static parts of the level.
	}


	public int h(ConstraintState s) {
		// Compute the heuristic for the specific goal state using Manhattan distance or
		// any other suitable method
		// Implement your own heuristic calculation here

		Distances d = new ManhattanDistance(s.agentRows, s.agentCols, s.goals, s.boxes);
		int heuristic = d.calculate();

		return heuristic;
	}

	public abstract int f(ConstraintState s);

	@Override
	public int compare(ConstraintState s1, ConstraintState s2) {
		int f1 = this.f(s1);
		int f2 = this.f(s2);

		if (f1 < f2) {
			return -1;
		} else if (f1 > f2) {
			return 1;
		} else {
			return 0;
		}
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

	@Override
	public int compare(ConstraintState s1, ConstraintState s2) {
		int f1 = this.f(s1);
		int f2 = this.f(s2);

		if (f1 < f2) {
			return -1;
		} else if (f1 > f2) {
			return 1;
		} else {
			return 0;
		}
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
