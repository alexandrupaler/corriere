package corr;

import java.util.ArrayList;

public class SATBraid {
	SATVar ss = null;
	SATVar[] tt = new SATVar[2];
	
	public SATBraid(SATVar s, SATVar t1, SATVar t2)
	{
		ss = s; tt[0] = t1; tt[1] = t2;
	}
	
	public ArrayList<int[]> getCNF()
	{
		
		ArrayList<int[]> ret = XorCnfConstruct.xorCNF(new int[]{-ss.getNumber(), tt[0].getNumber(), tt[1].getNumber()});
		
//		ArrayList<int[]> ret = new ArrayList<int[]>();
//		
//		ret.add(new int[]{ss.getNumber(),  tt[0].getNumber(), (-tt[1].getNumber())});
//		ret.add(new int[]{ss.getNumber(), (-tt[0].getNumber()), tt[1].getNumber()});
//		ret.add(new int[]{(-ss.getNumber()), tt[0].getNumber(), tt[1].getNumber()});
//		ret.add(new int[]{(-ss.getNumber()), (-tt[0].getNumber()), (-tt[1].getNumber())});

		return ret;
	}
	
	public void addTubeVar(SATVar tube)
	{
		for(int i=0;i<2;i++)
		{
			if(tt[i] != null && tt[i].getNumber().equals(tube.getNumber()))
			{
				//exista deja si nu mai tre pus
				return;
			}
			if(tt[i] == null)
			{
				System.out.println("braid : add tube " + tube.getNumber() + " at " + i);
				tt[i] = tube;
				break;
			}
		}
	}
	
	@Override
	public String toString()
	{
		return "b:" + ss.getNumber() + "|" + tt[0].getNumber() + "|" + tt[1].getNumber(); 
	}
}