package searchclient;

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
  public ArrayList<Agent> agents;

  public static boolean[][] walls;
  public char[][] boxes;
  public static char[][] goals;

  public static Color[] boxColors;

  public final State parent;
  public Action[] jointAction;
  private final int g;

  private int hash = 0;

  public int cost;
  final public int timestamp;

  public ArrayList<Constraints> globalConstraints;

  // Constructs an initial state.
  // Arguments are not copied, and therefore should not be modified after being
  // passed in.
  public State(ArrayList<Agent> agents, boolean[][] walls, char[][] boxes, Color[] boxColors, char[][] goals) {
    this.agents = agents;
    this.walls = walls;
    this.boxes = boxes;
    this.boxColors = boxColors;
    this.goals = goals;
    this.parent = null;
    this.jointAction = null;
    this.g = 0;
    this.globalConstraints = null;
    this.timestamp = 0;
  }

  public State(State state, Constraints constraints) {
    this.agents = state.agents;
    this.walls = state.walls;
    this.boxes = state.boxes;
    this.boxColors = state.boxColors;
    this.goals = state.goals;
    this.parent = state.parent;
    this.jointAction = state.jointAction;
    this.g = state.g;
    this.globalConstraints = new ArrayList<>(state.globalConstraints);
    this.timestamp = state.timestamp;
    this.globalConstraints.add(constraints);
  }

  public State(State parent, Action[] jointAction, ArrayList<Constraints> newConstraint) {
    this.parent = parent;
    this.timestamp = parent.timestamp;
    this.jointAction = Arrays.copyOf(jointAction, jointAction.length);
    this.g = parent.g + 1;
    this.globalConstraints = newConstraint;
  }

  public State(State parent, ArrayList<Constraints> newConstraints) {
    this.parent = parent;
    this.g = 0;
    this.globalConstraints = newConstraints;
    this.timestamp = 0;
  }

  // Constructs the state resulting from applying jointAction in parent.
  // Precondition: Joint action must be applicable and non-conflicting in parent
  // state.
  private State(State parent, Action[] jointAction) {
    if (jointAction.length != parent.agents.size()) {
      System.err.println("Error: Joint action length: " + jointAction.length + ", Parent agents size: " + parent.agents.size());
      throw new IllegalArgumentException("Invalid number of agents in joint action not allowed");
    }
    this.agents = new ArrayList<>();
    for (Agent agent : parent.agents) {
      this.agents.add(agent.copy());
    }

    this.boxes = new char[parent.boxes.length][];
    for (int i = 0; i < parent.boxes.length; i++) {
      this.boxes[i] = Arrays.copyOf(parent.boxes[i], parent.boxes[i].length);
    }

    this.boxColors = parent.boxColors;
    this.goals = parent.goals;
    this.globalConstraints = new ArrayList<>(parent.globalConstraints);
    this.parent = parent;
    this.jointAction = Arrays.copyOf(jointAction, jointAction.length);
    this.g = parent.g + 1;
    this.timestamp = parent.timestamp + 1;

    int numAgents = this.agents.size();
    for (int i = 0; i < numAgents; i++) {
      Agent agent = this.agents.get(i);
      Action action = jointAction[i];
      char box;
      int boxRow;
      int boxCol;

      switch (action.type) {
        case NoOp:
          break;

        case Move:
          agent.row += action.agentRowDelta;
          agent.col += action.agentColDelta;

          break;

        case Push:
          agent.row += action.agentRowDelta;
          agent.col += action.agentColDelta;
          boxRow = agent.row + action.boxRowDelta;
          boxCol = agent.col + action.boxColDelta;
          // delete previous state
          box = this.boxes[agent.row][agent.col];
          this.boxes[agent.row][agent.col] = 0;
          this.boxes[boxRow][boxCol] = box;
          break;

        case Pull:

          boxRow = agent.row - action.boxRowDelta;
          boxCol = agent.col - action.boxColDelta;
          // System.out.println(boxRow + " " + boxCol + "BOX COORDINATES AFTER ACTION");
          box = this.boxes[boxRow][boxCol];
          this.boxes[boxRow][boxCol] = '-';
          this.boxes[agent.row][agent.col] = box;
          agent.row += action.agentRowDelta;
          agent.col += action.agentColDelta;
          break;

      }
    }
  }

  public int g() {
    return this.g;
  }

  public boolean isGoalState() {
    boolean hasBoxGoals = false;
    for (int row = 1; row < this.goals.length - 1; row++) {
      for (int col = 1; col < this.goals[row].length - 1; col++) {
        char goal = this.goals[row][col];

        if ('A' <= goal && goal <= 'Z') {
          hasBoxGoals = true;
          if (this.boxes[row][col] != goal) {
            return false;
          }
        }
      }
    }
    if (!hasBoxGoals) {
      // No box goals, so the agent can simply move to their goal spot
      return true;
    }
    // Check if there are any boxes of the correct color remaining
    for (int i = 0; i < this.boxColors.length; i++) {
      boolean foundBox = false;
      for (int row = 1; row < this.boxes.length - 1; row++) {
        for (int col = 1; col < this.boxes[row].length - 1; col++) {
          if (this.boxes[row][col] == 'A' + i && this.boxColors[i].equals(this.agents.get(i).color)) {
            foundBox = true;
            break;
          }
        }
        if (foundBox) {
          break;
        }
      }
      if (!foundBox) {
        return true;
      }
    }

    return false;
  }

  public ArrayList<State> getExpandedStates() {

    int numAgents = this.agents.size();

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
      System.err.println("Applicable actions for agent " + agent + ": " + Arrays.toString(applicableActions[agent]));

    }

    // Iterate over joint actions, check conflict and generate child states.
    int[] actionsPermutation = new int[numAgents];
    ArrayList<State> expandedStates = new ArrayList<>(16);

    while (true) {
      Action[] jointAction = new Action[numAgents];
      boolean skipAction = false;
      for (int agent = 0; agent < numAgents; ++agent) {
        if (applicableActions[agent].length == 0) {
          skipAction = true;
          break;
        }
        jointAction[agent] = applicableActions[agent][actionsPermutation[agent]];
      }

      if (!skipAction && !this.isConflicting(jointAction)) {
        expandedStates.add(new State(this, jointAction));
      }

      boolean done = true;
      for (int agent = 0; agent < numAgents; ++agent) {
        if (actionsPermutation[agent] < applicableActions[agent].length - 1) {
          ++actionsPermutation[agent];
          done = false;
          break;
        } else {
          actionsPermutation[agent] = 0;
        }
      }

      if (done) {
        break;
      }
    }

    return expandedStates;
  }

  private boolean isApplicable(int agent, Action action) {
    int agentRow = this.agents.get(agent).row;
    int agentCol = this.agents.get(agent).col;
    Color agentColor = this.agents.get(agent).color;
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

        return !this.constraintViolated(destinationRow, destinationCol, this.timestamp)
            && this.cellIsFree(destinationRow, destinationCol);

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
        if (!this.constraintViolated(boxRow, boxCol, this.timestamp)) {
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
        if (!this.constraintViolated(destinationRow, destinationCol, this.timestamp)) {
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
    int boxIndex = this.boxes[boxRow][boxCol] - 'A' + 1;
    if (this.boxColors[boxIndex] == null || !this.boxColors[boxIndex].equals(boxColor)) {
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

    return false;
  }

  private boolean cellIsFree(int row, int col) {
    return !this.walls[row][col] && this.boxes[row][col] == 0 && this.agentAt(row, col) == 0;
  }

  private char agentAt(int row, int col) {
    for (Agent agent : this.agents) {
      if (agent.row == row && agent.col == col) {
        return (char) ('0' + agent.id);
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
          result = prime * result + Arrays.hashCode(this.boxColors);
          result = prime * result + Arrays.deepHashCode(this.walls);
          result = prime * result + Arrays.deepHashCode(this.goals);
          
          // include agent positions in hash code calculation
          for (Agent agent : this.agents) {
              result = prime * result + agent.row * 31 + agent.col;
          }
          
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
    if (!Arrays.equals(this.boxColors, other.boxColors)) {
      return false;
    }
    if (!Arrays.deepEquals(this.goals, other.goals)) {
      return false;
    }
    if (!Arrays.deepEquals(this.walls, other.walls)) {
      return false;
    }
    if (!Arrays.deepEquals(this.boxes, other.boxes)) {
      return false;
    }
    // compare each agent in the state
    for (int i = 0; i < this.agents.size(); i++) {
      Agent thisAgent = this.agents.get(i);
      Agent otherAgent = other.agents.get(i);
      if (thisAgent.row != otherAgent.row || thisAgent.col != otherAgent.col) {
        return false;
      }
    }
    return true;
  }
  

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("Global Constraints: ");
    s.append(globalConstraints);
    s.append("\n");

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

  private boolean constraintViolated(int destCol, int destRow, int timestamp) {
    if (globalConstraints == null || globalConstraints.size() == 0) {
      return false;
    }
    for (int i = 0; i < globalConstraints.size(); i++) {
      if (globalConstraints.get(i).isViolated(destCol, destRow, timestamp)) {
        return true;
      }
    }
    return false;
  }
}
