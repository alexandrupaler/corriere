package corr;

import java.util.ArrayList;

public class XorCnfConstruct {
	
	public static ArrayList<int[]> xorCNF(int[] terms)
	{
		ArrayList<int[]> ret = new ArrayList<int[]>();
		//o pula
		int max = 1 << terms.length;
		for(int i=0; i<max; i++)
		{
			if(hasEvenNrOnes(i, terms.length))
			{
				ret.add(getClause(i, terms));
			}
		}
		
		return ret;
	}

	private static int[] getClause(int nr, int[] terms) {
		int[] clause = new int[terms.length];
		for(int i=0; i<terms.length; i++)
		{
			//0: pastrez termenul
			//1: il neg
			clause[i] = terms[i] * (1-2*(nr&1));
			nr = nr >> 1;
		}
		return clause;
	}

	private static boolean hasEvenNrOnes(int nr, int len) {
		int nrOnes = 0;
		
		for(int i=0; i<len; i++)
		{
			nrOnes += (nr&1);
			nr = nr >> 1;
		}
		
		return nrOnes%2 == 0;
	}

}
