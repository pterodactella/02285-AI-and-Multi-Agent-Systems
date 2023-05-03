package searchclient;

public class Constraints {
    int agent1;
    int agent2;
    int loc_x;
    int loc_y;
    Integer timestamp;
    Action action1;
    Action action2;

    Constraints(int agentID, int loc_x, int loc_y, Integer timestamp) {
        this.agent1 = agentID;
        this.loc_x = loc_x;
        this.loc_y = loc_y;
        this.timestamp = timestamp;
    }

    public boolean isConflicting(Constraints o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Constraints that = (Constraints) o;
        if ((this.agent1 == that.agent1 && this.agent1 == that.agent2) || (this.agent2 == that.agent1
                && this.agent2 == that.agent2)) {
            // The agents are the same in both constraints, so check if they are occupying
            // the same location at the same time
            return this.timestamp == that.timestamp && this.loc_y == that.loc_y && this.loc_x == that.loc_x;
        }
        return false;
    }

    public boolean isViolated(int agentId, int destCol, int destRow, int timestamp) {
        // Check if the constraint is being violated
        if (agentId == agent1 && destCol == loc_x && destRow == loc_y && timestamp == this.timestamp) {
            // Agent is occupying the cell, so the constraint is being violated
            return true;
        }

        // Constraint is not being violated
        return false;
    }

}