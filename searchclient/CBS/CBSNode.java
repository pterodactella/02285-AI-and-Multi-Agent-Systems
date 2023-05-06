package searchclient.CBS;

import java.util.ArrayList;
import java.util.Arrays;
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
	public Action[][] solution;
	public int cost;
	private int longestPath;

	public CBSNode(State state) {
		this.state = state;
		this.constraints = new ArrayList<>();
		this.solution = null;
		this.cost = 0;
	}

	// public Conflict findFirstConflict() {
	// for (int i = 0; i < this.longestPath; i++) {
	// for (int j = 0; j < this.solution.length; j++) {
	// for (int k = j; k < this.solution.length; k++) {
	// Action action1 = (i < this.solution[j].length) ? this.solution[j][i]
	// : this.solution[j][this.solution[j].length - 1];
	// Action action2 = (i < this.solution[k].length) ? this.solution[k][i]
	// : this.solution[k][this.solution[k].length - 1];
	// if (action1 != Action.NoOp && action1 != Action.NoOp
	// && (action1.agentRowDelta != 0
	// || action1.agentColDelta != 0 || action2.agentRowDelta != 0
	// || action2.agentColDelta != 0)) {
	// int agent1Row = this.state.agentRows[j];
	// int agent1Col = this.state.agentCols[j];
	// int newRow1 = agent1Row + action1.agentRowDelta;
	// int newCol1 = agent1Col + action1.agentColDelta;

	// int agent2Row = this.state.agentRows[k];
	// int agent2Col = this.state.agentCols[k];
	// int newRow2 = agent2Row + action2.agentRowDelta;
	// int newCol2 = agent2Col + action2.agentColDelta;

	// // Check if there is a box at the new positions
	// boolean boxAtNewPos1 = this.state.boxes[newRow1][newCol1] != 0;
	// boolean boxAtNewPos2 = this.state.boxes[newRow2][newCol2] != 0;

	// // Check if agents are pushing the same box
	// boolean pushingSameBox = boxAtNewPos1 && boxAtNewPos2 &&
	// newRow1 == newRow2 && newCol1 == newCol2;

	// if (!pushingSameBox && boxAtNewPos1 && boxAtNewPos2) {
	// // Check if any constraints block the agents
	// boolean agent1BlockedByConstraint = false;
	// boolean agent2BlockedByConstraint = false;
	// for (Constraint constraint : this.constraints) {
	// if (constraint.agentIndex == j && i < constraint.action.length &&
	// constraint.action[i].agentRowDelta == action1.agentRowDelta &&
	// constraint.action[i].agentColDelta == action1.agentColDelta) {
	// agent1BlockedByConstraint = true;
	// }
	// if (constraint.agentIndex == k && i < constraint.action.length &&
	// constraint.action[i].agentRowDelta == action2.agentRowDelta &&
	// constraint.action[i].agentColDelta == action2.agentColDelta) {
	// agent2BlockedByConstraint = true;
	// }
	// }

	// // If both agents are not blocked by constraints, there is a conflict
	// if (!agent1BlockedByConstraint && !agent2BlockedByConstraint) {
	// return new Conflict(j, k, newRow1, newCol1, i);
	// }
	// }
	// }
	// }
	// }
	// }
	// return null;
	// }
	public Conflict findFirstConflict() {
		for (int i = 0; i < this.longestPath; i++) {
			for (int j = 0; j < this.solution.length; j++) {
				for (int k = j; k < this.solution.length; k++) {
					Action action1 = (i < this.solution[j].length) ? this.solution[j][i]
							: this.solution[j][this.solution[j].length - 1];
					Action action2 = (i < this.solution[k].length) ? this.solution[k][i]
							: this.solution[k][this.solution[k].length - 1];

					if (action1 != Action.NoOp && action2 != Action.NoOp
							&& (action1.agentRowDelta != 0 || action1.agentColDelta != 0 ||
									action2.agentRowDelta != 0 || action2.agentColDelta != 0)) {
						int agent1Row = this.state.agentRows[j];
						int agent1Col = this.state.agentCols[j];
						int newRow1 = agent1Row + action1.agentRowDelta;
						int newCol1 = agent1Col + action1.agentColDelta;

						int agent2Row = this.state.agentRows[k];
						int agent2Col = this.state.agentCols[k];
						int newRow2 = agent2Row + action2.agentRowDelta;
						int newCol2 = agent2Col + action2.agentColDelta;

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
		// Create new goals array for the specific agent only
		char[][] newGoals = new char[state.goals.length][state.goals[0].length];
		for (int i = 0; i < newGoals.length; i++) {
			if (i == agentIndex) {
				newGoals[i] = state.goals[i].clone(); // Use the original goals for the specific agent
			} else {
				newGoals[i] = new char[state.goals[i].length];
				Arrays.fill(newGoals[i], '.'); // Set all other agents' goals to empty
			}
		}

		// Create new state with updated goals for the specific agent
		return new State(state, agentIndex, newGoals, this.constraints, solution[agentIndex]);
	}

	// public Action[][] findPlan() {
	// int numberOfAgents = state.agentRows.length;
	// Action[][] individualPlans = new Action[numberOfAgents][];

	// for (int i = 0; i < numberOfAgents; i++) {

	// State stateForAgent = createStateForAgent(i);
	// Frontier frontier = new FrontierBestFirst(new HeuristicAStar(stateForAgent));
	// Action[] plan = GraphSearch.search(stateForAgent, frontier)[0];
	// if (plan.length > this.longestPath) {
	// this.longestPath = plan.length;
	// }
	// individualPlans[i] = plan;

	// // TODO: Add search with constraint
	// ArrayList<Constraint> constraints = new ArrayList<>();
	// for (int j = 0; j < i; j++) {
	// Constraint constraint = new Constraint(j, individualPlans[j]);
	// constraints.add(constraint);
	// }
	// frontier = new FrontierBestFirst(new HeuristicAStar(stateForAgent));
	// plan = GraphSearch.search(stateForAgent, frontier)[0];
	// individualPlans[i] = plan;

	// }

	// return individualPlans;
	// }
	public Action[][] findPlan() {
		int numberOfAgents = state.agentRows.length;
		Action[][] individualPlans = new Action[numberOfAgents][];

		for (int i = 0; i < numberOfAgents; i++) {

			State stateForAgent = createStateForAgent(i);
			Frontier frontier = new FrontierBestFirst(new HeuristicAStar(stateForAgent));
			Action[] plan = GraphSearch.search(stateForAgent, frontier)[0];
			if (plan.length > this.longestPath) {
				this.longestPath = plan.length;
			}
			individualPlans[i] = plan;

			// Check for conflicts and apply constraints
			Conflict conflict = findFirstConflict();
			while (conflict != null) {
				// Add constraint for the agent involved in the conflict
				ArrayList<Constraint> constraints = new ArrayList<>(this.constraints);
				constraints.add(new Constraint(conflict.agentIndexes[0], conflict.locationX, conflict.locationY,
						conflict.timestamp));

				// Update the state with the new constraints
				stateForAgent = new State(state, i, state.goals, constraints, solution[i]);

				// Recompute the plan for the agent involved in the conflict
				frontier = new FrontierBestFirst(new HeuristicAStar(stateForAgent));
				plan = GraphSearch.search(stateForAgent, frontier)[0];
				individualPlans[conflict.agentIndexes[0]] = plan;

				// Check for new conflicts
				conflict = findFirstConflict();
			}
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
	public int[] boxIndexes;

	public Conflict(int agentAIndex, int agentBIndex, int locationX, int locationY, int timestamp) {
		this.agentIndexes = new int[2];
		this.agentIndexes[0] = agentAIndex;
		this.agentIndexes[1] = agentBIndex;
		this.locationX = locationX;
		this.locationY = locationY;
		this.timestamp = timestamp;
		this.boxIndexes = new int[2];
	}

	// TODO:Include boxes
	public Conflict(int agentAIndex, int agentBIndex, int locationX, int locationY, int timestamp, int boxAIndex,
			int boxBIndex) {
		this.agentIndexes = new int[2];
		this.agentIndexes[0] = agentAIndex;
		this.agentIndexes[1] = agentBIndex;
		this.locationX = locationX;
		this.locationY = locationY;
		this.timestamp = timestamp;
		this.boxIndexes = new int[2];
		this.boxIndexes[0] = boxAIndex;
		this.boxIndexes[1] = boxBIndex;
	}

}

class FrontierBestFirst implements Frontier {
	private Heuristic heuristic;
	private final PriorityQueue<State> priorityQueue;
	private final HashSet<State> set;

	public FrontierBestFirst(Heuristic h) {
		this.heuristic = h;
		this.priorityQueue = new PriorityQueue<State>(this.heuristic);
		this.set = new HashSet<State>(65536);
	}

	@Override
	public void add(State state) {
		this.priorityQueue.add(state);
		this.set.add(state);
	}

	@Override
	public State pop() {
		State state = this.priorityQueue.poll();
		this.set.remove(state);
		return state;
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
	public boolean contains(State state) {
		return this.set.contains(state);
	}

	@Override
	public String getName() {
		return String.format("best-first search using %s", this.heuristic.toString());
	}
}

class HeuristicAStar extends Heuristic {
	public HeuristicAStar(State initialState) {
		super(initialState);
	}

	@Override
	public int f(State s) {
		return s.g() + this.h(s);
	}

	@Override
	public String toString() {
		return "A* evaluation";
	}
}