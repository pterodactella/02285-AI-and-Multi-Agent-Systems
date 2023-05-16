package searchclient.CBS.Conflicts;

public class SymmetryConflict implements GenericConflict{
    
    public int agent1;
    public int agent2;
    public int[][] agentIndexes = new int[2][2];
    public int timestamp;
    public SymmetryType type;

    public SymmetryConflict(int agent1, int agent2, int agent1locationX, int agent1locationY, int agent2locationX, int agent2locationY, int timestamp, SymmetryType type) {
        this.agent1 = agent1;
        this.agent2 = agent2;
        this.agentIndexes[0][0] = agent1locationX;
        this.agentIndexes[0][1] = agent1locationY;
        this.agentIndexes[1][0] = agent2locationX;
        this.agentIndexes[1][1] = agent2locationY;
        this.timestamp = timestamp; 
        this.type = type;
    }
    
}
