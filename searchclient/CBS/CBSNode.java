package searchclient.CBS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

import searchclient.Action;
import searchclient.Frontier;
import searchclient.GraphSearch;
import searchclient.Heuristic;
import searchclient.State;

public class CBSNode {
	public State state;
	public ArrayList<Constraint> constraints;
	public PlanStep[][] solution;
	public int cost;
	private int longestPath;

	public CBSNode(State state) {
		this.state = state;
		this.constraints = new ArrayList<>();
		this.solution = null;
		this.cost = 0;
	}

	public Conflict findFirstConflict() {
    for (int i = 0; i < this.longestPath; i++) {
        for (int j = 0; j < this.solution.length; j++) {
            for (int k = j; k < this.solution.length; k++) {
                PlanStep step1 = (i < this.solution[j].length) ? this.solution[j][i]
                        : this.solution[j][this.solution[j].length - 1];
                PlanStep step2 = (i < this.solution[k].length) ? this.solution[k][i]
                        : this.solution[k][this.solution[k].length - 1];

                if (step1.action != Action.NoOp && step2.action != Action.NoOp
                        && (step1.action.agentRowDelta != 0 || step1.action.agentColDelta != 0 ||
                                step2.action.agentRowDelta != 0 || step2.action.agentColDelta != 0)) {
                    int agent1Row = this.state.agentRows[j];
                    int agent1Col = this.state.agentCols[j];
                    int newRow1 = agent1Row + step1.action.agentRowDelta;
                    int newCol1 = agent1Col + step1.action.agentColDelta;

                    int agent2Row = this.state.agentRows[k];
                    int agent2Col = this.state.agentCols[k];
                    int newRow2 = agent2Row + step2.action.agentRowDelta;
                    int newCol2 = agent2Col + step2.action.agentColDelta;

                    // Check if agents are occupying each other's goal positions
                    boolean agent1AtAgent2Goal = this.state.goals[newRow1][newCol1] == state.boxes[agent2Row][agent2Col];
                    boolean agent2AtAgent1Goal = this.state.goals[newRow2][newCol2] == state.boxes[agent1Row][agent1Col];

                    if (agent1AtAgent2Goal || agent2AtAgent1Goal) {
                        // Conflict found: agents are blocking each other's goal positions
                        return new Conflict(j, k, newRow1, newCol1, i);
                    }
                }
            }
        }
    }
    return null;
}


	public State createStateForAgent(int agentIndex) {
		// TODO: change goals for one agent only
		return this.state;
	}

	public PlanStep[][] findPlan() {
		int numberOfAgents = state.agentRows.length;
		PlanStep[][] individualPlans = new PlanStep[numberOfAgents][];
		// System.err.println("NUMBER OF AGENTS" + numberOfAgents);

		for (int i = 0; i < numberOfAgents; i++) {

			// System.err.println("THE STATE for " + i + " IS: \n" + state.toString());
			ConstraintState constraintState = new ConstraintState(state, i, this.constraints, 0);
			//			State stateForAgent = createStateForAgent(i);
			ConstraintFrontier frontier = new ConstraintFrontierBestFirst(new ConstraintHeuristicAStar(constraintState));
			PlanStep[] plan = ConstraintGraphSearch.search(this, frontier, i);
			if (plan.length > this.longestPath) {
				this.longestPath = plan.length;
			}
			individualPlans[i] = plan;
			// System.out.println("THE PLAN FOR: " + i);
			for(PlanStep step: plan) {
			System.out.println("Step: " + step.toString());
			}
			// TODO: Add search with constraint
		}		
		PlanStep.mergePlans(individualPlans);
		return individualPlans;
	}

	public int sumCosts() {
		int sum = 0;
		for (int i = 0; i < this.solution.length; i++) {
			sum += this.solution[i].length;
		}
		return sum;
	}
}

class Conflict {
	public int[] agentIndexes;
	public int locationX;
	public int locationY;
	public int timestamp;

	// TODO:Include boxes
	public Conflict(int agentAIndex, int agentBIndex, int locationX, int locationY, int timestamp) {
		this.agentIndexes = new int[2];
		this.agentIndexes[0] = agentAIndex;
		this.agentIndexes[1] = agentBIndex;
		this.locationX = locationX;
		this.locationY = locationY;
		this.timestamp = timestamp;

	}

}
