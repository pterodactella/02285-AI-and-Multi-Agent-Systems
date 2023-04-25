package searchclient;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

public class State {
  private static final Random RNG = new Random(1);

  /*
   * The agent rows, columns, and colors are indexed by the agent number.
   * For example, this.agentRows[0] is the row location of agent '0'.
   */

  // WIP we need to bridge this agent rows to the actual agent object
  public int[] agentRows;
  public int[] agentCols;
  public static Color[] agentColors;
  public Agent[] agents;

  public static boolean[][] walls;
  public char[][] boxes;
  public static char[][] goals;

  public static Color[] boxColors;

  public final State parent;
  public final Action[] jointAction;
  private final int g;

  private int hash = 0;

  public int cost;
  public int timestamp;

  public Constraints[] globalConstraints;

  // Constructs an initial state.
  // Arguments are not copied, and therefore should not be modified after being
  // passed in.
  public State(int[] agentRows, int[] agentCols, Color[] agentColors, boolean[][] walls,
      char[][] boxes, Color[] boxColors, char[][] goals) {
    this.agentRows = agentRows;
    this.agentCols = agentCols;
    this.agentColors = agentColors;
    this.walls = walls;
    this.boxes = boxes;
    this.boxColors = boxColors;
    this.goals = goals;
    this.parent = null;
    this.jointAction = null;
    this.g = 0;
    this.globalConstraints = new Constraints[agentRows.length];
    this.timestamp = 0;
  }

  public State(State parent, Action[] jointAction, Constraints[] newConstraint) {
    this.parent = parent;
    this.timestamp = parent.timestamp += 1;
    this.jointAction = Arrays.copyOf(jointAction, jointAction.length);
    this.g = parent.g + 1;
    this.globalConstraints = newConstraint;
  }

  // Constructs the state resulting from applying jointAction in parent.
  // Precondition: Joint action must be applicable and non-conflicting in parent
  // state.
  private State(State parent, Action[] jointAction) {
    // Copy parent
    this.agentRows = Arrays.copyOf(parent.agentRows, parent.agentRows.length);
    this.agentCols = Arrays.copyOf(parent.agentCols, parent.agentCols.length);
    this.boxes = new char[parent.boxes.length][];
    for (int i = 0; i < parent.boxes.length; i++) {
      this.boxes[i] = Arrays.copyOf(parent.boxes[i], parent.boxes[i].length);
    }

    // Set own parameters
    this.parent = parent;
    this.jointAction = Arrays.copyOf(jointAction, jointAction.length);
    this.g = parent.g + 1;
    this.timestamp = parent.timestamp + 1; // Increment the timestamp

    // Apply each action
    int numAgents = this.agentRows.length;
    for (int agent = 0; agent < numAgents; ++agent) {
      Action action = jointAction[agent];
      char box;
      int boxRow;
      int boxCol;

      switch (action.type) {
        case NoOp:
          break;

        case Move:
          this.agentRows[agent] += action.agentRowDelta;
          this.agentCols[agent] += action.agentColDelta;

          break;

        case Push:
          this.agentRows[agent] = this.agentRows[agent] + action.agentRowDelta;
          this.agentCols[agent] = this.agentCols[agent] + action.agentColDelta;

          boxRow = this.agentRows[agent] + action.boxRowDelta;
          boxCol = this.agentCols[agent] + action.boxColDelta;
          // delete previous state
          box = this.boxes[this.agentRows[agent]][this.agentCols[agent]];

          this.boxes[this.agentRows[agent]][this.agentCols[agent]] = 0;

          this.boxes[boxRow][boxCol] = box;

          break;

        case Pull:

          boxRow = this.agentRows[agent];
          boxCol = this.agentCols[agent];
          // System.out.println(boxRow + " " + boxCol + "BOX COORDINATES AFTER ACTION");

          box = this.boxes[this.agentRows[agent] - action.boxRowDelta][this.agentCols[agent]
              - action.boxColDelta];
          this.boxes[this.agentRows[agent] - action.boxRowDelta][this.agentCols[agent]
              - action.boxColDelta] = 0;
          this.boxes[boxRow][boxCol] = box;

          this.agentRows[agent] += action.agentRowDelta;
          this.agentCols[agent] += action.agentColDelta;

          break;

      }
    }
  }

  public int g() {
    return this.g;
  }

  public boolean isGoalState() {
    for (int row = 1; row < this.goals.length - 1; row++) {
      for (int col = 1; col < this.goals[row].length - 1; col++) {
        char goal = this.goals[row][col];

        if ('A' <= goal && goal <= 'Z' && this.boxes[row][col] != goal) {
          return false;
        } else if ('0' <= goal && goal <= '9' &&
            !(this.agentRows[goal - '0'] == row && this.agentCols[goal - '0'] == col)) {
          return false;
        }
      }
    }
    return true;
  }

  public ArrayList<State> getExpandedStates() {
    int numAgents = this.agentRows.length;

    // Determine list of applicable actions for each individual agent.
    Action[][] applicableActions = new Action[numAgents][];
    for (int agent = 0; agent < numAgents; ++agent) {
      ArrayList<Action> agentActions = new ArrayList<>(Action.values().length);
      for (Action action : Action.values()) {
        if (this.isApplicable(agent, action)) {
          agentActions.add(action);

        }
      }
      applicableActions[agent] = agentActions.toArray(new Action[0]);
    }

    // Iterate over joint actions, check conflict and generate child states.
    Action[] jointAction = new Action[numAgents];
    int[] actionsPermutation = new int[numAgents];
    ArrayList<State> expandedStates = new ArrayList<>(16);

    while (true) {
      for (int agent = 0; agent < numAgents; ++agent) {
        jointAction[agent] = applicableActions[agent][actionsPermutation[agent]];
      }

      if (!this.isConflicting(jointAction)) {
        expandedStates.add(new State(this, jointAction));
      }

      // Advance permutation
      boolean done = false;
      for (int agent = 0; agent < numAgents; ++agent) {
        if (actionsPermutation[agent] < applicableActions[agent].length - 1) {
          ++actionsPermutation[agent];
          break;
        } else {
          actionsPermutation[agent] = 0;
          if (agent == numAgents - 1) {
            done = true;
          }
        }
      }

      // Last permutation?
      if (done) {
        break;
      }
    }

    Collections.shuffle(expandedStates, State.RNG);
    return expandedStates;
  }

  private boolean isApplicable(int agent, Action action) {
    int agentRow = this.agentRows[agent];
    int agentCol = this.agentCols[agent];
    Color agentColor = this.agentColors[agent];
    int boxRow;
    int boxCol;
    char box;
    int destinationRow;
    int destinationCol;

    switch (action.type) {
      case NoOp:
        return true;

      case Move:
        // destinationRow = agentRow + action.agentRowDelta;
        // destinationCol = agentCol + action.agentColDelta;
        // return this.cellIsFree(destinationRow, destinationCol);
        destinationRow = agentRow + action.agentRowDelta;
        destinationCol = agentCol + action.agentColDelta;

        return this.checkConstraint(destinationRow, destinationCol, this.timestamp) && this.cellIsFree(destinationRow, destinationCol);

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
        if (!this.checkConstraint(boxRow, boxCol, this.timestamp)) {
          return false;
        }
        return true;

      case Pull:

        // check for box location on the map
        if (!this.isBoxAt(agentRow - action.boxRowDelta, agentCol - action.boxColDelta, agentColor)) {
          return false;
        }

        //
        // boxRow = agentRow + action.boxRowDelta;
        // boxCol = agentCol + action.boxColDelta;

        destinationRow = agentRow + action.agentRowDelta;
        destinationCol = agentCol + action.agentColDelta;

        if (destinationRow < 0 || destinationCol < 0 || destinationRow >= this.boxes.length
            || destinationCol >= this.boxes[0].length) {
          return false;
        }

        if (!this.cellIsFree(destinationRow, destinationCol)) {
          return false;
        }
        if(!this.checkConstraint(destinationRow, destinationCol, this.timestamp)){
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

  private boolean isConflicting(Action[] jointAction) {
    int numAgents = this.agentRows.length;

    int[] destinationRows = new int[numAgents]; // row of new cell to become occupied by action
    int[] destinationCols = new int[numAgents]; // column of new cell to become occupied by action
    int[] boxRows = new int[numAgents]; // current row of box moved by action
    int[] boxCols = new int[numAgents]; // current column of box moved by action
    int[] destinationBoxRows = new int[numAgents]; // destination row of box moved by action
    int[] destinationBoxCols = new int[numAgents]; // destination column of box moved by action

    // Collect cells to be occupied and boxes to be moved
    for (int agent = 0; agent < numAgents; ++agent) {
      Action action = jointAction[agent];
      int agentRow = this.agentRows[agent];
      int agentCol = this.agentCols[agent];
      int boxRow;
      int boxCol;

      switch (action.type) {
        case NoOp:
          break;

        case Move:
          destinationRows[agent] = agentRow + action.agentRowDelta;
          destinationCols[agent] = agentCol + action.agentColDelta;
          boxRows[agent] = agentRow; // Distinct dummy value
          boxCols[agent] = agentCol; // Distinct dummy value
          destinationBoxRows[agent] = agentRow + action.agentRowDelta;
          destinationBoxCols[agent] = agentCol + action.agentColDelta;
          break;

      }
    }

    for (int a1 = 0; a1 < numAgents; ++a1) {
      if (jointAction[a1] == Action.NoOp) {
        continue;
      }

      for (int a2 = a1 + 1; a2 < numAgents; ++a2) {
        if (jointAction[a2] == Action.NoOp) {
          continue;
        }

        // Moving into same cell?
        if (destinationRows[a1] == destinationRows[a2] && destinationCols[a1] == destinationCols[a2]) {
          return true;
        }
      }
    }

    return false;
  }


  private boolean cellIsFree(int row, int col) {
    return !this.walls[row][col] && this.boxes[row][col] == 0 && this.agentAt(row, col) == 0;
  }

  private char agentAt(int row, int col) {
    for (int i = 0; i < this.agentRows.length; i++) {
      if (this.agentRows[i] == row && this.agentCols[i] == col) {
        return (char) ('0' + i);
      }
    }
    return 0;
  }

  public Action[][] extractPlan() {
    Action[][] plan = new Action[this.g][];
    State state = this;
    while (state.jointAction != null) {
      plan[state.g - 1] = state.jointAction;
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
    State other = (State) obj;
    return Arrays.equals(this.agentRows, other.agentRows) &&
        Arrays.equals(this.agentCols, other.agentCols) &&
        Arrays.equals(this.agentColors, other.agentColors) &&
        Arrays.deepEquals(this.walls, other.walls) &&
        Arrays.deepEquals(this.boxes, other.boxes) &&
        Arrays.equals(this.boxColors, other.boxColors) &&
        Arrays.deepEquals(this.goals, other.goals);
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
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

  private boolean checkConstraint(int destCol, int destRow, int timestamp) {
    for (int i = 0; i < globalConstraints.length; i++) {
      if (globalConstraints[i].isViolated(destCol, destRow, timestamp)) {
        return false;
      }
    }
    return true;
  }
}
