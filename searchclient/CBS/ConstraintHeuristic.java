

package searchclient.CBS;

import java.util.Comparator;

import searchclient.Distances;

import searchclient.ManhattanDistance;

public abstract class ConstraintHeuristic implements Comparator<ConstraintState> {
    public ConstraintHeuristic() {
        // Here's a chance to pre-process the static parts of the level.
    }
	public int h(ConstraintState s)
    {
    	Distances d = new ManhattanDistance(s.agentRows, s.agentCols, s.goals, s.boxes);
   	// System.out.println("d=" );
   	// System.out.println(d.calculate());
        return d.calculate();
    }


    public int calculateConstraintPenalty(ConstraintState s) {
        // Calculate the penalty based on the number of constraints in the state
        // You can define your own function to determine the penalty value

        int numConstraints = s.constraints.size();
        int penalty = numConstraints * 10; // Adjust the penalty factor as needed

        return penalty;
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
    private ConstraintState initialState;

    public ConstraintHeuristicAStar(ConstraintState initialState) {
        super();
        this.initialState = initialState;
    }

    @Override
    public int f(ConstraintState s) {
        return s.g() + this.h(s);
    }

    @Override
    public int h(ConstraintState s) {
        int combinedHeuristic = Integer.MAX_VALUE;

        if (s.goalIndex == null) {
            // System.err.println("goal index is null");
            Distances d = new ManhattanDistance(s.agentRows, s.agentCols, s.goals, s.boxes);
            return d.calculate();
        }
        // Find the closest box to the goal state
        int closestBoxHeuristic = Integer.MAX_VALUE;
        for (int[] goalIndex : s.goalIndex) {
            int goalRow = goalIndex[0];
            int goalCol = goalIndex[1];
            int closestBoxDistance = Integer.MAX_VALUE;

            // If there are boxes
            if (s.boxes.length > 0) {
                for (int i = 0; i < s.boxes.length; i++) {
                    int boxRow = s.boxes[i][0];
                    int boxCol = s.boxes[i][1];
                    int boxToGoalDistance = Math.abs(boxRow - goalRow) + Math.abs(boxCol - goalCol);
                    closestBoxDistance = Math.min(closestBoxDistance, boxToGoalDistance);
                }
            }
            // If there are no boxes, calculate the distance between the agent and the goal
            else {
                int agentRow = s.agentRows[s.agent];
                int agentCol = s.agentCols[s.agent];
                closestBoxDistance = Math.abs(agentRow - goalRow) + Math.abs(agentCol - goalCol);
            }
            closestBoxHeuristic = Math.min(closestBoxHeuristic, closestBoxDistance);
        }

        // Apply a penalty based on the number of constraints
        int constraintPenalty = calculateConstraintPenalty(s);
        combinedHeuristic = closestBoxHeuristic + constraintPenalty;

        return combinedHeuristic;
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

