package corr;

import java.util.ArrayList;
import java.util.HashSet;

public class SATInput{
	public static final boolean ISXOR = true;
	public static final boolean ISEQUIV = false;
	
	//SATVar[] tt = new SATVar[2];
	ArrayList<SATVar> tt = new ArrayList<SATVar>();
	Integer responsibleFor = null;
	
	//public SATInput(Integer input, SATVar t1, SATVar t2)
	public SATInput(Integer input)//, SATVar t1, SATVar t2)
	{
//		tt.add(t1);
//		tt.add(t2);
		//tt[0] = t1;tt[1] = t2;
		responsibleFor = input;
	}
	
	public ArrayList<int[]> getCNF(boolean isXor)
	{
//		ArrayList<int[]> ret = new ArrayList<int[]>();
		
		if(tt.size() == 1)
		{
			ArrayList<int[]> ret = new ArrayList<int[]>();
//			ret.add(new int[]{tt.get(0).getNumber()});
//			ret.add(new int[]{-tt.get(0).getNumber()});
			return ret;
		}
		
		int[] terms = new int[tt.size()];
		int pos = 0;
		for(SATVar v : tt)
			terms[pos++] = v.getNumber();
		if(!isXor)
			terms[0] *= -1;
		
		ArrayList<int[]> ret = XorCnfConstruct.xorCNF(terms);
		
//		ArrayList<int[]> ret = XorCnfConstruct.xorCNF(new int[]{tt.get(0).getNumber(), tt.get(1).getNumber()*(isXor?1:-1)});

//		if(isXor)
//		{
//			ret.add(new int[]{tt[0].getNumber() ,(tt[1].getNumber())});
//			ret.add(new int[]{(-tt[0].getNumber()), -tt[1].getNumber()});
//		}
//		else
//		{
//			//neverificat
//			ret.add(new int[]{tt[0].getNumber(), -tt[1].getNumber()});
//			ret.add(new int[]{(-tt[0].getNumber()), (tt[1].getNumber())});
//		}
		
//		if(isXor)
//		{
//			ret.add(tt[0].getNumber() + " " + (-tt[1].getNumber()) + " 0");
//			ret.add((-tt[0].getNumber()) + " " + tt[1].getNumber() + " 0");
//		}
//		else
//		{
//			//neverificat
//			ret.add(tt[0].getNumber() + " " + tt[1].getNumber() + " 0");
//			ret.add((-tt[0].getNumber()) + " " + (-tt[1].getNumber()) + " 0");
//		}
		return ret;
	}
	
	public void addSATVar(SATVar tubeorsheet)
	{
		//exista variabila?
		//as putea cauta dupa grafuri, dar deocamdata las asa...?
		for(int i=0;i<tt.size();i++)
		{
			if(/*tt[i] != null && */tt.get(i).getNumber().equals(tubeorsheet.getNumber()))
			{
				//exista deja si nu mai tre pus
				return;
			}
		}
		//if(tt[i] == null)
		{
			tt.add(tubeorsheet);
			System.out.println("input " + getResponsibleInput() + ": add tube " + tubeorsheet.isTube() + " " + tubeorsheet.getNumber() + " at " + (tt.size()-1));
//			break;
		}

	}
	
	public Integer getResponsibleInput()
	{
		//doua tuburi pot avea inputuri si braiduri in comun
		return responsibleFor;
	}
	
	@Override
	public String toString()
	{
		String ret = "i:";
		for(SATVar v : tt)
		{
			ret += v.getNumber() + "|";
		}
		return ret;
	}
}