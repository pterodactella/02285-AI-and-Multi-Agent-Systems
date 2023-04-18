package searchclient;

public class Constraints {
  int agentId;
  Integer loc_x;
  Integer loc_y;
  Integer timestamp;
  Integer loc_x2;
  Integer loc_y2;
  Action action1;
  Action action2;

  Constraints(int agentID, int loc_x, int loc_y, Integer timestamp) {
    this.agentId = agentID;
    this.loc_x = loc_x;
    this.loc_y = loc_y;
    this.timestamp = timestamp;
  }

  Constraints(int agentID, int loc_x, int loc_y, int loc_x2, int loc_y2, Integer timestamp) {
    this.agentId = agentID;
    this.loc_x = loc_x;
    this.loc_y = loc_y;
    this.loc_x2 = loc_x2;
    this.loc_y2 = loc_y2;
    this.timestamp = timestamp;
  }

  public boolean isConflicting(Constraints o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Constraints that = (Constraints) o;

    boolean isVertexConflict = timestamp == that.timestamp && loc_y == that.loc_y && loc_x == that.loc_x;
    boolean isEdgeConflict = timestamp == that.timestamp
        && ((loc_x == that.loc_x2 && loc_y == that.loc_y2 && loc_x2 == that.loc_x && loc_y2 == that.loc_y) ||
            (loc_x2 != 0 && loc_y2 != 0 && loc_x == that.loc_x && loc_y == that.loc_y && loc_x2 == that.loc_x2
                && loc_y2 == that.loc_y2));

    return isVertexConflict || isEdgeConflict;
  }

  public boolean isViolated(Action[] jointAction, State state) {
    Agent a1 = state.agents.get(agent1);
    Agent a2 = state.agents.get(agent2);
    int a1Row = a1.row + jointAction[agent1].agentRowDelta;
    int a1Col = a1.col + jointAction[agent1].agentColDelta;
    int a2Row = a2.row + jointAction[agent2].agentRowDelta;
    int a2Col = a2.col + jointAction[agent2].agentColDelta;
    return manhattanDistance(a1Row, a1Col, a2Row, a2Col) < distance;
  }

  private static int manhattanDistance(int row1, int col1, int row2, int col2) {
    return Math.abs(row1 - row2) + Math.abs(col1 - col2);
  }

}