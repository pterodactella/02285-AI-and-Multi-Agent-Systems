package searchclient;

import java.util.Arrays;
import java.util.HashMap;
//This class deals with the distances that are used for push and pull boxes, be aware there is 2 type of Distance class that we uploaded.
public abstract class Distances {
	private int[] agentRows;
	private int[] agentCols;
	private char[][] goals;
	private char[][] boxes;
	protected HashMap<Character, int[]> coordinates;
	public Distances(int[] agentRows, int[] agentCols, char[][] goals, char[][] boxes) {
		this.agentRows = agentRows;
		this.agentCols = agentCols;
		this.goals = goals;
		this.boxes = boxes;
		
		parseCoordinates();
		
	}
	
	public int calculateAgentBoxDistance() { return 0; } ;
	public abstract int calculate();
	public void parseCoordinates() {
		this.coordinates = new HashMap<>();

		
		for(int i=0; i<boxes.length; i++) { // rows? 
			for(int j=0; j<boxes[i].length; j++) {	// cols?
				if('A' <= boxes[i][j] && boxes[i][j] <= 'Z') {
				coordinates.put((char) boxes[i][j], new int[] {i,j, 0, 0, 0, 0}); // character map has mapping: boxLetter -> [row, col, goalRow, goalCol]
				}
			}
		}

		for(int i=0; i<goals.length; i++) { // rows? 
			for(int j=0; j<goals[i].length; j++) { // cols?
//				if ('0' <= goals[i][j] && goals[i][j] <= '9') {
//					coordinates.get(goals[i][j])[2] = i;
//					coordinates.get(goals[i][j])[3] = j;
				if('A' <= goals[i][j] && goals[i][j] <= 'Z') {
					coordinates.get(goals[i][j])[2] = i;
					coordinates.get(goals[i][j])[3] = j;
					coordinates.get(goals[i][j])[4] = agentRows[0];
					coordinates.get(goals[i][j])[5] = agentCols[0];
				}
			}
		}
		
	}
}





class EuclideanDistanceWithoutRoot extends Distances{
	public EuclideanDistanceWithoutRoot(int[] agentRows, int[] agentCols, char[][] goals, char[][] boxes) {
		super(agentRows, agentCols, goals, boxes);
	}

	@Override
	public int calculate() {
		int sum = 0;
		for (HashMap.Entry<Character, int[]> set :
            this.coordinates.entrySet()) {
			int[] location = set.getValue();
			sum+= Math.pow(location[2]-location[0],2) + Math.pow(location[3]-location[1],2);
       }
		return sum;
	}
}