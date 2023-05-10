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
	
	public Constraint(Constraint copy) {
		this.agentIndex = copy.agentIndex;
		this.locationX = copy.locationX;
		this.locationY = copy.locationY;
		this.timestamp = copy.timestamp;
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("locationX: " + locationX + "; ");
		s.append("locationY: " + locationY + "; ");
		s.append("agentIndex: " + agentIndex + "; ");
		s.append("timestamp: " + timestamp + "; ");
		return s.toString();
	}
	
}
