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

//		for (int i = 0; i < this.longestPath; i++) {
//			for (int j = 0; j < this.solution.length; j++) {
//				for (int k = j; k < this.solution.length; k++) {
////					if(this.solution[i])
//				}
//			}

//		}
		// TODO: findConflct
		return new Conflict(0, 0, 0, 0, 0);

	}

	public State createStateForAgent(int agentIndex) {
		// TODO: change goals for one agent only
		return this.state;
	}

	public PlanStep[][] findPlan() {
		int numberOfAgents = state.agentRows.length;
		PlanStep[][] individualPlans = new PlanStep[numberOfAgents][];
		System.err.println("NUMBER OF AGENTS" + numberOfAgents);

		for (int i = 0; i < numberOfAgents; i++) {

			System.err.println("THE STATE for " + i + " IS: \n" + state.toString());
			ConstraintState constraintState = new ConstraintState(state, i, this.constraints, 0);
//			State stateForAgent = createStateForAgent(i);
			ConstraintFrontier frontier = new ConstraintFrontierBestFirst(new ConstraintHeuristicAStar(constraintState));
			PlanStep[] plan = ConstraintGraphSearch.search(this, frontier, i);
			if (plan.length > this.longestPath) {
				this.longestPath = plan.length;
			}
			individualPlans[i] = plan;
			System.out.println("THE PLAN FOR: " + i);
			for(PlanStep step: plan) {
			System.out.println("Step: " + step.toString());
			}
			// TODO: Add search with constraint

		}

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
