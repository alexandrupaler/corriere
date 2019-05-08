package corr;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

//public class CorrSurface extends ArrayList<Integer>{
public class CorrSurface extends SATVar{
	//public int[] nodes;//fara inputuri
	//public int[] allnodes;
	public HashSet<Integer> inputs = new HashSet<Integer>();
	List<Sheet> sheets = new ArrayList<Sheet>();
	
//	public CorrSurface(int[] nodes, HashSet<Integer> allinputs)
//	{
//		this.allnodes = Arrays.copyOf(nodes, nodes.length);
//		fillInputs(allinputs);
//	}
	
	public CorrSurface(UndirectedGraph<Integer,DefaultEdge> graph, ArrayList<Integer> nodes, HashSet<Integer> allinputs)
	{
		super(graph, nodes, SATVar.SHEET);//inlocuieste allnodes
		
		Integer a[] = new Integer[nodes.size()];
		nodes.toArray(a);
//		allnodes = new int[a.length];
//		for(int i=0; i<a.length; i++)
//			allnodes[i] = a[i];
		//System.out.println("new corrs " + allnodes.length);
		fillInputs(allinputs);
	}
	
	public List<Integer> getNodesAsList()
	{
		List<Integer> ret = new ArrayList<Integer>(this.vertexSet());
		ret.removeAll(inputs);
		return ret;
	}
	
//	public Set<Integer> getAllNodesAsSet()
//	{
//		return new HashSet<Integer>(this);
//	}
	
	protected void fillInputs(HashSet<Integer> allinputs)
	{
		for(Integer n : this.vertexSet())
		{
			if(allinputs.contains(n))
			{
				inputs.add(n);
			}
		}
		
//		ArrayList<Integer> newnodes = new ArrayList<Integer>();
//		for(int n : allnodes)
//			newnodes.add(n);
//		newnodes.removeAll(inputs);
//		
//		nodes = new int[newnodes.size()];
//		for(int i=0; i<newnodes.size(); i++)
//			nodes[i] = (int)newnodes.get(i);
	}
	
//	public boolean equals(CorrSurface other)
//	{
//		if(nodes.length != other.nodes.length)
//			return false;
//		
//		for(int i=0; i<nodes.length; i++)
//			if(nodes[i] != other.nodes[i])
//				return false;
//		
//		return true;
//	}
	
	public String toString()
	{
		String s = "";
		for (Integer node : this.vertexSet())
		{
			String ss = inputs.contains(node) ? "["+node+"] " : node+" ";
			s += ss;
		}
		s += "/ " + inputs.size();
		
		return s;
	}
	
	//metoda din codul C, functiona pe segmente
	//metoda asta incerc sa o fac sa functioneze cu noduri
	public Sheet computeSheet(HashMap<Integer, Coordinate> ncoord, int startIndex)
	{
		Sheet sheet = new Sheet();
		
		boolean mademove = true;

		int pi[] = new int[5];

		int nextreshape = -1;
		
		//construiesc o lista cu toate coordonatele existente in ciclu
		//si de aici in colo numai cu coordonate voi lucra
		List<Coordinate> stack = new ArrayList<Coordinate>();
//		for(int x : nodes)
//			stack.add(new Coordinate(ncoord.get(x)));
		DefaultEdge edg = this.edgeSet().iterator().next();
		
		Integer start = this.getEdgeSource(edg);
		
		HashSet<Integer> visited = new HashSet<Integer>();
		while(!this.vertexSet().equals(visited))
		{
			visited.add(start);
			
			if(!inputs.contains(start))
				stack.add(new Coordinate(ncoord.get(start)));
			
			for(DefaultEdge ed : this.edgesOf(start))
			{
				Integer[] nodes = new Integer[]{this.getEdgeSource(ed), this.getEdgeTarget(ed)};
				for(Integer nnodes : nodes)
				{
					if(!visited.contains(nnodes))
					{
						start = nnodes;//din cauza ca e ciclu o singura data intra aici si nu am nevoie de break?
					}
				}
			}
		}
		
		while(mademove)
		{
			mademove = false;
			//vector<startstop> newstack;
			//vector<int> newtypes;
			
			List<Coordinate> newstack = new ArrayList<Coordinate>();
			for(Coordinate c : stack)
				newstack.add(new Coordinate(c));
//			newstack.add(newstack.size() - 1, stack.get(0));
//			newstack.add(newstack.size() - 1, stack.get(1));

			//newstack.push_back(stack[0]);newtypes.push_back(types[0]);
			//newstack.push_back(stack[1]);newtypes.push_back(types[1]);

			int stacksize = newstack.size();
			for(int i=0; i<stacksize && !mademove; i++)
			{
				pi[0] = (i - 0 + newstack.size()) % newstack.size();
				pi[1] = (i - 1 + newstack.size()) % newstack.size();
				pi[2] = (i - 2 + newstack.size()) % newstack.size();
				pi[3] = (i - 3 + newstack.size()) % newstack.size();
				pi[4] = (i - 4 + 2*newstack.size()) % newstack.size();
				
				//astea sunt directiile edge-urilor
				int types[] = new int[4];
				for(int ii=0; ii<4; ii++)
					types[ii] = newstack.get(pi[(ii + 0)]).getDirection(newstack.get(pi[(ii+1)]));


				if(!mademove && types[0] == types[1])//current segment of same type with previous...add them together
				{
					mademove = true;

					newstack.remove(pi[1]);
				}
				else if(!mademove && ((types[0] ^ types[1])) == 1)//of opposite type - last bit differs
				{
					//printf("# reduce\n");
					mademove = true;
					
					long len[] = new long[2];//ar trebui o metoda care sa calculeze lungimea
					len[0] = Math.abs(stack.get(pi[0]).getSegmentLength(stack.get(pi[1])));
					len[1] = Math.abs(stack.get(pi[1]).getSegmentLength(stack.get(pi[2])));
										
					ArrayList<Coordinate> torem = new ArrayList<Coordinate>();
					if(len[0] == len[1])
						torem.add(newstack.get(pi[0]));
					torem.add(newstack.get(pi[1]));
					for(Coordinate trc : torem)
						newstack.remove(trc);
				}
				else if(!mademove && (types[0] ^ types[2]) == 1)//of opposite type - last bit differs
				{
					mademove = true; 
					//added later
					nextreshape = -1;
	
					Coordinate off1 = newstack.get(pi[3]).computeOffset(newstack.get(pi[2]));
					Coordinate off2 = newstack.get(pi[0]).computeOffset(newstack.get(pi[1]));
					Coordinate off = new Coordinate();
					if(Math.abs(off1.getSegmentLength(off)) < Math.abs(off2.getSegmentLength(off)))
						off = off1;
					else
						off = off2;
					
					Rectangle subsheet = new Rectangle(newstack.get(pi[2]), newstack.get(pi[1]), off);
					sheet.add(subsheet);
					newstack.get(pi[2]).addOffset(off);
					newstack.get(pi[1]).addOffset(off);

					ArrayList<Coordinate> torem = new ArrayList<Coordinate>();
					if(newstack.get(pi[2]).equals(newstack.get(pi[3])))
						torem.add(newstack.get(pi[2]));
					if(newstack.get(pi[1]).equals(newstack.get(pi[0])))
						torem.add(newstack.get(pi[1]));
					
					for(Coordinate trc : torem)
					{
						newstack.remove(trc);//scoate de mai multe ori. problem?
					}
				}
			}

			//segmentul din mijloc vine mutat
			//trebuie si segmentele redimensionate in functie de care e mai lung
			//daca segmentele de pe margine au aceeasi lungime atunci...
			//1.daca ala dinainte e de acelasi tip cu segmentul din mijloc adun
			//2.daca ala din fata e de acelasi tip cu segmentul din mijloc adun

			//readauga la stack primele doua elemente pentru a simula o lista circulara
			//stack.swap(newstack);
			//types.swap(newtypes);
			stack.clear(); stack.addAll(newstack);
			//types este calculat de fiecare data din nou
			
//			printf("new stack size:%d\n", stack.size());
			//System.out.println(stack.size());
			if(stack.size() <= 2)
			{
				mademove = false;
				break;
			}

			/*asta e un caz particular cand se formeaza un cerc*/
			if(stack.size() >= 3 && !mademove)
			{
				nextreshape++;
				//System.out.println("reshape");
				
				int rshpcoords[] = new int[3];
				for(int i=0; i<3; i++)
				{
					rshpcoords[i] = (nextreshape + i + startIndex) % stack.size();
				}
				
				//coordonata de la rshpcoords[1] trebuie mutata
				Coordinate f1 = stack.get(rshpcoords[0]).computeOffset(stack.get(rshpcoords[1]));
				Coordinate f2 = stack.get(rshpcoords[2]).computeOffset(stack.get(rshpcoords[1]));
				stack.get(rshpcoords[1]).addOffset(f1);
				stack.get(rshpcoords[1]).addOffset(f2);
				
				Rectangle reshapesubsheet = new Rectangle(stack.get(rshpcoords[2]), stack.get(rshpcoords[0]));
				sheet.add(reshapesubsheet);

				mademove = true;
			}
			//System.out.println(sheet);
		}
		
		
		return sheet;
	}
	
	public void computeSheets(HashMap<Integer, Coordinate> ncoord)
	{
		//for(int i=0; i<nodes.length; i++)
		{
			//System.out.print("sheet with startindex " + i + " :");

			//Sheet sh = computeSheet(ncoord, i);
			Sheet sh = computeSheet(ncoord, 0);
			if(sh.size() > 0)
			{
				boolean empty = true;
				for(Sheet s : sheets)
				{
					if(s.equals(sh))
						empty = false;
				}
				if(empty)
				{
					sheets.add(sh);
					//System.out.println(sh);
				}
				//else
					//System.out.println("exists");
					
			}
		}
		//System.out.println("has " + sheets.size() + "sheets");
	
	}
}