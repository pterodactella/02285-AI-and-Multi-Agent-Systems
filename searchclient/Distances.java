package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

//This class deals with the distances that are used for push and pull boxes, be aware there is 2 type of Distance class that we uploaded.
public abstract class Distances {
	private int[] agentRows;
	private int[] agentCols;
	private char[][] goals;
	private char[][] boxes;
	protected HashMap<Character, ArrayList<int[]>> coordinates;

	public Distances(int[] agentRows, int[] agentCols, char[][] goals, char[][] boxes) {
		this.agentRows = agentRows.clone();
		this.agentCols = agentCols.clone();
		this.goals = new char[goals.length][];
		for (int i = 0; i < goals.length; i++) {
			this.goals[i] = goals[i].clone();
		}
		this.boxes = new char[boxes.length][];
		for (int i = 0; i < boxes.length; i++) {
			this.boxes[i] = boxes[i].clone();
		}
//		this.goals = goals;
//		this.boxes = boxes;

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
//		
//		for(int i=0; i<agentRows.length; i++) {
//			coordinates.put((char) ('0' + i), new int[] {agentRows[i], agentCols[i], 0, 0});
//		}

		for (int i = 0; i < boxes.length; i++) {
			for (int j = 0; j < boxes[i].length; j++) {
				if ('A' <= boxes[i][j] && boxes[i][j] <= 'Z') {
					if (!coordinates.containsKey((char) boxes[i][j])) {
						coordinates.put((char) boxes[i][j], new ArrayList<>());
						coordinates.get((char) boxes[i][j]).add(new int[] { i, j, -1, -1 });
					} else {
						coordinates.get((char) boxes[i][j]).add(new int[] { i, j, -1, -1 });
					}
				}
			}
		}
//		System.err.println(coordinates.keySet());

		for (int i = 0; i < goals.length; i++) {
			for (int j = 0; j < goals[i].length; j++) {
//				if ('0' <= goals[i][j] && goals[i][j] <= '9') {
//					coordinates.get(goals[i][j])[2] = i;
//					coordinates.get(goals[i][j])[3] = j;
				if ('A' <= goals[i][j] && goals[i][j] <= 'Z') {
					for (int[] arr : coordinates.get(goals[i][j])) {
						if (arr[2] == -1) {
							arr[2] = i;
							arr[3] = j;
						}
					}
				}
			}
		}
		
//		for (HashMap.Entry<Character, ArrayList<int[]>> set : this.coordinates.entrySet()) {
//			for (int j = set.getValue().size() - 1; j >= 0; j--) {
//				if(set.getValue().get(j)[2] == -1) {
//					set.getValue().remove(j);
//				}
//			}
//		}

	}
}

class EuclideanDistanceWithoutRoot extends Distances {
	public EuclideanDistanceWithoutRoot(int[] agentRows, int[] agentCols, char[][] goals, char[][] boxes) {
		super(agentRows, agentCols, goals, boxes);
	}

	@Override
	public int calculate() {
		int sum = 0;
		for (HashMap.Entry<Character, ArrayList<int[]>> set : this.coordinates.entrySet()) {
			for (int[] location : set.getValue()) {
				sum += Math.pow(location[2] - location[0], 2) + Math.pow(location[3] - location[1], 2);
			}
		}
		return sum;
	}
}