package corr;

import java.util.ArrayList;
import java.util.HashSet;

public class SATJunction {
	SATVar[] ss = new SATVar[3];
	
	public SATJunction(SATVar s1, SATVar s2, SATVar s3)
	{
		ss[0] = s1; ss[1] = s2; ss[2] = s3;
	}
	
	public ArrayList<int[]> getCNF()
	{
		//formula nu e inca verificata
		ArrayList<int[]> ret = new ArrayList<int[]>();
		
		//ArrayList<int[]> ret = XorCnfConstruct.xorCNF(new int[]{ss[0].getNumber(), ss[1].getNumber(), ss[2].getNumber()});
		
		//(a xnor (b xor c)) and (a xnor (d xor e)) and (b xnor (d xor f)) and (c xnor (e xor f))
		//(a xor (b xor c)) and (a xor (d and e)) and (b xor (d and f)) and (c xor (e and f))
		//not (a and b) xor not (b and c) xor not (a and c)
		ret.add(new int[]{-ss[0].getNumber(), -ss[1].getNumber()});
		ret.add(new int[]{-ss[0].getNumber(), -ss[2].getNumber()});
		ret.add(new int[]{-ss[1].getNumber(), -ss[2].getNumber()});
		
//		ret.add(new int[]{(-ss[0].getNumber()), ss[1].getNumber() , (-ss[2].getNumber())});
//		ret.add(new int[]{(-ss[0].getNumber()), (-ss[1].getNumber()), ss[2].getNumber()});
//		ret.add(new int[]{ss[0].getNumber(),ss[1].getNumber(), ss[2].getNumber()});
//		ret.add(new int[]{ss[0].getNumber(), (-ss[1].getNumber()), (-ss[2].getNumber())});

		
//		ret.add((-ss[0].getNumber()) + " " + ss[1].getNumber() + " " + (-ss[2].getNumber()) + " 0");
//		ret.add((-ss[0].getNumber()) + " " + (-ss[1].getNumber()) + " " + ss[2].getNumber() + " 0");
//		ret.add(ss[0].getNumber() + " " + ss[1].getNumber() + " " + ss[2].getNumber() + " 0");
//		ret.add(ss[0].getNumber() + " " + (-ss[1].getNumber()) + " " + (-ss[2].getNumber()) + " 0");
		return ret;
	}
	
	@Override
	public String toString()
	{
		return "j: " + ss[0].getNumber() + "|" + ss[1].getNumber() + "|" + ss[2].getNumber(); 
	}
	
	@Override
	public boolean equals(Object o)
	{
		SATJunction other = (SATJunction)o;
		
		HashSet<Integer> oset = new HashSet<Integer>();
		for(int i=0; i<3; i++)
			oset.add(other.ss[i].getNumber());
		
		HashSet<Integer> tset = new HashSet<Integer>();
		for(int i=0; i<3; i++)
			tset.add(ss[i].getNumber());
		
		return oset.equals(tset);
	}
}
