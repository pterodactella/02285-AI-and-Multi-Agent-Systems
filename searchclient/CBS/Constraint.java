package searchclient.CBS;
import searchclient.Action;

public class Constraint {
	public int agentIndex;
	public int locationX;
	public int locationY;
	public int timestamp;
	public Action[] action;
	//TODO:Include boxes
	public Constraint(int agentIndex, int locationX, int locationY, int timestamp ) {
		this.agentIndex = agentIndex;
		this.locationX = locationX;
		this.locationY = locationY;
		this.timestamp = timestamp;
	}

	public Constraint(int agentIndex, Action[] action) {
		this.agentIndex = agentIndex;
		this.action = action;
	}
}
