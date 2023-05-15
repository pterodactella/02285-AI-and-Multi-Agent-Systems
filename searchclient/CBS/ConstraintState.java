package searchclient.CBS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import searchclient.Action;
import searchclient.Color;
import searchclient.State;

public class ConstraintState {
	private static final Random RNG = new Random(1);

	/*
	 * The agent rows, columns, and colors are indexed by the agent number. For
	 * example, this.agentRows[0] is the row location of agent '0'.
	 */
	public int[] agentRows;
	public int[] agentCols;
	public static Color[] agentColors;

	/*
	 * The walls, boxes, and goals arrays are indexed from the top-left of the
	 * level, row-major order (row, col). Col 0 Col 1 Col 2 Col 3 Row 0: (0,0) (0,1)
	 * (0,2) (0,3) ... Row 1: (1,0) (1,1) (1,2) (1,3) ... Row 2: (2,0) (2,1) (2,2)
	 * (2,3) ... ...
	 * 
	 * For example, this.walls[2] is an array of booleans for the third row.
	 * this.walls[row][col] is true if there's a wall at (row, col).
	 * 
	 * this.boxes and this.char are two-dimensional arrays of chars.
	 * this.boxes[1][2]='A' means there is an A box at (1,2). If there is no box at
	 * (1,2), we have this.boxes[1][2]=0 (null character). Simiarly for goals.
	 * 
	 */
	public static boolean[][] walls;
	public char[][] boxes;
	public static char[][] goals;

	/*
	 * The box colors are indexed alphabetically. So this.boxColors[0] is the color
	 * of A boxes, this.boxColor[1] is the color of B boxes, etc.
	 */
	public static Color[] boxColors;

	public final ConstraintState parent;
	public final Action agentAction;
	private final int g;

	private int hash = 0;
	int timestamp = 0;

	public HashSet<Constraint> constraints;
	int agent = 0;
	public ArrayList<int[]> goalIndex;

	// Constructs an initial state.
	// Arguments are not copied, and therefore should not be modified after being
	// passed in.
	public ConstraintState(State nonConstraintState, int agent, HashSet<Constraint> constraints, int timestamp) {
		this.agentRows = Arrays.copyOf(nonConstraintState.agentRows, nonConstraintState.agentRows.length);
		this.agentCols = Arrays.copyOf(nonConstraintState.agentCols, nonConstraintState.agentCols.length);
		this.agentColors = nonConstraintState.agentColors;
		this.walls = nonConstraintState.walls;
		this.boxes = new char[nonConstraintState.boxes.length][];
		for (int i = 0; i < nonConstraintState.boxes.length; i++) {
			this.boxes[i] = Arrays.copyOf(nonConstraintState.boxes[i], nonConstraintState.boxes[i].length);
		}
		this.boxColors = nonConstraintState.boxColors;
		this.goals = nonConstraintState.goals;
		this.parent = null;
		this.agentAction = null;
		this.g = 0;
		this.constraints = constraints;
		this.timestamp = timestamp;
		this.agent = agent;
		this.goalIndex = new ArrayList<>(); // Initialize goalIndex ArrayList

		preProcessMaps();

	}

	// Constructs the state resulting from applying jointAction in parent.
	// Precondition: Joint action must be applicable and non-conflicting in parent
	// state.
	private ConstraintState(ConstraintState parent, Action agentAction) {
		// System.err.println("Constraint move state constructor parent : " +
		// parent.toString());

		// Copy parent
		this.agentRows = Arrays.copyOf(parent.agentRows, parent.agentRows.length);
		this.agentCols = Arrays.copyOf(parent.agentCols, parent.agentCols.length);
		this.boxes = new char[parent.boxes.length][];
		for (int i = 0; i < parent.boxes.length; i++) {
			this.boxes[i] = Arrays.copyOf(parent.boxes[i], parent.boxes[i].length);
		}

		// Set own parameters
		this.parent = parent;
		this.agentAction = agentAction;
		this.g = parent.g + 1;
		this.timestamp = parent.timestamp + 1;
		this.constraints = parent.constraints;
		this.agent = parent.agent;
		// Apply each action
		int numAgents = this.agentRows.length;

		char box;
		int boxRow;
		int boxCol;

		switch (agentAction.type) {
			case NoOp:

				break;

			case Move:
				this.agentRows[agent] += agentAction.agentRowDelta;
				this.agentCols[agent] += agentAction.agentColDelta;

				break;

			case Push:
				this.agentRows[agent] = this.agentRows[agent] + agentAction.agentRowDelta;
				this.agentCols[agent] = this.agentCols[agent] + agentAction.agentColDelta;

				boxRow = this.agentRows[agent] + agentAction.boxRowDelta;
				boxCol = this.agentCols[agent] + agentAction.boxColDelta;
				// delete previous state
				box = this.boxes[this.agentRows[agent]][this.agentCols[agent]];

				this.boxes[this.agentRows[agent]][this.agentCols[agent]] = 0;

				this.boxes[boxRow][boxCol] = box;

				break;

			case Pull:

				boxRow = this.agentRows[agent];
				boxCol = this.agentCols[agent];
				// System.out.println(boxRow + " " + boxCol + "BOX COORDINATES AFTER ACTION");
				boxRow = this.agentRows[agent];
				boxCol = this.agentCols[agent];
				// System.out.println(boxRow + " " + boxCol + "BOX COORDINATES AFTER ACTION");

				box = this.boxes[this.agentRows[agent] - agentAction.boxRowDelta][this.agentCols[agent]
						- agentAction.boxColDelta];
				this.boxes[this.agentRows[agent] - agentAction.boxRowDelta][this.agentCols[agent]
						- agentAction.boxColDelta] = 0;
				this.boxes[boxRow][boxCol] = box;

				this.agentRows[agent] += agentAction.agentRowDelta;
				this.agentCols[agent] += agentAction.agentColDelta;

				break;

		}

	}

	public int g() {
		return this.g;
	}

	public void preProcessMaps() {
		Color agentColor = this.agentColors[this.agent];
		for (int i = 0; i < this.boxes.length; i++) {
			for (int j = 0; j < this.boxes[i].length; j++) {
				char onLocation = this.boxes[i][j];
				if (onLocation != 0) {
					if (!this.boxColors[onLocation - 'A'].equals(agentColor)) {
						this.boxes[i][j] = 0;
					} else if (this.boxColors[onLocation - 'A'].equals(agentColor)) {
						int[] goalIndexArray = { i, j };
						System.err.println("goalIndexArray " + goalIndexArray[0] + " " + goalIndexArray[1]);
						this.goalIndex.add(goalIndexArray);
						System.err.println("goalIndex " + this.goalIndex.get(0)[0] + this.goalIndex.get(0)[1]);

					}

				}
			}
		}
		// for (int i = 0; i < this.age)
	}

	public boolean isGoalState() {
		for (int row = 1; row < this.goals.length - 1; row++) {
			for (int col = 1; col < this.goals[row].length - 1; col++) {
				char goal = this.goals[row][col];

				if ('A' <= goal && goal <= 'Z'
						&& ConstraintState.boxColors[goal - 'A'] == ConstraintState.agentColors[this.agent]
						&& this.boxes[row][col] != goal) {
					return false;
				} else if (goal == this.agent + '0'
						&& !(this.agentRows[goal - '0'] == row && this.agentCols[goal - '0'] == col)) {
					return false;
				}
			}
		}
		System.err.println("Goal State for: " + this.agent + ":\n" + this.toString());
		// System.err.println("Goal State for: " + this.agent + ":\n" +
		// this.toString());
		return true;
	}

	public ArrayList<ConstraintState> getExpandedStates() {
		// int numAgents = this.agentRows.length;

		// Determine list of applicable actions for each individual agent.

		ArrayList<Action> agentActions = new ArrayList<>(Action.values().length);
		for (Action action : Action.values()) {
			if (this.isApplicable(agent, action)) {
				agentActions.add(action);
			}
		}
		Action[] applicableActions = agentActions.toArray(new Action[0]);
		// for (Action a: applicableActions) {
		// System.err.print(a + "; ");
		// }
		// System.err.println();

		// Iterate over joint actions, check conflict and generate child states.
		// Action[] jointAction = new Action[numAgents];
		// int[] actionsPermutation = new int[numAgents];
		ArrayList<ConstraintState> expandedStates = new ArrayList<>(16);

		for (Action nextAction : applicableActions) {
			ConstraintState newState = new ConstraintState(this, nextAction);
			// Apply pruning based on the heuristic value
			if (!expandedStates.contains(newState)) {
				expandedStates.add(newState);
			}
		}
		// Collections.shuffle(expandedStates, ConstraintState.RNG);
		return expandedStates;
	}

	private boolean violatesConstraints(int agentCol, int agentRow) {
		// System.err.println("HERE FOR: " + this.agent + ". TIMESTAMP: " +
		// this.timestamp + ". Agent row: " + agentRow + " ;AGENT COL: " + agentCol);
		// List all the constraints
		// for (Constraint constr: this.constraints) {
		// System.err.println(constr.toString() + "; ");
		// }
		// System.err.println("\n");

		for (Constraint constraint : this.constraints) {
			if (this.timestamp + 1 == constraint.timestamp && constraint.locationX == agentCol
					&& constraint.locationY == agentRow // && constraint.agentIndex == this.agent
			) {
				// System.err.println("VIOLATES CONSTRAINT: " + constraint);
				return true;
			}
			if (this.timestamp == constraint.timestamp && constraint.locationX == agentCol
					&& constraint.locationY == agentRow // && constraint.agentIndex == this.agent
			) {
				// System.err.println("VIOLATES CONSTRAINT: " + constraint);
				return true;
			}
			// if (
			// this.timestamp -1 == constraint.timestamp && constraint.locationX == agentCol
			// && constraint.locationY == agentRow //&& constraint.agentIndex == this.agent
			// ) {
			// System.err.println("VIOLATES CONSTRAINT: " + constraint);
			// return true;
			// }
		}
		return false;

	}

	private boolean isApplicable(int agent, Action action) {
		int agentRow = this.agentRows[agent];
		int agentCol = this.agentCols[agent];
		Color agentColor = this.agentColors[agent];
		int boxRow;
		int boxCol;
		int destinationRow;
		int destinationCol;
		switch (action.type) {
			case NoOp:
				if (violatesConstraints(agentCol, agentRow)) {
					return false;
				}

				return true;

			case Move:
				destinationRow = agentRow + action.agentRowDelta;
				destinationCol = agentCol + action.agentColDelta;

				if (!this.cellIsFree(destinationRow, destinationCol)) {
					return false;
				}
				if (violatesConstraints(destinationCol, destinationRow)) {
					return false;
				}
				return true;

			case Push:
				destinationRow = agentRow + action.agentRowDelta;
				destinationCol = agentCol + action.agentColDelta;

				if (!isBoxAt(destinationRow, destinationCol, agentColor)) {
					return false;
				}
				boxRow = destinationRow + action.boxRowDelta;
				boxCol = destinationCol + action.boxColDelta;

				if (boxRow < 0 || boxCol < 0 || boxRow >= this.boxes.length || boxCol >= this.boxes[0].length) {
					return false;
				}

				if (!this.cellIsFree(boxRow, boxCol)) {
					return false;
				}
				if (violatesConstraints(destinationCol, destinationRow)) {
					return false;
				}
				return true;

			case Pull:

				// check for box location on the map
				if (!this.isBoxAt(agentRow - action.boxRowDelta, agentCol - action.boxColDelta, agentColor)) {
					return false;
				}

				destinationRow = agentRow + action.agentRowDelta;
				destinationCol = agentCol + action.agentColDelta;

				if (destinationRow < 0 || destinationCol < 0 || destinationRow >= this.boxes.length
						|| destinationCol >= this.boxes[0].length) {
					return false;
				}

				if (!this.cellIsFree(destinationRow, destinationCol)) {
					return false;
				}
				if (violatesConstraints(destinationCol, destinationRow)) {
					return false;
				}
				return true;

		}

		// Unreachable:
		return false;
	}

	private boolean isBoxAt(int boxRow, int boxCol, Color boxColor) {

		// check for out of boundaries
		if (boxRow < 0 || boxCol < 0) {
			return false;
		}
		// check for character
		if (this.boxes[boxRow][boxCol] == 0) {
			return false;
		}
		// check color of box to match the agent
		int boxIndex = this.boxes[boxRow][boxCol] - 'A';
		if (!this.boxColors[boxIndex].equals(boxColor)) {
			return false;
		}
		return true;

	}

	private boolean cellIsFree(int row, int col) {
		// return !this.walls[row][col] && this.boxes[row][col] == 0 &&
		// this.agentAt(row, col) == 0;
		return !this.walls[row][col] && this.boxes[row][col] == 0;
	}

	private char agentAt(int row, int col) {
		for (int i = 0; i < this.agentRows.length; i++) {
			if (this.agentRows[i] == row && this.agentCols[i] == col) {
				return (char) ('0' + i);
			}
		}
		return 0;
	}

	public PlanStep[] extractPlan() {
		PlanStep[] plan = new PlanStep[this.timestamp];
		ConstraintState state = this;
		while (state.agentAction != null) {

			plan[state.timestamp - 1] = new PlanStep(state.agentAction, state.agentCols[state.agent],
					state.agentRows[state.agent], state.timestamp, state.parent.agentCols[state.agent],
					state.parent.agentRows[state.agent]);
			state = state.parent;
		}
		return plan;
	}

	@Override
	public int hashCode() {
		if (this.hash == 0) {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(this.agentColors);
			result = prime * result + Arrays.hashCode(this.boxColors);
			result = prime * result + Arrays.deepHashCode(this.walls);
			result = prime * result + Arrays.deepHashCode(this.goals);
			result = prime * result + Arrays.hashCode(this.agentRows);
			result = prime * result + Arrays.hashCode(this.agentCols);
			result = prime * result + Integer.hashCode(this.agent);
			result = prime * result + this.constraints.hashCode();

			for (int row = 0; row < this.boxes.length; ++row) {
				for (int col = 0; col < this.boxes[row].length; ++col) {
					char c = this.boxes[row][col];
					if (c != 0) {
						result = prime * result + (row * this.boxes[row].length + col) * c;
					}
				}
			}
			this.hash = result;
		}
		return this.hash;
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
		ConstraintState other = (ConstraintState) obj;
		if (Arrays.equals(this.agentRows, other.agentRows) && Arrays.equals(this.agentCols, other.agentCols)
				&& Arrays.equals(this.agentColors, other.agentColors) && Arrays.deepEquals(this.walls, other.walls)
				&& Arrays.deepEquals(this.boxes, other.boxes) && Arrays.equals(this.boxColors, other.boxColors)
				&& Arrays.deepEquals(this.goals, other.goals) && this.agent == other.agent
				&& this.constraints.equals(other.constraints) && this.timestamp == other.timestamp) {
			// System.err.println("EQUALS FROM STATE WORKS");
			return true;
		}
		return false;

	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		// s.append("AGENT ACTION: " + this.agentAction.toString() + "\n");
		s.append("AGENT: " + this.agent + "\n");
		for (int row = 0; row < this.walls.length; row++) {
			for (int col = 0; col < this.walls[row].length; col++) {
				if (this.boxes[row][col] > 0) {
					s.append(this.boxes[row][col]);
				} else if (this.walls[row][col]) {
					s.append("+");
				} else if (this.agentAt(row, col) != 0) {
					s.append(this.agentAt(row, col));
				} else {
					s.append(" ");
				}
			}
			s.append("\n");
		}
		return s.toString();
	}


}
