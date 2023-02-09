package searchclient;

enum ActionType
{
    NoOp,
    Move,
    Push,
    Pull
}

public enum Action
{
    /*
        List of possible actions. Each action has the following parameters, 
        taken in order from left to right:
        1. The name of the action as a string. This is the string sent to the server
        when the action is executed. Note that for Pull and Push actions the syntax is
        "Push(X,Y)" and "Pull(X,Y)" with no spaces.
        2. Action type: NoOp, Move, Push or Pull (only NoOp and Move initially supported)
        3. agentRowDelta: the vertical displacement of the agent (-1,0,+1)
        4. agentColDelta: the horisontal displacement of the agent (-1,0,+1)
        5. boxRowDelta: the vertical displacement of the box (-1,0,+1)
        6. boxColDelta: the horisontal discplacement of the box (-1,0,+1) 
        Note: Origo (0,0) is in the upper left corner. So +1 in the vertical direction is down (S) 
        and +1 in the horisontal direction is right (E).
    */
    NoOp("NoOp", ActionType.NoOp, 0, 0, 0, 0),

    MoveN("Move(N)", ActionType.Move, -1, 0, 0, 0),
    MoveS("Move(S)", ActionType.Move, 1, 0, 0, 0),
    MoveE("Move(E)", ActionType.Move, 0, 1, 0, 0),
    MoveW("Move(W)", ActionType.Move, 0, -1, 0, 0),
	
	PushNN("Push(NN)", ActionType.Push, -1, 0, -1, 0),
	PushNE("Push(NE)", ActionType.Push, -1, 0, 0, 1),
	PushNW("Push(NW)", ActionType.Push, -1, 0, 0, -1),
	
	PushSS("Push(SS)", ActionType.Push, 1, 0, 1, 0),
	PushSE("Push(SE)", ActionType.Push, 1, 0, 0, 1),
	PushSW("Push(SW)", ActionType.Push, 1, 0, 0, -1),

	
	PushEN("Push(EN)", ActionType.Push, 0, 1, -1, 0),
	PushES("Push(ES)", ActionType.Push, 0, 1, 1, 0),
	PushEE("Push(EE)", ActionType.Push, 0, 1, 0, 1),
	
	PushWN("Push(WN)", ActionType.Push, 0, -1, -1, 0),
	PushWS("Push(WS)", ActionType.Push, 0, -1, 1, 0),
	PushWW("Push(WW)", ActionType.Push, 0, -1, 0, -1),
	


	PullNN("Pull(NN)", ActionType.Pull, -1, 0, -1, 0),
	PullNE("Pull(NE)", ActionType.Pull, -1, 0, 0, 1),
	PullNW("Pull(NW)", ActionType.Pull, -1, 0, 0, -1),
	
	PullSS("Pull(SS)", ActionType.Pull, 1, 0, 1, 0),
	PullSE("Pull(SE)", ActionType.Pull, 1, 0, 0, 1),
	PullSW("Pull(SW)", ActionType.Pull, 1, 0, 0, -1),
	
	PullEN("Pull(EN)", ActionType.Pull, 0, 1, -1, 0),
	PullES("Pull(ES)", ActionType.Pull, 0, 1, 1, 0),
	PullEE("Pull(EE)", ActionType.Pull, 0, 1, 0, 1),
	
	PullWN("Pull(WN)", ActionType.Pull, 0, -1, -1, 0),
	PullWS("Pull(WS)", ActionType.Pull, 0, -1, 1, 0),
	PullWW("Pull(WW)", ActionType.Pull, 0, -1, 0, -1);
	

	

    public final String name;
    public final ActionType type;
    public final int agentRowDelta; // vertical displacement of agent (-1,0,+1)
    public final int agentColDelta; // horisontal displacement of agent (-1,0,+1)
    public final int boxRowDelta; // vertical diplacement of box (-1,0,+1)
    public final int boxColDelta; // horisontal displacement of box (-1,0,+1)

    Action(String name, ActionType type, int ard, int acd, int brd, int bcd)
    {
        this.name = name;
        this.type = type;
        this.agentRowDelta = ard; 
        this.agentColDelta = acd; 
        this.boxRowDelta = brd; 
        this.boxColDelta = bcd;  
    }
}
