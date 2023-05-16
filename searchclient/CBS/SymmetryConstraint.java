package searchclient.CBS;

import java.util.Arrays;
import searchclient.CBS.Conflicts.SymmetryType;

public class SymmetryConstraint extends Constraint{
    
    public int agent2;
    public int[] agent2Indexes = new int[2];
    public SymmetryType type;

    public SymmetryConstraint(int agent1, int agent2, int agent1locationX, int agent1locationY, int agent2locationX, int agent2locationY, int timestamp, SymmetryType type) {
        super(agent1, agent1locationX, agent1locationY, timestamp);

        this.agent2 = agent2;
        this.agent2Indexes[0] = agent2locationX;
        this.agent2Indexes[1] = agent2locationY;
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SymmetryConstraint other = (SymmetryConstraint) obj;
        return super.equals(obj) && agent2 == other.agent2 && Arrays.equals(agent2Indexes, other.agent2Indexes)
                && type == other.type;
    }
    
}