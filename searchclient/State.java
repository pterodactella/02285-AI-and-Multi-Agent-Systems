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
  // public int[] agentRows;
  // public int[] agentCols;
  // public static Color[] agentColors;
  public ArrayList<Agent> agents;

  public static boolean[][] walls;
  public char[][] boxes;
  public static char[][] goals;

  public static Color[] boxColors;
  public final State parent;
  public final Action[] jointAction;
  private final int g;
  private int hash = 0;
  public int cost;
  private int timestamp;

  public ArrayList<Constraints> globalConstraints;


  // Constructs an initial state.
  // Arguments are not copied, and therefore should not be modified after being
  // passed in.
  public State(ArrayList<Agent> agents, boolean[][] walls,
      char[][] boxes, Color[] boxColors, char[][] goals) {
    this.agents = new ArrayList<>(agents.size());
    for (Agent agent : agents) {
      this.agents.add(agent.copy());
    }
    this.walls = walls;
    this.boxes = boxes;
    this.boxColors = boxColors;
    this.goals = goals;
    this.parent = null;
    this.jointAction = null;
    this.g = 0;
    this.globalConstraints = new ArrayList<Constraints>();
    this.timestamp = -1;
  }

  // Constructs the state resulting from applying jointAction in parent.
  // Precondition: Joint action must be applicable and non-conflicting in parent
  // state.
  private State(State parent, Action[] jointAction, ArrayList<Constraints> constraints) {
    // Copy parent
    this.agents = new ArrayList<>(parent.agents.size());
    for (Agent agent : parent.agents) {
      this.agents.add(agent.copy());
    }
    this.boxes = new char[parent.boxes.length][];
    this.globalConstraints = constraints;
    for (int i = 0; i < parent.boxes.length; i++) {
      this.boxes[i] = Arrays.copyOf(parent.boxes[i], parent.boxes[i].length);
    }
    // Set own parameters
    this.parent = parent;
    this.jointAction = Arrays.copyOf(jointAction, jointAction.length);
    this.g = parent.g + 1;
    this.timestamp = -1;

    // Apply each action
    int numAgents = this.agents.size();
    for (int agent = 0; agent < numAgents; ++agent) {
      Action action = jointAction[agent];
      char box;
      int boxRow;
      int boxCol;

      switch (action.type) {
        case NoOp:
          break;

        case Move:
          this.agents.get(agent).row += action.agentRowDelta;
          this.agents.get(agent).col += action.agentColDelta;

          break;

        case Push:
          this.agents.get(agent).row = this.agents.get(agent).row + action.agentRowDelta;
          this.agents.get(agent).col = this.agents.get(agent).col + action.agentColDelta;

          boxRow = this.agents.get(agent).row + action.boxRowDelta;
          boxCol = this.agents.get(agent).col + action.boxColDelta;
          // delete previous state
          box = this.boxes[this.agents.get(agent).row][this.agents.get(agent).col];

          this.boxes[this.agents.get(agent).col][this.agents.get(agent).col] = 0;

          this.boxes[boxRow][boxCol] = box;

          break;

        case Pull:

          boxRow = this.agents.get(agent).row;
          boxCol = this.agents.get(agent).col;
          // System.out.println(boxRow + " " + boxCol + "BOX COORDINATES AFTER ACTION");

          box = this.boxes[this.agents.get(agent).row - action.boxRowDelta][this.agents.get(agent).col 
              - action.boxColDelta];
          this.boxes[this.agents.get(agent).row - action.boxRowDelta][this.agents.get(agent).col 
              - action.boxColDelta] = 0;
          this.boxes[boxRow][boxCol] = box;

          this.agents.get(agent).row += action.agentRowDelta;
          this.agents.get(agent).col += action.agentColDelta;

          break;

      }
    }
  }

  public int g() {
    return this.g;
  }

  public void setTimestamp(int timestamp) {
    this.timestamp = timestamp;
  };

  public boolean isGoalState() {
    for (int row = 1; row < this.goals.length - 1; row++) {
      for (int col = 1; col < this.goals[row].length - 1; col++) {
        char goal = this.goals[row][col];

        if ('A' <= goal && goal <= 'Z' && this.boxes[row][col] != goal) {
          return false;
        } else if ('0' <= goal && goal <= '9') {
          int agentIndex = goal - '0';
          if (!(this.agents.get(agentIndex).row == row && this.agents.get(agentIndex).col == col)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  public ArrayList<State> getExpandedStates() {
    int numAgents = this.agents.size();

    // Determine list of applicable actions for each individual agent.
    Action[][] applicableActions = new Action[numAgents][];
    for (int agent = 0; agent < numAgents; ++agent) {
      ArrayList<Action> agentActions = new ArrayList<>(Action.values().length);
      for (Action action : Action.values()) {
        if (this.isApplicable(agent, action, timestamp)) {
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
        expandedStates.add(new State(this, jointAction, globalConstraints));
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

  private boolean isApplicable(int agentIndex, Action action, int timestamp) {
    Agent agent = this.agents.get(agentIndex);
    int agentRow = agent.row;
    int agentCol = agent.col;
    Color agentColor = agent.color;
    int boxRow;
    int boxCol;
    char box;
    int destinationRow;
    int destinationCol;
    if (this.timestamp != -1) {
      throw new Error("Not initialised timestamp");
    }
    ;

    switch (action.type) {
      case NoOp:
        return true;

      case Move:
        destinationRow = agentRow + action.agentRowDelta;
        destinationCol = agentCol + action.agentColDelta;
        return this.cellIsFree(destinationRow, destinationCol);

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
        for (Constraints constraint : this.globalConstraints) {
          if (constraint.isViolated(jointAction, this)) {
            return false;
          }
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
    int numAgents = this.agents.size();

    int[] destinationRows = new int[numAgents]; // row of new cell to become occupied by action
    int[] destinationCols = new int[numAgents]; // column of new cell to become occupied by action
    int[] boxRows = new int[numAgents]; // current row of box moved by action
    int[] boxCols = new int[numAgents]; // current column of box moved by action
    int[] destinationBoxRows = new int[numAgents]; // destination row of box moved by action
    int[] destinationBoxCols = new int[numAgents]; // destination column of box moved by action

    // Collect cells to be occupied and boxes to be moved
    for (int agent = 0; agent < numAgents; ++agent) {
      Action action = jointAction[agent];
      int agentRow = this.agents.get(agent).row;
      int agentCol = this.agents.get(agent).col;
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
    for (Constraints constraint : this.globalConstraints) {
      if (constraint.isViolated(jointAction, this)) {
        return true;
      }
    }

    return false;
  }

  public boolean conflictingConstraints(Constraints[] constraints) {
    for (int i = 0; i < agents.size(); i++) {
      for (int j = i + 1; j < agents.size(); j++) {
        Agent agent1 = agents.get(i);
        Agent agent2 = agents.get(j);
        if (agent1.row == agent2.row && agent1.col == agent2.col) {
          // Agents are in the same cell, so there is a conflict
          return true;
        }
      }
    }

    for (int i = 0; i < constraints.length; i++) {
      Constraints c1 = constraints[i];
      for (int j = i + 1; j < constraints.length; j++) {
        Constraints c2 = constraints[j];
        if (c1.isViolated(c2)) {
          // Two constraints are being violated, so there is a conflict
          return true;
        }
      }
    }

    // No conflicts found
    return false;
  }

  private boolean cellIsFree(int row, int col) {
    return !this.walls[row][col] && this.boxes[row][col] == 0 && this.agentAt(row, col) == 0;
  }

  private char agentAt(int row, int col) {
    for (Agent agent : this.agents) {
      if (agent.row == row && agent.col == col) {
        return (char) ('0' + agent.agentIndex);
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

  public ArrayList<Agents> getAgents() {
    return this.agents;
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
}
