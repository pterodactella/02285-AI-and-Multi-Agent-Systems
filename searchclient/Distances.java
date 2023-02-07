package searchclient;

import java.util.Arrays;
import java.util.HashMap;

public abstract class Distances {
	private int[] agentRows;
	private int[] agentCols;
	private char[][] goals;
	protected HashMap<Character, int[]> coordinates;
	public Distances(int[] agentRows, int[] agentCols, char[][] goals) {
		this.agentRows = agentRows;
		this.agentCols = agentCols;
		this.goals = goals;
		
		parseCoordinates();
		
//		for (HashMap.Entry<Character, int[]> set :
//            this.coordinates.entrySet()) {
//			System.out.println(set.getKey() + " = "
//                    + Arrays.toString(set.getValue()));
//       }
	}
	
	public abstract int calculate();
	public void parseCoordinates() {
		this.coordinates = new HashMap<>();
		for(int i=0; i<agentRows.length; i++) {
			coordinates.put((char) ('0' + i), new int[] {agentRows[i], agentCols[i], 0, 0});
		}
		for(int i=0; i<goals.length; i++) {
			for(int j=0; j<goals[i].length; j++) {
				if ('0' <= goals[i][j] && goals[i][j] <= '9') {
					coordinates.get(goals[i][j])[2] = i;
					coordinates.get(goals[i][j])[3] = j;
				}
			}
		}
		
	}
}



class ManhattanDistance extends Distances{
	public ManhattanDistance(int[] agentRows, int[] agentCols, char[][] goals) {
		super(agentRows, agentCols, goals);
	}

	@Override
	public int calculate() {
		int sum = 0;
		for (HashMap.Entry<Character, int[]> set :
            this.coordinates.entrySet()) {
			int[] location = set.getValue();
			sum+= Math.abs(location[2]-location[0]) + Math.abs(location[3]-location[1]);
       }
		return sum;
	}
}


class EuclideanDistanceWithoutRoot extends Distances{
	public EuclideanDistanceWithoutRoot(int[] agentRows, int[] agentCols, char[][] goals) {
		super(agentRows, agentCols, goals);
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
