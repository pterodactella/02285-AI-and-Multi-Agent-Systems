package searchclient.CBS;

import java.util.ArrayList;

import searchclient.Action;
import searchclient.State;

public class PlanStep {
	
	public Action action;
	int locationX;
	int locationY;
	int timestamp;
	
	
	public PlanStep(Action action, int locationX, int locationY, int timestamp) {
		this.action = action;
		this.locationX = locationX;
		this.locationY = locationY;
		this.timestamp = timestamp;
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
//		for (int row = 0; row < this.walls.length; row++) {
//			for (int col = 0; col < this.walls[row].length; col++) {
//				if (this.boxes[row][col] > 0) {
//					s.append(this.boxes[row][col]);
//				} else if (this.walls[row][col]) {
//					s.append("+");
//				} else if (this.agentAt(row, col) != 0) {
//					s.append(this.agentAt(row, col));
//				} else {
//					s.append(" ");
//				}
//			}
//			s.append("\n");
//		}
		s.append("action: " + action.toString() + "; ");
		s.append("locationX: " + locationX + "; ");
		s.append("locationY: " + locationY + "; ");
		s.append("timestamp: " + timestamp + "; ");
		return s.toString();
	}
}
