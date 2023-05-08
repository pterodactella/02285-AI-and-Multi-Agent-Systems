package searchclient.CBS;

public class Constraint {
	public int agentIndex;
	public int locationX;
	public int locationY;
	public int timestamp;
	//TODO:Include boxes
	public Constraint(int agentIndex, int locationX, int locationY, int timestamp ) {
		this.agentIndex = agentIndex;
		this.locationX = locationX;
		this.locationY = locationY;
		this.timestamp = timestamp;
	}
	
}
