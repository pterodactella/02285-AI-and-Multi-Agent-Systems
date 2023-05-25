package searchclient.CBS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.PriorityQueue;

import searchclient.Action;
import searchclient.Frontier;
import searchclient.GraphSearch;
import searchclient.Heuristic;
import searchclient.Logger;
import searchclient.State;
import searchclient.Color;
import searchclient.CBS.SameColoredAgentsAndBoxesObject;

public class CBSNode {
	public State state;
	public HashSet<Constraint> constraints;
	public PlanStep[][] solution;
	public int[] costs;
	private int longestPath;
	public int totalCost;
	private InitialState initialStateForStorage;
	private ArrayList<Integer> shiftedAgents;
	private int hash = 0;

	public CBSNode(State state) {
		this.state = state;
		this.constraints = new HashSet<>();
		this.solution = null;
		this.costs = new int[state.agentRows.length];
		this.totalCost = 0;
	}

	public CBSNode(CBSNode parent) {
		this.state = parent.state;
		this.constraints = new HashSet<>();
		for (Constraint constr : parent.constraints) {
			this.constraints.add(new Constraint(constr));
		}

		this.solution = new PlanStep[parent.solution.length][];
		for (int i = 0; i < parent.solution.length; i++) {
			this.solution[i] = new PlanStep[parent.solution[i].length];
			for (int j = 0; j < parent.solution[i].length; j++) {
				this.solution[i][j] = new PlanStep(parent.solution[i][j]);
			}
		}

		this.costs = parent.costs.clone();
		this.totalCost = 0;
	}

	public GenericConflict findFirstConflict() {
		int[] agentsPositions = null;
		for (int i = 1; i <= this.longestPath; i++) {
			for (int j = 0; j < this.solution[i].length; j++) {
				if (this.solution[i][j].locationX == -1)
					continue;
				for (int k = j + 1; k < this.solution[i].length; k++) {
					if (this.solution[i][k].locationX == -1)
						continue;
					agentsPositions = new int[] { /* [0] : */ this.solution[i][j].locationX,
							/* [1] : */ this.solution[i][j].locationY, /* [2] : */ this.solution[i][k].locationX,
							/* [3] : */ this.solution[i][k].locationY, /* [4] : */ this.solution[i][j].originalX,
							/* [5] : */ this.solution[i][j].originalY, /* [6] : */ this.solution[i][k].originalX,
							/* [7] : */ this.solution[i][k].originalY };
//					System.err.println("The agents positions are: ")
//					if(agentsPositions[0] == -1 || agentsPositions[1] == -1)
//						continue;

					if (agentsPositions[0] == agentsPositions[2] && agentsPositions[1] == agentsPositions[3]) {
						return new Conflict(j, k, agentsPositions[0], agentsPositions[1], i);
					}
					if (agentsPositions[2] == agentsPositions[4] && agentsPositions[3] == agentsPositions[5]) {
						return new OrderedConflict(j, k, agentsPositions[4], agentsPositions[5], i);
					}
					if (agentsPositions[0] == agentsPositions[6] && agentsPositions[1] == agentsPositions[7]) {
						return new OrderedConflict(k, j, agentsPositions[6], agentsPositions[7], i);
					}

				}
			}
		}

		// TODO: findConflct
		return null;

	}

	public PlanStep[][] findPlan() {
		int numberOfAgents = state.agentRows.length;
		PlanStep[][] individualPlans = new PlanStep[numberOfAgents][];
//		System.err.println("NUMBER OF AGENTS" + numberOfAgents);

		for (int i = 0; i < numberOfAgents; i++) {

			System.out.println("------------------ Find individul plan for: " + i + " ------------------");
			findIndividualPlan(i, individualPlans);
			//			System.out.println("THE PLAN FOR: " + i);
			//			for (PlanStep step : plan) {
			//				System.out.println("Step: " + step.toString());
			//			}
			// TODO: Add search with constraint

		}

		return PlanStep.mergePlans(individualPlans);
	}

	public void findIndividualPlan(int agentIndex, PlanStep[][] individualPlans) {

		PlanStep[] plan =calculateIndividualPlanForAgent(agentIndex);


		//TODO: Instead of initializing the frontier again and again for evey agent, we need to modify so that it re-uses the same frontier. This will optimize a lot the run-speed.
		//HAVE THE FRONTIER AS A SINGLETON OR GLOBAL CLASS THAT WILL BE RE-USED IN CONSTRAINT GRAPHSEARCH AND HERE IN CBSNODE!!!

		if (plan != null && plan.length > this.longestPath) {
			this.longestPath = plan.length;
		}

		individualPlans[agentIndex] = plan;
		this.costs[agentIndex] = plan[plan.length - 1].timestamp;

	}

	// This is wehre the whole path calculation goes on
	private PlanStep[]  calculateIndividualPlanForAgent (int agentIndex) {

		// Calculate how many boxes we have for the agent
		SameColoredAgentsAndBoxesObject dataObject = calculateAttributesForSpecificStateCreation(agentIndex) ;
		ArrayList<Integer> sameColoredAgents = dataObject.sameColoredAgents;
		ArrayList<Integer> sameColoredBoxes = dataObject.sameColoredBoxes;
		// int[] sameColoredBoxesInt = Arrays.stream(sameColoredBoxes.toArray(new Integer[sameColoredBoxes.size()])).mapToInt(Integer::intValue).toArray();


		if (sameColoredAgents.size() != 1) {
			System.err.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@----------- This is not one agent -----------@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		}
		
		ArrayList<PlanStep> mergedPlans = new ArrayList<PlanStep>();
		int[] agentPos = new int[]{-1, -1};
		for (int boxIndex = 0; boxIndex < sameColoredBoxes.size(); boxIndex++) {
			
			int[] completedBoxes = new int[boxIndex];
			for (int i = 0; i < boxIndex; i++) {
				completedBoxes[i] = sameColoredBoxes.get(i);
			}
			// Construct a state with the constructor that takes arguments
			State searchSpecificState = createSpecificState(agentIndex, sameColoredBoxes.get(boxIndex), completedBoxes, sameColoredBoxes, agentPos   );
			System.out.println("Specific State: agentsLength: "+ searchSpecificState.agentRows.length + " boxesLength: " + searchSpecificState.boxes.length + " goalsLength: " + searchSpecificState.goals.length    );
			this.state = searchSpecificState;
	
			// Calculate teh shifted agent index that matches the real one in the searchSpecificState
			int shiftedAgentIndex = this.shiftedAgents.indexOf(agentIndex);
	
			// ConstraintFrontier frontier = new ConstraintFrontierBestFirst(new ConstraintHeuristicAStar(constraintState));
			ConstraintFrontier frontier = new ConstraintFrontierBestFirst(new ConstraintHeuristicAStar());
	
			// // we need to create the specific maps here since we have the index here 
			// // and call search on it
			PlanStep[] plan = ConstraintGraphSearch.search(this, frontier, shiftedAgentIndex);
			int timestampShift = mergedPlans.size();
			for (PlanStep step : plan) {
				mergedPlans.add( new PlanStep(step.action, step.locationX, step.locationY, timestampShift+step.timestamp, step.originalX, step.originalY) );
			}
			agentPos[0] = mergedPlans.get(mergedPlans.size() - 1).locationY;
			agentPos[1] = mergedPlans.get(mergedPlans.size() - 1).locationX;

		}

		if( sameColoredBoxes.size() ==0 ) {
			int completedBoxes[] = {-1};
			
			// Construct a state with the constructor that takes arguments
			State searchSpecificState = createSpecificState(agentIndex, -1, completedBoxes, sameColoredBoxes, agentPos   );
			System.out.println("Specific State: agentsLength: "+ searchSpecificState.agentRows.length + " boxesLength: " + searchSpecificState.boxes.length + " goalsLength: " + searchSpecificState.goals.length    );
			this.state = searchSpecificState;
	
			// Calculate teh shifted agent index that matches the real one in the searchSpecificState
			int shiftedAgentIndex = this.shiftedAgents.indexOf(agentIndex);
	
			// ConstraintFrontier frontier = new ConstraintFrontierBestFirst(new ConstraintHeuristicAStar(constraintState));
			ConstraintFrontier frontier = new ConstraintFrontierBestFirst(new ConstraintHeuristicAStar());
	
			// // we need to create the specific maps here since we have the index here 
			// // and call search on it
			PlanStep[] plan = ConstraintGraphSearch.search(this, frontier, shiftedAgentIndex);
			int timestampShift = mergedPlans.size();
			for (PlanStep step : plan) {
				mergedPlans.add( new PlanStep(step.action, step.locationX, step.locationY, timestampShift+step.timestamp, step.originalX, step.originalY) );
			}
			agentPos[0] = mergedPlans.get(mergedPlans.size() - 1).locationY;
			agentPos[1] = mergedPlans.get(mergedPlans.size() - 1).locationX;

		}

		// Convert ArrayList to an array of Person objects
		PlanStep[] plan = mergedPlans.toArray(new PlanStep[mergedPlans.size()]);

		return plan;
	}

	

	public void setNewIndividualPlanForAgent(int agentIndex) {
		PlanStep[] plan = calculateIndividualPlanForAgent(agentIndex);
		this.costs[agentIndex] = plan[plan.length - 1].timestamp;

		if (this.longestPath > plan.length) {
			// just copy it there
			for (int timestamp = 0; timestamp < this.longestPath; timestamp++) {
				if (timestamp < plan.length) {
					this.solution[timestamp][agentIndex] = plan[timestamp];
				} else {
					this.solution[timestamp][agentIndex] = new PlanStep(Action.NoOp, this.solution[timestamp-1][agentIndex].locationX, this.solution[timestamp-1][agentIndex].locationY, timestamp, this.solution[timestamp-1][agentIndex].locationX, this.solution[timestamp-1][agentIndex].locationY );
				}

			}

		} else {
			int prevLength = this.solution.length;
			this.longestPath = plan.length;
			
			// SOLUTION: [timestamp][agentIndex]
			PlanStep[][] newSolution = new PlanStep[this.longestPath][this.solution[0].length];

			// The 0th actions are all NoOps
			for (int agentInd = 0; agentInd < this.solution[0].length; agentInd++) { // for each agent
					newSolution[0][agentInd] = new PlanStep(Action.NoOp, -1, -1, 0, -1, -1);
			}

			// The real action set starts from 1st timestamp
			for (int timestamp = 0; timestamp < this.longestPath; timestamp++) { // for each timestamp
				if (timestamp < prevLength  ) {
					for (int agentInd = 0; agentInd < this.solution[0].length; agentInd++) { // for each agent
						if (agentInd == agentIndex) { // if it is the agent we are calculating the plan for
							newSolution[timestamp][agentInd] = plan[timestamp];
						} else { // if it is one of the other agents
							newSolution[timestamp][agentInd] = this.solution[timestamp][agentInd];
						}
					}
				} else {
					for (int agentInd = 0; agentInd < this.solution[0].length; agentInd++) { // for each agent
						if (agentInd == agentIndex) { // if it is the agent we are calculating the plan for
							newSolution[timestamp][agentInd] = plan[timestamp];
						} else { // if it is one of the other agents
							newSolution[timestamp][agentInd] = new PlanStep(Action.NoOp, newSolution[timestamp-1][agentIndex].locationX, newSolution[timestamp-1][agentIndex].locationY, timestamp, newSolution[timestamp-1][agentIndex].locationX, newSolution[timestamp-1][agentIndex].locationY );
						}
					}
				}
			}

			this.solution = newSolution;
		}

		

	}


	private SameColoredAgentsAndBoxesObject calculateAttributesForSpecificStateCreation(int agentIndex) {
		initialStateForStorage = new InitialState();
		InitialStateObject initialStateObject = initialStateForStorage.getInitialState();

		System.out.println("++++++++++Creating specific state for agent: " + agentIndex + "++++++++++");
		// Create a new state with only the appropriately colored agents
		// Find the color of the agent  
		Color agentColor = initialStateObject.agentColors[agentIndex];
		// Find everything from that color
		ArrayList<Integer> sameColoredAgents = new ArrayList<>(initialStateObject.agentColors.length);
		// agents
		// Color[] agentColors = initialStateObject.agentColors;
		for (int i = 0; i < initialStateObject.agentColors.length; i++) {
			 if (initialStateObject.agentColors[i] == agentColor) {
				  sameColoredAgents.add(i);
			 }
		}
		// boxes
		ArrayList<Integer> sameColoredBoxes = new ArrayList<>(initialStateObject.boxColors.length);
		for (int i = 0; i < initialStateObject.boxColors.length; i++) {
			Color box = initialStateObject.boxColors[i] ;
			 if (box == agentColor) {
				  sameColoredBoxes.add(i);
			 }
		}
		System.out.println("++++++++++ Index of agent: " + agentIndex +",  Color of agent: " + agentColor+ ", Agents with this color: " + sameColoredAgents + ",  Boxes with this color: " + sameColoredBoxes +"   ++++++++++");


  		return new SameColoredAgentsAndBoxesObject(sameColoredAgents, sameColoredBoxes);	

	}


	private State createSpecificState(  int agent, int currentBoxIndexToMove, int[] boxesThatHaveBeenCompleted, ArrayList<Integer> allRelevantBoxes, int[] agentPositionToReturn) {
		initialStateForStorage = new InitialState();
		InitialStateObject initialStateObject = initialStateForStorage.getInitialState();

		SameColoredAgentsAndBoxesObject dataObject = calculateAttributesForSpecificStateCreation(agent) ;
		ArrayList<Integer> sameColoredAgents = dataObject.sameColoredAgents;
		ArrayList<Integer> sameColoredBoxes = dataObject.sameColoredBoxes;

      // this.shiftedAgents = sameColoredAgents.stream().mapToInt(i -> i).toArray();
		this.shiftedAgents = new ArrayList<>(sameColoredAgents.size());
      this.shiftedAgents = sameColoredAgents;


		int[] newAgentRows = new int[sameColoredAgents.size() ];
		int[] newAgentCols =  new int[sameColoredAgents.size() ];
		Color[] newAgentColors = new Color[sameColoredAgents.size() ]; 
		boolean[][] newWalls = new boolean[initialStateObject.wallsIntial.length] [initialStateObject.wallsIntial[0].length ];
		char[][] newBoxes = initialStateObject.boxesInitial; ;
		Color[] newBoxColors = new Color[sameColoredBoxes.size() ];  
		char[][] newGoals = new char[initialStateObject.goals.length] [initialStateObject.goals[0].length];

		// Set the agent details
		if(agentPositionToReturn[0]==-1){
			newAgentRows = setAgentRows(initialStateObject, sameColoredAgents);
			newAgentCols = setAgentCols(initialStateObject, sameColoredAgents);
		}
		else{
			newAgentRows = new int[]{agentPositionToReturn[0]};
			newAgentCols = new int[]{agentPositionToReturn[1]};
		}
		newAgentColors = setAgentColors(initialStateObject, sameColoredAgents);
		newGoals = setGoals(initialStateObject, sameColoredAgents, sameColoredBoxes, initialStateObject.goals);
		newBoxes = setBoxes(initialStateObject, sameColoredBoxes, initialStateObject.boxesInitial );
		newBoxColors = setBoxColors(initialStateObject, sameColoredBoxes);

		// Set the walls
		newWalls = setExtraWalls(initialStateObject, sameColoredAgents, sameColoredBoxes);

		if( currentBoxIndexToMove != -1) { // If we hve any boxes to move
			for (Integer boxIndex : allRelevantBoxes) {
				if( boxIndex == currentBoxIndexToMove) {
					// This is the box that is being moved, no modification is required
				} else if( boxIndex > currentBoxIndexToMove) {
					// This is a box that has not been moved yet
					// The StartingPos of the box has to be set as a wall and the box has to be removed
					setInitialBoxPosAsWall(  newWalls, allRelevantBoxes.indexOf( boxIndex ),  newBoxes );

					// The StartingPos of the box has to be removed
					removeInitialBoxPosFromBoxes( allRelevantBoxes.indexOf( boxIndex ),  newBoxes );

					// The goal of it has to be removed
					removeGoalBoxPosFromGoals( allRelevantBoxes.indexOf( boxIndex ),  newGoals  );

				}	else {
					// This is a box that has already been moved
					// The goalPosition of the box has to be set as a wall
					setGoalBoxPosAsWall(  newWalls, allRelevantBoxes.indexOf( boxIndex ), newGoals ) ;

					// The StartingPos of the box has to be removed
					removeInitialBoxPosFromBoxes( allRelevantBoxes.indexOf( boxIndex ),  newBoxes );

					// The goal of the box has to be removed
					removeGoalBoxPosFromGoals( allRelevantBoxes.indexOf( boxIndex ),  newGoals  );
				
				} 	
			}
		}


		// TODO: create new state, for this new spcificState class is rqeuired WITHOUT THE STATIC fields!
		// Create a new state with only the appropriately colored elements
		State newState = new State(newAgentRows, newAgentCols, newAgentColors, newWalls, newBoxes, newBoxColors, newGoals);

		// NEW STATE PRINTING
		// Printing out the new state from the specific state
		System.out.println("==========Printing out the new state from the specific state============");
		System.out.print(newState.toString() );
		System.out.println("========================================================================");
		// State newState = cbsNode.state;

		return newState;
	}

	public boolean isApplicableStep( PlanStep step, int arrivingTimestamp ) {
		// The walls, boxes, and goals arrays are indexed from the top-left of the level, row-major order (row, col).
		// 		Col 0  Col 1  Col 2  Col 3
		// Row 0: (0,0)  (0,1)  (0,2)  (0,3)  ...
		// Row 1: (1,0)  (1,1)  (1,2)  (1,3)  ...
		// Row 2: (2,0)  (2,1)  (2,2)  (2,3)  ...

		// rows == Y
		// cols == X

		ArrayList<Constraint> constraintsToCheck = new ArrayList<Constraint>();


		// constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
		switch (step.action) {
		case NoOp:
			constraintsToCheck.add(new Constraint( -5,  step.originalX, step.originalY, arrivingTimestamp)); 
			constraintsToCheck.add(new Constraint( -5,  step.originalX, step.originalY, arrivingTimestamp));
			break;
		
		
			case MoveN:
			constraintsToCheck.add(new Constraint( -5,  step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint( -5,  step.originalX, step.originalY-1, arrivingTimestamp)); 
			break;
		case MoveS:
			constraintsToCheck.add(new Constraint( -5,  step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint( -5,  step.originalX, step.originalY+1, arrivingTimestamp));
			break;
		case MoveE:
			constraintsToCheck.add(new Constraint( -5,  step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint( -5,  step.originalX+1, step.originalY, arrivingTimestamp));
			break;
		case MoveW:
			constraintsToCheck.add(new Constraint( -5,  step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint( -5,  step.originalX-1, step.originalY, arrivingTimestamp));
			break;

			case PullNN:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY-1, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY+1, arrivingTimestamp));
			break;
		case PullNE:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX+1, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY+1, arrivingTimestamp));
			break;
		case PullNW:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX-1, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY+1, arrivingTimestamp));
			break;

			case PullSS:	
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY+1, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY-1, arrivingTimestamp));
			break;
		case PullSW:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX-1, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY-1, arrivingTimestamp));
			break;	
		case PullSE:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX+1, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY-1, arrivingTimestamp));
			break;

			case PullEE:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX+1, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX-1, step.originalY, arrivingTimestamp));
			break;
		case PullEN:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY-1, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX-1, step.originalY, arrivingTimestamp));
			break;
		case PullES:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY+1, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX-1, step.originalY, arrivingTimestamp));
			break;

			case PullWW:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX-1, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX+1, step.originalY, arrivingTimestamp));
			break;
		case PullWN:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY-1, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX+1, step.originalY, arrivingTimestamp));
			break;
		case PullWS:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY+1, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX+1, step.originalY, arrivingTimestamp));
			break;


			case PushNN:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY-1, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY-2, arrivingTimestamp));
			break;	
		case PushNE:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY-1, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX+1, step.originalY-1, arrivingTimestamp));
			break;
		case PushNW:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY-1, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX-1, step.originalY-1, arrivingTimestamp));

			case PushSS:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY+1, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY+2, arrivingTimestamp));
			break;
		case PushSE:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY+1, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX+1, step.originalY+1, arrivingTimestamp));
			break;
		case PushSW:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY+1, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX-1, step.originalY+1, arrivingTimestamp));
			break;

			case PushEE:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX+1, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX+2, step.originalY, arrivingTimestamp));
			break;
		case PushEN:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX+1, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX+1, step.originalY-1, arrivingTimestamp));
			break;
		case PushES:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX+1, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX+1, step.originalY+1, arrivingTimestamp));
			break;

			case PushWW:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX-1, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX-2, step.originalY, arrivingTimestamp));
			break;
		case PushWN:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX-1, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX-1, step.originalY-1, arrivingTimestamp));
			break;
		case PushWS:
			constraintsToCheck.add(new Constraint(-5, step.originalX, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX-1, step.originalY, arrivingTimestamp));
			constraintsToCheck.add(new Constraint(-5, step.originalX-1, step.originalY+1, arrivingTimestamp));
			break;


		}





		

			// List all constratins
			// for (Constraint globalConstr2 : this.constraints) { 
			// 	System.out.println("globalConstr:  agentInde:" + globalConstr2.agentIndex + ", locationx: " + globalConstr2.locationX + ", locationy: " + globalConstr2.locationY + ", temistamp: " + globalConstr2.timestamp);
			// }
			System.out.println("");
		// CONSTRAINT 
		// public int agentIndex;
		// public int locationX;
		// public int locationY;
		// public int timestamp;

		for (Constraint globalConstr : this.constraints) {
			for  (Constraint agentConstr : constraintsToCheck ) {
				if (    agentConstr.locationX == globalConstr.locationX && agentConstr.locationY == globalConstr.locationY && agentConstr.timestamp == globalConstr.timestamp ) {
					return false;
				}
			}
		}

		return true;
	}

	public void addToConstraints( PlanStep step, int arrivingTimestamp, int agentIndex ) {
				// The walls, boxes, and goals arrays are indexed from the top-left of the level, row-major order (row, col).
		// 		Col 0  Col 1  Col 2  Col 3
		// Row 0: (0,0)  (0,1)  (0,2)  (0,3)  ...
		// Row 1: (1,0)  (1,1)  (1,2)  (1,3)  ...
		// Row 2: (2,0)  (2,1)  (2,2)  (2,3)  ...

		// rows == Y
		// cols == X


		// locationsToCheck looks like [first,second] [row, col, timestamp]
		ArrayList<Constraint> constraintsToAdd = new ArrayList<Constraint>();

		switch (step.action) {
			case NoOp:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				break;
			
			
				case MoveN:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY-1, arrivingTimestamp));
				break;
			case MoveS:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY+1, arrivingTimestamp));
				break;
			case MoveE:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX+1, step.originalY, arrivingTimestamp));
				break;
			case MoveW:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX-1, step.originalY, arrivingTimestamp));
				break;



				case PullNN:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY-1, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY+1, arrivingTimestamp));
				break;
			case PullNE:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX+1, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY+1, arrivingTimestamp));
				break;
			case PullNW:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX-1, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY+1, arrivingTimestamp));
				break;

				case PullSS:	
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY+1, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY-1, arrivingTimestamp));
				break;
			case PullSW:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX-1, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY-1, arrivingTimestamp));
				break;	
			case PullSE:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX+1, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY-1, arrivingTimestamp));
				break;

				case PullEE:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX+1, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX-1, step.originalY, arrivingTimestamp));
				break;
			case PullEN:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY-1, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX-1, step.originalY, arrivingTimestamp));
				break;
			case PullES:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY+1, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX-1, step.originalY, arrivingTimestamp));
				break;

				case PullWW:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX-1, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX+1, step.originalY, arrivingTimestamp));
				break;
			case PullWN:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY-1, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX+1, step.originalY, arrivingTimestamp));
				break;
			case PullWS:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY+1, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX+1, step.originalY, arrivingTimestamp));
				break;


				case PushNN:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY-1, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY-2, arrivingTimestamp));
				break;	
			case PushNE:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY-1, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX+1, step.originalY-1, arrivingTimestamp));
				break;
			case PushNW:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY-1, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX-1, step.originalY-1, arrivingTimestamp));

				case PushSS:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY+1, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY+2, arrivingTimestamp));
				break;
			case PushSE:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY+1, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX+1, step.originalY+1, arrivingTimestamp));
				break;
			case PushSW:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY+1, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX-1, step.originalY+1, arrivingTimestamp));
				break;

				case PushEE:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX+1, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX+2, step.originalY, arrivingTimestamp));
				break;
			case PushEN:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX+1, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX+1, step.originalY-1, arrivingTimestamp));
				break;
			case PushES:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX+1, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX+1, step.originalY+1, arrivingTimestamp));
				break;

				case PushWW:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX-1, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX-2, step.originalY, arrivingTimestamp));
				break;
			case PushWN:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX-1, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX-1, step.originalY-1, arrivingTimestamp));
				break;
			case PushWS:
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX-1, step.originalY, arrivingTimestamp));
				constraintsToAdd.add(new Constraint(agentIndex, step.originalX-1, step.originalY+1, arrivingTimestamp));
				break;

			default:
				System.err.println("=================================== Unknown Type of Step in addToConstraints() ===================================");
				break;

			}
		for (Constraint constr : constraintsToAdd) {
			this.constraints.add(constr);
		}


	}

	private int[] setAgentRows(InitialStateObject initialStateObject, ArrayList<Integer> sameColoredAgents  ) {
		int [] agentRowsFilled = new int[sameColoredAgents.size() ];

		// System.out.println("length of the same colored agents: "+ sameColoredAgents.size() );
		for (int i = 0; i < sameColoredAgents.size(); i++) {
			agentRowsFilled[i] = initialStateObject.agentRowsInitial[sameColoredAgents.get(i)];
		}
		return agentRowsFilled;
	}

	private int[] setAgentCols(InitialStateObject initialStateObject,  ArrayList<Integer> sameColoredAgents) {
		int [] agentColsFilled = new int[sameColoredAgents.size() ];

		// System.out.println("length of the same colored agents: "+ sameColoredAgents.size() );
		for (int i = 0; i < sameColoredAgents.size(); i++) {
			agentColsFilled[i] = initialStateObject.agentColsInitial[sameColoredAgents.get(i)];
		}
		return agentColsFilled;
	}

	private Color[] setAgentColors(InitialStateObject initialStateObject, ArrayList<Integer> sameColoredAgents ) {

		Color [] agentColorsFilled = new Color[sameColoredAgents.size() ];
		for (int i = 0; i < sameColoredAgents.size(); i++) {
			agentColorsFilled[i] = initialStateObject.agentColors[sameColoredAgents.get(i)];
		}
 		return agentColorsFilled;
	}

	private char[][] setBoxes(InitialStateObject initialStateObject, ArrayList<Integer> sameColoredBoxes, char[][] boxesInitial ) {
		// Perform deep copy
		char[][] copiedBoxes = new char[boxesInitial.length][boxesInitial[1].length];


		for (int i = 0; i < boxesInitial.length; i++) { // rows =first dim
			for(int j = 0; j < boxesInitial[i].length; j++) { // cols =second dim
				char box = boxesInitial[i][j];
				int val = box - 'A';
				
				if( sameColoredBoxes.contains( boxesInitial[i][j] - 'A')   ){
					int index = sameColoredBoxes.indexOf(boxesInitial[i][j] - 'A');
					char temp = (char) (index + 'A');
					copiedBoxes[i][j] = temp;
				}
				//  else {
				// 	copiedBoxes[i][j] = null;
				// }
			}
		}
		return copiedBoxes;
	}

	private Color[] setBoxColors(InitialStateObject initialStateObject, ArrayList<Integer> sameColoredBoxes ) {

		Color [] boxColorsFilled = new Color[sameColoredBoxes.size() ];
		for (int i = 0; i < sameColoredBoxes.size(); i++) {
			boxColorsFilled[i] = initialStateObject.boxColors[sameColoredBoxes.get(i)];
		}
 		return boxColorsFilled;
	}

	private char[][] setGoals(InitialStateObject initialStateObject, ArrayList<Integer> sameColoredAgents, ArrayList<Integer> sameColoredBoxes, char[][] goalsInitial ) {
		// Perform deep copy
		char[][] copiedGoals = new char[goalsInitial.length][goalsInitial[1].length];

		for (int i = 0; i < goalsInitial.length; i++) { // rows =first dim
			for(int j = 0; j < goalsInitial[i].length; j++) { // cols =second dim
				// check if the goal is one of the samecolored agents
				if ( sameColoredAgents.contains (Character.getNumericValue(goalsInitial[i][j]) ) && Character.getNumericValue(goalsInitial[i][j]) <=9 && Character.getNumericValue(goalsInitial[i][j]) >=0 ) {
					// Convert the goal number to the index of the agent in the sameColoredAgents array. In char form
					copiedGoals[i][j] = (char) (sameColoredAgents.indexOf(Character.getNumericValue(goalsInitial[i][j])) + '0');
				}
				// check if the goal is one of the samecolored boxes
				
				else if( sameColoredBoxes.contains( goalsInitial[i][j] - 'A')   ){
					int index = sameColoredBoxes.indexOf(goalsInitial[i][j] - 'A');
					char temp = (char) (index + 'A');
					copiedGoals[i][j] = temp;
				}else{ 
					copiedGoals[i][j] = ' ';
				}
			}
		}
		return copiedGoals;
	}

	public static boolean compareCharArrays(char[][] array1, char[][] array2) {
		if (array1.length != array2.length || array1[0].length != array2[0].length) {
			 // Arrays have different dimensions
			 System.out.println("Arrays are equal: " + false);
			 return false;
		}

		// Iterate through the arrays and compare each element
		for (int i = 0; i < array1.length; i++) {
			 for (int j = 0; j < array1[i].length; j++) {
				  if (array1[i][j] != array2[i][j]) {
						// Elements are not equal
						char one = array1[i][j];
						char two = array2[i][j];
						System.out.println("Arrays are equal: " + false);
						return false;
				  }
			 }
		}

		// All elements are equal
		System.out.println("Arrays are equal: " + true);
		return true;
  }


	private boolean[][] setExtraWalls(InitialStateObject initialStateObject, ArrayList<Integer> sameColoredAgents, ArrayList<Integer> sameColoredBoxes  ) {
		// Perform deep copy
		boolean[][] copiedWalls = new boolean[initialStateObject.wallsIntial.length][];
		for (int i = 0; i < initialStateObject.wallsIntial.length; i++) {
			copiedWalls[i] = new boolean[ initialStateObject.wallsIntial[i].length];
				System.arraycopy(initialStateObject.wallsIntial[i], 0, copiedWalls[i], 0, initialStateObject.wallsIntial[i].length);
		}

		// Iterates over the agents and sets the walls to true when the agentIndex is not in the sameColoredAgents array
		for (int i = 0; i < initialStateObject.agentColsInitial.length; i++) {
			if ( !sameColoredAgents.contains(i) ) {
				copiedWalls[initialStateObject.agentRowsInitial[i]] [initialStateObject.agentColsInitial[i]] = true;
			}
		}

		// Find the boxes that are not in the sameColoredBoxes array and save them in an boxesToSetAsWall
		ArrayList<Character> boxesToSetAsWall = new ArrayList<>();
		for (int i = 0; i < initialStateObject.boxColors.length; i++) {
			if ( initialStateObject.boxColors[i] != null && !sameColoredBoxes.contains(i)  ) {
				boxesToSetAsWall.add( (char) (i + 'A') );
			}
		}

		// Set the walls to true for boxes that are in boxesToSetAsWall
		for (int row = 0; row < copiedWalls.length; row++) {
			for (int col = 0; col < copiedWalls[0].length; col++) {
				if ( boxesToSetAsWall.contains( initialStateObject.boxesInitial[row][col] ) && initialStateObject.goals[row][col] == '\0' ) {
					copiedWalls[row][col] = true;
				};
			}
		}

		// set the walls to true for the goals that are in the boxesToSetAsWall
		for (int row = 0; row < copiedWalls.length; row++) {
			for (int col = 0; col < copiedWalls[0].length; col++) {
				if ( boxesToSetAsWall.contains( initialStateObject.goals[row][col] ) ) {
					copiedWalls[row][col] = true;
				};
			}
		}


		return copiedWalls;
			//			System.out.println("THE PLAN FOR: " + i);
			//			for (PlanStep step : plan) {
			//				System.out.println("Step: " + step.toString());
			//			}
			// TODO: Add search with constraint

		}


	private void setInitialBoxPosAsWall( boolean[][] walls,  int boxIndex, char[][] newBoxes ) {
		for (int row = 0; row < newBoxes.length; row++) { // rows =first dim
			for(int col = 0; col < newBoxes[row].length; col++) { // cols =second dim
				char box = newBoxes[row][col];
				int val = box - 'A'; // The integer value of the character at place boxes[row][col]
				
				if( boxIndex == val  ){
					// set the position of the box as a wall
					walls[row] [col] = true;
				}


			}
		}
	}

	private void removeInitialBoxPosFromBoxes(  int boxIndex, char[][] newBoxes ) {
		for (int row = 0; row < newBoxes.length; row++) { // rows =first dim
			for(int col = 0; col < newBoxes[row].length; col++) { // cols =second dim
				char box = newBoxes[row][col];
				int val = box - 'A'; // The integer value of the character at place boxes[row][col]
				
				if( boxIndex == val  ){
					// set the position of the box as a wall
					newBoxes[row][col] = '\0';
				}
			}
		}
	}

	private void removeGoalBoxPosFromGoals(  int boxIndex, char[][] newGoals  ){
		for (int row = 0; row < newGoals.length; row++) { // rows =first dim
			for(int col = 0; col < newGoals[row].length; col++) { // cols =second dim
				char box = newGoals[row][col];
				int val = box - 'A'; // The integer value of the character at place boxes[row][col]
				
				if( boxIndex == val  ){
					// set the position of the box as a wall
					newGoals[row][col] = ' ';
				}
			}
		}

	}

	private void setGoalBoxPosAsWall(  boolean[][] walls,  int boxIndex,  char[][] newGoals ) {
		for (int row = 0; row < newGoals.length; row++) { // rows =first dim
			for(int col = 0; col < newGoals[row].length; col++) { // cols =second dim
				char box = newGoals[row][col];
				int val = box - 'A'; // The integer value of the character at place boxes[row][col]
				
				if( boxIndex == val  ){
					// set the position of the box as a wall
					walls[row][col] = true;
				}
			}
		}

	}





	public int sumCosts() {
		int sum = 0;
		for (int i = 0; i < this.costs.length; i++) {
			sum += this.costs[i];
		}
		return sum;
	}

	@Override
	public int hashCode() {
		if (this.hash == 0) {
			final int prime = 43;
			int result = 1;
			result = prime * result + this.constraints.hashCode();
			result = prime * result + Arrays.deepHashCode(this.solution);

			this.hash = result;
		}
		return this.hash;

//		System.err.println("WAS CALLED HASH CODE!");
//		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		CBSNode other = (CBSNode) obj;
		if (this.constraints.equals(other.constraints) && Arrays.deepEquals(this.solution, other.solution)) {
//			System.err.println("EQUALS!");
			return true;
		}
//		return this.constraints.equals(other.constraints) && this.solution.equals(other.solution);
		return false;
	}


class Conflict implements GenericConflict {
	public int[] agentIndexes;
	public int locationX;
	public int locationY;
	public int timestamp;

	// TODO:Include boxes
	public Conflict(int agentAIndex, int agentBIndex, int locationX, int locationY, int timestamp) {
		this.agentIndexes = new int[2];
		this.agentIndexes[0] = agentAIndex;
		this.agentIndexes[1] = agentBIndex;
		this.locationX = locationX;
		this.locationY = locationY;
		this.timestamp = timestamp;

	}
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("locationX: " + locationX + "; ");
		s.append("locationY: " + locationY + "; ");
		s.append("agentB: " + agentIndexes[1] + "; ");
		s.append("agentA: " + agentIndexes[0] + "; ");
		s.append("timestamp: " + timestamp + "; ");
		return s.toString();
	}

	}
}

