package searchclient.CBS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

import searchclient.Action;
import searchclient.ActionType;
import searchclient.Color;
import searchclient.Frontier;
import searchclient.GraphSearch;
import searchclient.Heuristic;
import searchclient.Logger;
import searchclient.State;
import searchclient.CBS.Conflicts.AgentAlongBoxConflict;
import searchclient.CBS.Conflicts.AgentInBoxConflict;
import searchclient.CBS.Conflicts.BoxConflict;
import searchclient.CBS.Conflicts.BoxOrderedConflict;
import searchclient.CBS.Conflicts.Conflict;
import searchclient.CBS.Conflicts.GenericConflict;
import searchclient.CBS.Conflicts.OrderedConflict;
import searchclient.CBS.Conflicts.SymmetryConflict;
import searchclient.CBS.Conflicts.SymmetryType;

public class CBSNode {
	public State state;
	public HashSet<Constraint> constraints;
	public HashSet<BoxConstraint> boxConstraints;
	public PlanStep[][] solution;
	public int[] costs;
	private int longestPath;
	public int totalCost;
	private int hash = 0;

	public CBSNode(State state) {
		this.state = state;
		this.constraints = new HashSet<>();
		this.boxConstraints = new HashSet<>();
		this.solution = null;
		this.costs = new int[state.agentRows.length];
		this.totalCost = 0;
	}

	public CBSNode(CBSNode parent) {
		this.state = parent.state;
		this.constraints = new HashSet<>();
		for (Constraint constr : parent.constraints) {
			this.constraints.add(new Constraint(constr));
		}
		this.boxConstraints = new HashSet<>();
		for (BoxConstraint boxConstr : parent.boxConstraints) {
			this.boxConstraints.add(new BoxConstraint(boxConstr));
		}

		this.solution = new PlanStep[parent.solution.length][];
		for (int i = 0; i < parent.solution.length; i++) {
			this.solution[i] = new PlanStep[parent.solution[i].length];
			for (int j = 0; j < parent.solution[i].length; j++) {
				this.solution[i][j] = new PlanStep(parent.solution[i][j]);
			}
		}

		this.costs = parent.costs.clone();
		this.totalCost = 0;
	}

	// public metaAgentConstructor findMetaAgentConstructor() {

	// }

	public GenericConflict findFirstConflict() {

		State temporaryState = new State(this.state);
		int[] agentsPositions = null;
		for (int i = 1; i <= this.longestPath; i++) {

			for (int j = 0; j < this.solution[i].length; j++) {
				if (this.solution[i][j].locationX == -1)
					continue;
				// //
				// if(this.solution[i][j].movingBox == null &&
				// temporaryState.isBoxAt(this.solution[i][j].locationY,
				// this.solution[i][j].locationX) ||
				// (this.solution[i][j].movingBox != null && this.solution[i][j].locationX !=
				// this.solution[i][j].movingBox.prevX && this.solution[i][j].locationY !=
				// this.solution[i][j].movingBox.prevY &&
				// temporaryState.isBoxAt(this.solution[i][j].locationY,
				// this.solution[i][j].locationX))) {
				// return new AgentInBoxConflict(j, this.solution[i][j].locationX,
				// this.solution[i][j].locationY, i);
				// }

				// if(this.solution[i][j].movingBox != null &&
				// !this.solution[i][j].action.equals(Action.NoOp) &&
				// temporaryState.isBoxAt(this.solution[i][j].movingBox.currY,
				// this.solution[i][j].movingBox.currX)) {
				// System.err.println("Found standing box conflict at position: " +
				// this.solution[i][j].movingBox.currY + "; " +
				// this.solution[i][j].movingBox.currX);
				// System.err.println("Actions at time: " + i + ": Agent 0 = " +
				// this.solution[i][0].action.toString() + "; Action 1: " +
				// this.solution[i][1]);
				// System.err.println(temporaryState.toString());
				// return new BoxOrderedConflict(-1, j, this.solution[i][j].movingBox.currX,
				// this.solution[i][j].movingBox.currY, i);
				// }

				for (int k = j + 1; k < this.solution[i].length; k++) {
					if (this.solution[i][k].locationX == -1)
						continue;
					agentsPositions = new int[] {
							/* [0] : */ this.solution[i][j].locationX,
							/* [1] : */ this.solution[i][j].locationY,
							/* [2] : */ this.solution[i][k].locationX,
							/* [3] : */ this.solution[i][k].locationY,
							/* [4] : */ this.solution[i][j].originalX,
							/* [5] : */ this.solution[i][j].originalY,
							/* [6] : */ this.solution[i][k].originalX,
							/* [7] : */ this.solution[i][k].originalY };
					// System.err.println("The agents positions are: ")
					// if(agentsPositions[0] == -1 || agentsPositions[1] == -1)
					// continue;
					if (agentsPositions[0] == agentsPositions[2] && agentsPositions[1] == agentsPositions[3] + 2) {
						System.err.println("Corridor symmetry found ");
						this.constraints.add(new SymmetryConstraint(j, k, agentsPositions[0], agentsPositions[1], agentsPositions[2],
								agentsPositions[3], i, SymmetryType.Corridor));
					}
					if (agentsPositions[0] == agentsPositions[2] && agentsPositions[1] == agentsPositions[3]) {
						return new Conflict(j, k, agentsPositions[0], agentsPositions[1], i);
					}
					if (agentsPositions[2] == agentsPositions[4] && agentsPositions[3] == agentsPositions[5]) {
						return new OrderedConflict(j, k, agentsPositions[4], agentsPositions[5], i);
					}
					if (agentsPositions[0] == agentsPositions[6] && agentsPositions[1] == agentsPositions[7]) {
						return new OrderedConflict(k, j, agentsPositions[6], agentsPositions[7], i);
					}
					if (this.solution[i][k].movingBox != null
							&& agentsPositions[0] == this.solution[i][k].movingBox.currX
							&& agentsPositions[1] == this.solution[i][k].movingBox.currY) {
						return new AgentAlongBoxConflict(j, agentsPositions[0], agentsPositions[1], k, i);
					}
					// add check for if box has no goal state, so its just a movable obstacle

					if (this.solution[i][j].movingBox != null
							&& agentsPositions[2] == this.solution[i][j].movingBox.currX
							&& agentsPositions[3] == this.solution[i][j].movingBox.currY) {
						return new AgentAlongBoxConflict(k, agentsPositions[2], agentsPositions[3], j, i);
					}
					// if (this.solution[i][k].movingBox != null && agentsPositions[0] ==
					// this.solution[i][k].movingBox.prevX && agentsPositions[1] ==
					// this.solution[i][k].movingBox.prevY) {
					// return new OrderedConflict(k, j, this.solution[i][k].movingBox.prevX,
					// this.solution[i][k].movingBox.prevY, i);
					// }

					// if (this.solution[i][j].movingBox != null && agentsPositions[4] ==
					// this.solution[i][j].movingBox.currX && agentsPositions[5] ==
					// this.solution[i][j].movingBox.currY) {
					// return new BoxOrderedConflict(j, k, agentsPositions[4], agentsPositions[5],
					// i);
					// }
					//
					// if (this.solution[i][j].movingBox != null && this.solution[i][k].movingBox !=
					// null && this.solution[i][j].movingBox.currX ==
					// this.solution[i][k].movingBox.prevX && this.solution[i][j].movingBox.currY ==
					// this.solution[i][k].movingBox.prevY) {
					// return new BoxOrderedConflict(k, j, this.solution[i][k].movingBox.prevX,
					// this.solution[i][k].movingBox.prevY, i);
					// }
					//
					// if (this.solution[i][k].movingBox != null && this.solution[i][j].movingBox !=
					// null && this.solution[i][k].movingBox.currX ==
					// this.solution[i][j].movingBox.prevX && this.solution[i][k].movingBox.currY ==
					// this.solution[i][j].movingBox.prevY) {
					// return new BoxOrderedConflict(j, k, this.solution[i][j].movingBox.prevX,
					// this.solution[i][j].movingBox.prevY, i);
					// }
					// if (this.solution[i][k].movingBox != null && this.solution[i][j].movingBox !=
					// null && this.solution[i][k].movingBox.currX ==
					// this.solution[i][j].movingBox.currX && this.solution[i][k].movingBox.currY ==
					// this.solution[i][j].movingBox.currY) {
					// return new BoxConflict(j, k, this.solution[i][k].movingBox.currX,
					// this.solution[i][k].movingBox.currY, i);
					// }
				}
			}
			// Action[] jointAction = new Action[this.solution[i].length];
			// for (int l = 0; l < jointAction.length; l++) {
			// jointAction[l] = this.solution[i][l].action;
			// }
			// temporaryState = new State(temporaryState, jointAction);
		}

		// TODO: findConflct
		return null;

	}


	public PlanStep[] findIndividualPlan(int agentIndex, PlanStep[][] individualPlans) {

		ConstraintState constraintState = new ConstraintState(this.state, agentIndex, this.constraints,
				this.boxConstraints, 0);

		// TODO: Instead of initializing the frontier again and again for evey agent, we
		// need to modify so that it re-uses the same frontier. This will optimize a lot
		// the run-speed.
		// HAVE THE FRONTIER AS A SINGLETON OR GLOBAL CLASS THAT WILL BE RE-USED IN
		// CONSTRAINT GRAPHSEARCH AND HERE IN CBSNODE!!!
		// ConstraintFrontier frontier = GlobalExpandsQueue.getInstance().getQueue();
		ConstraintFrontier frontier = new ConstraintFrontierBestFirst(new ConstraintHeuristicAStar());
		PlanStep[] plan = ConstraintGraphSearch.search(this, frontier, agentIndex);

		Logger logger = Logger.getInstance();
		logger.log("^^^^^ .... ^^^^^");
		logger.log("THESE ARE THE CONSTRAINTS: ");
		for (Constraint constr : this.constraints) {
			logger.log(constr.toString());
		}
		logger.log("^^^^^ .... ^^^^^");

		logger.log("THE PLAN FOR: " + agentIndex);
		for (PlanStep step : plan) {
			logger.log("Step: " + step.toString());
		}
		logger.log("");
		if (plan != null && plan.length > this.longestPath) {
			this.longestPath = plan.length;
		}

		individualPlans[agentIndex] = plan;
		// logger.log("THE COSTS FOR: " + plan[plan.length - 1].timestamp);

		this.costs[agentIndex] = plan[plan.length - 1].timestamp;
		// logger.log("THE COSTS after being set FOR: " + this.costs[agentIndex]);

		return plan;
	}

	public PlanStep[][] findPlan() {
		int numberOfAgents = this.state.agentRows.length;
		PlanStep[][] individualPlans = new PlanStep[numberOfAgents][];
		// System.err.println("NUMBER OF AGENTS" + numberOfAgents);
		for (int i = 0; i < numberOfAgents; i++) {

			findIndividualPlan(i, individualPlans);
			// System.out.println("THE PLAN FOR: " + i);
			// for (PlanStep step : plan) {
			// System.out.println("Step: " + step.toString());
			// }
			// TODO: Add search with constraint

		}
		this.solution = PlanStep.mergePlans(individualPlans);
		return this.solution;
	}

	// TAMAS STUFF

	public int sumCosts() {
		int sum = 0;
		for (int i = 0; i < this.costs.length; i++) {
			sum += this.costs[i];
		}
		return sum;
	}

	@Override
	public int hashCode() {
		if (this.hash == 0) {
			final int prime = 43;
			int result = 1;
			result = prime * result + this.constraints.hashCode();
			result = prime * result + Arrays.deepHashCode(this.solution);

			this.hash = result;
		}
		return this.hash;

		// System.err.println("WAS CALLED HASH CODE!");
		// return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		CBSNode other = (CBSNode) obj;
		if (this.constraints.equals(other.constraints) && Arrays.deepEquals(this.solution, other.solution)
				&& this.boxConstraints.equals(other.boxConstraints)) {
			// System.err.println("EQUALS!");
			return true;
		}
		// return this.constraints.equals(other.constraints) &&
		// this.solution.equals(other.solution);
		return false;
	}
}
