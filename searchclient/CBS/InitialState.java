package searchclient.CBS;

import searchclient.Color;

public class  InitialState {

	/*
	 * The agent rows, columns, and colors are indexed by the agent number. For
	 * example, this.agentRows[0] is the row location of agent '0'.
	 */
	private  static int[] agentRowsInitial;
	private  static int[] agentColsInitial;
	private  static Color[] agentColors;

	/*
	 * The walls, boxes, and goals arrays are indexed from the top-left of the
	 * level, row-major order (row, col). Col 0 Col 1 Col 2 Col 3 Row 0: (0,0) (0,1)
	 * (0,2) (0,3) ... Row 1: (1,0) (1,1) (1,2) (1,3) ... Row 2: (2,0) (2,1) (2,2)
	 * (2,3) ... ...
	 * 
	 * For example, this.walls[2] is an array of booleans for the third row.
	 * this.walls[row][col] is true if there's a wall at (row, col).
	 * 
	 * this.boxes and this.char are two-dimensional arrays of chars.
	 * this.boxes[1][2]='A' means there is an A box at (1,2). If there is no box at
	 * (1,2), we have this.boxes[1][2]=0 (null character). Simiarly for goals.
	 * 
	 */
	private  static  boolean[][] wallsIntial;
	private  static char[][] boxesInitial;
	private  static char[][] goals;

	/*
	 * The box colors are indexed alphabetically. So this.boxColors[0] is the color
	 * of A boxes, this.boxColor[1] is the color of B boxes, etc.
	 */
	private  static Color[] boxColors;

   // public ConstraintInitialState() {


   // }
   public InitialState(int[] agentRowsInitial, int[] agentColsInitial, Color[] agentColors, boolean[][] wallsIntial, char[][] boxesInitial, Color[] boxColors, char[][] goals) {
      InitialState.agentRowsInitial = agentRowsInitial;
      InitialState.agentColsInitial = agentColsInitial;
      InitialState.agentColors = agentColors;
      InitialState.wallsIntial = wallsIntial;
      InitialState.boxesInitial = boxesInitial;
      InitialState.boxColors = boxColors;
      InitialState.goals = goals;

   }
	public InitialState() {};

   public InitialStateObject getInitialState() {
      return new InitialStateObject(agentRowsInitial, agentColsInitial, agentColors, wallsIntial, boxesInitial, boxColors, goals);
   }


	@Override
	public String toString()
	{
		 StringBuilder s = new StringBuilder();
		 for (int row = 0; row < this.wallsIntial.length; row++)
		 {
			  for (int col = 0; col < this.wallsIntial[row].length; col++)
			  {
					if (this.boxesInitial[row][col] > 0)
					{
						 s.append(this.boxesInitial[row][col]);
					}
					else if (this.wallsIntial[row][col])
					{
						 s.append("+");
					}
					else if (this.agentAt(row, col) != 0)
					{
						 s.append(this.agentAt(row, col));
					}
					else
					{
						 s.append(" ");
					}
			  }
			  s.append("\n");
		 }
		 return s.toString();
	}


	private char agentAt(int row, int col)
	{
		 for (int i = 0; i < this.agentRowsInitial.length; i++)
		 {
			  if (this.agentRowsInitial[i] == row && this.agentColsInitial[i] == col)
			  {
					return (char) ('0' + i);
			  }
		 }
		 return 0;
	}


}

