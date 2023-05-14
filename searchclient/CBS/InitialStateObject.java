package searchclient.CBS;

import searchclient.Color;

public class InitialStateObject {
   public  final int[] agentRowsInitial;
   public  final int[] agentColsInitial;
   public  final Color[] agentColors;
   public  final  boolean[][] wallsIntial;
   public  final char[][] boxesInitial;
   public  final char[][] goals;
   public  final Color[] boxColors;

   public InitialStateObject(int[] agentRowsInitial, int[] agentColsInitial, Color[] agentColors, boolean[][] wallsIntial, char[][] boxesInitial, Color[] boxColors, char[][] goals) {
      this.agentRowsInitial = agentRowsInitial;
      this.agentColsInitial = agentColsInitial;
      this.agentColors = agentColors;
      this.wallsIntial = wallsIntial;
      this.boxesInitial = boxesInitial;
      this.boxColors = boxColors;
      this.goals = goals;

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
