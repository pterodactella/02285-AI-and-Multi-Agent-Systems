package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class State {
  private static final Random RNG = new Random(1);

  private ArrayList<Agent> agents;
  private int[] agentTimestamps;
  private int[][] boxTimestamps;
  private static boolean[][] walls;
  private char[][] boxes;
  private static char[][] goals;
  private static Color[] boxColors;
  private State parent;
  private Action[] jointAction;
  private int g;
  private int hash = 0;
  private ArrayList<Constraints> globalConstraints = new ArrayList<Constraints>();

  public State(ArrayList<Agent> agents, boolean[][] walls, char[][] boxes, Color[] boxColors, char[][] goals) {
    this.agents = agents;
    this.agentTimestamps = new int[2300];
    this.boxTimestamps = new int[2300][2300];
    this.g = 0;
    this.parent = null;
    State.walls = walls;
    this.boxes = boxes;
    State.boxColors = boxColors;
    State.goals = goals;
    this.globalConstraints = new ArrayList<>(globalConstraints);
    this.parent = null;
    this.jointAction = null;
    this.g = 0;
    this.globalConstraints = null;
  }

  public State(State state, Constraints constraints) {
    this.g = state.g + 1;
    this.parent = state; // set parent state

  
      this.globalConstraints.add(constraints);
    this.agentTimestamps = new int[state.agentTimestamps.length];
    System.arraycopy(state.agentTimestamps, 0, this.agentTimestamps, 0, state.agentTimestamps.length);
    this.boxTimestamps = new int[state.boxTimestamps.length][];
    for (int i = 0; i < state.boxTimestamps.length; i++) {
      this.boxTimestamps[i] = Arrays.copyOf(state.boxTimestamps[i], state.boxTimestamps[i].length);
    }
  }

  // Constructs the state resulting from applying jointAction in parent.
  // Precondition: Joint action must be applicable and non-conflicting in parent
  // state.
  private State(State parent, Action[] jointAction) {
    this.agents = new ArrayList<>();
    for (Agent agent : parent.agents) {
      this.agents.add(agent.copy());
    }

    this.agentTimestamps = Arrays.copyOf(parent.agentTimestamps, parent.agentTimestamps.length);
    this.boxTimestamps = new int[parent.boxTimestamps.length][];
    for (int i = 0; i < parent.boxTimestamps.length; i++) {
      this.boxTimestamps[i] = Arrays.copyOf(parent.boxTimestamps[i], parent.boxTimestamps[i].length);
    }

    this.boxes = new char[parent.boxes.length][];
    for (int i = 0; i < parent.boxes.length; i++) {
      this.boxes[i] = Arrays.copyOf(parent.boxes[i], parent.boxes[i].length);
    }

    this.globalConstraints = new ArrayList<>();
    if (parent != null & parent.globalConstraints != null) {
      this.globalConstraints.addAll(parent.globalConstraints);

    }

    this.parent = parent;
    this.jointAction = Arrays.copyOf(jointAction, jointAction.length);
    this.g = parent.g + 1;

    for (int i = 0; i < this.agents.size(); i++) {
      System.err.print("this.agents.size()"+this.agents.size());
      Action action = this.jointAction[i];
      System.err.print("action"+action);

      char box;
      int boxRow;
      int boxCol;

      switch (action.type) {
        case NoOp:
          break;

        case Move:
          this.agents.get(i).row += action.agentRowDelta;
          this.agents.get(i).col += action.agentColDelta;

          this.agentTimestamps[i]++;

          break;

        case Push:
          this.agents.get(i).row += action.agentRowDelta;
          this.agents.get(i).col += action.agentColDelta;

          boxRow = this.agents.get(i).row + action.boxRowDelta;
          boxCol = this.agents.get(i).col + action.boxColDelta;
          // System.err.println(boxRow + " " + boxCol + "BOX COORDINATES AFTER PUSH");
          // delete previous state
          box = this.boxes[this.agents.get(i).row][this.agents.get(i).col];
          this.boxes[this.agents.get(i).row][this.agents.get(i).col] = 0;
          this.boxes[boxRow][boxCol] = box;
          this.boxTimestamps[boxRow][boxCol]++;
          this.agentTimestamps[i]++;

          break;

        case Pull:

          boxRow = this.agents.get(i).row;
          boxCol = this.agents.get(i).col;
          // System.err.println(boxRow + " " + boxCol + "BOX COORDINATES AFTER PULL");
          box = this.boxes[boxRow - action.boxRowDelta][boxCol - action.boxColDelta];
          this.boxes[boxRow - action.boxRowDelta][boxCol - action.boxColDelta] = 0;
          this.boxes[this.agents.get(i).row][this.agents.get(i).col] = box;
          this.agents.get(i).row += action.agentRowDelta;
          this.agents.get(i).col += action.agentColDelta;
          this.boxTimestamps[boxRow][boxCol]--;
          this.boxTimestamps[this.agents.get(i).row][this.agents.get(i).col]++;
          this.agentTimestamps[i]++;
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
          if ('A' <= goal && goal <= 'Z' && this.boxes[row][col] != goal)
          {
            return false;
          } else if ('0' <= goal && goal <= '9') {
            int agentId = Character.getNumericValue(goal);
            for (Agent agent : this.agents) {
              if (!(agent.id == agentId && agent.row == row && agent.col == col)) {
                  return false;
              }
          }
        }
      }
    }
    return true;

  }

  public ArrayList<State> getExpandedStates() {

    int numAgents = this.agents.size();
    // System.err.println("numAgents: " + numAgents);

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
      System.err.println(agentActions.toArray(applicableActions[agent]));
    }

    // Iterate over joint actions, check conflict and generate child states.
    int[] actionsPermutation = new int[numAgents];
    Action[] jointAction = new Action[numAgents];
    ArrayList<State> expandedStates = new ArrayList<>();

    while (true) {

      for (int agent = 0; agent < numAgents; ++agent) {
        jointAction[agent] = applicableActions[agent][actionsPermutation[agent]];
        System.err.println(jointAction[agent]);

      }

      if (!this.isConflicting(jointAction)) {
        expandedStates.add(new State(this, jointAction));
        System.err.println(expandedStates);

      }

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
    int destinationRow;
    int destinationCol;

    int currentTime = this.agentTimestamps[agent];

    switch (action.type) {
      case NoOp:
        return true;

      case Move:
        // System.err.println("Move: ");
        destinationRow = agentRow + action.agentRowDelta;
        destinationCol = agentCol + action.agentColDelta;

        if (this.constraintViolated(agent, destinationRow, destinationCol, currentTime)) {
          return false;
        }
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
        } else if (!this.cellIsFree(boxRow, boxCol)) {
          return false;
        } else if (constraintViolated(agent, destinationRow, destinationCol, currentTime)) {
          return false;
        }
        return true;

      case Pull:

        // check for box location on the map
        if (!this.isBoxAt(agentRow - action.boxRowDelta, agentCol - action.boxColDelta, agentColor)) {
          return false;
        }

        boxRow = agentRow - action.boxRowDelta; // Fix: use boxRow and boxCol
        boxCol = agentCol - action.boxColDelta;
        destinationRow = agentRow + action.agentRowDelta;
        destinationCol = agentCol + action.agentColDelta;

        if (destinationRow < 0 || destinationCol < 0 || destinationRow >= this.boxes.length
            || destinationCol >= this.boxes[0].length) {
          return false;
        }

        if (!this.cellIsFree(destinationRow, destinationCol)) {
          return false;
        }
        if (constraintViolated(agent, destinationRow, destinationCol, currentTime)) {
          return false;
        }

        return true;

    }

    // Unreachable:
    return false;
  }

  private boolean isBoxAt(int boxRow, int boxCol, Color boxColor) {

    // System.err.println("boxRow: " + boxRow + " boxCol: " + boxCol + " boxColor: "
    // + boxColor);
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
    if (State.boxColors[boxIndex] == null || !State.boxColors[boxIndex].equals(boxColor)) {
      return false;
    }
    return true;

  }

  private boolean isConflicting(Action[] jointAction) {
    System.err.println(this.agents.size());
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
      System.err.println("agentRow" + agentRow);

      int agentCol = this.agents.get(agent).col;
      System.err.println("agentCol" + agentCol);
      switch (action.type) {
        case NoOp:
          System.err.println("Noop");
          break;

        case Move:
          System.err.println("Move");

          destinationRows[agent] = agentRow + action.agentRowDelta;
          destinationCols[agent] = agentCol + action.agentColDelta;
          boxRows[agent] = agentRow; // Distinct dummy value
          boxCols[agent] = agentCol; // Distinct dummy value
          destinationBoxRows[agent] = agentRow + action.agentRowDelta;
          destinationBoxCols[agent] = agentCol + action.agentColDelta;
          // if (constraintViolated(destinationCols[agent], destinationRows[agent], agentTimestamps[agent])) {
          //   return true;
          // }
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
        if (destinationRows[a1] == destinationRows[a2] && 
        destinationCols[a1] == destinationCols[a2] && 
        agentTimestamps[a1] == agentTimestamps[a2]) {
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
      plan[state.g-1] = state.jointAction;
      state = state.parent;
    }
    System.err.println("plan: " + plan);

    return plan;
  }

  @Override
  public int hashCode() {
    if (this.hash == 0) {
      final int prime = 31;
      int result = 1;
      result = prime * result + Arrays.hashCode(State.boxColors);

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

    for (int row = 0; row < walls.length; row++) {
      for (int col = 0; col < walls[row].length; col++) {
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

  private boolean constraintViolated(int agentId,int destCol, int destRow, int timestamp) {
    if (globalConstraints == null || globalConstraints.size() == 0) {
      return false;
    }
    for (int i = 0; i < globalConstraints.size(); i++) {
      if (globalConstraints.get(i).isViolated(agentId, destCol, destRow, timestamp)) {
        return true;
      }
    }
    return false;
  }

  public ArrayList<Agent> getAgents() {
    return this.agents;
  }

  public ArrayList<Constraints> getConstraints() {
    return this.globalConstraints;
  }

  public char[][] getGoals() {
    return State.goals;
  }

  public boolean[][] getWalls() {
    return State.walls;
  }

  public char[][] getBoxes() {
    return this.boxes;
  }

  public int[] getAgentTimestamps() {
    return this.agentTimestamps;
  }

  public int[][] getBoxTimestamps() {
    return this.boxTimestamps;
  }
}
