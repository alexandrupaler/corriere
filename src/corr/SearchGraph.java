package corr;
import java.util.AbstractQueue;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.cycle.PatonCycleBase;
import org.jgrapht.alg.cycle.UndirectedCycleBase;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.GraphUnion;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.Subgraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;


public class SearchGraph
{
	protected HashMap<Integer, SNode> nodes = new HashMap<Integer, SNode>();
	
	//protected HashMap<String, SSheet> sheets = new HashMap<String, SSheet>();
	protected List<SSheet> sheets = new ArrayList<SSheet>();
	
	protected HashMap<String, SInput> inputs = new HashMap<String, SInput>();
	//protected HashMap<Integer, SInput> inputs = new HashMap<Integer, SInput>();
	protected List<SContact> scontacts = new ArrayList<SContact>();
	protected List<SContact> icontacts = new ArrayList<SContact>();
	
	protected List<SNode> sheethelps = new ArrayList<SNode>();
	
	public ArrayList<CorrSurface> cycles = new ArrayList<CorrSurface>();

	protected static int nrHelpNode = -1;
	
//	public static ArrayList<String> CNF = new ArrayList<String>();
//	public static HashSet<SATVar> vars = new HashSet<SATVar>();
	
	public HashSet<SATJunction> SATJunctions = new HashSet<SATJunction>();
	public HashSet<SATBraid> SATBraids = new HashSet<SATBraid>();
//	public HashSet<SATInput> SATInputs = new HashSet<SATInput>();
	
	private UndirectedGraph<Integer, DefaultEdge> theGraph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
	
	public ArrayList<SATVar> tubeVars = new ArrayList<SATVar>();
	//public static ArrayList<SATVar> sheetVars = new ArrayList<SATVar>();
	private HashSet<Integer> inputNodes = new HashSet<Integer>();
	private HashSet<Integer> braidNodes = new HashSet<Integer>();
	
	private static int braidNodeNumber = -1;
	private boolean isPrimal = true;
	protected HashMap<Integer, SATBraid> braidNodesToBraidVars = new HashMap<Integer, SATBraid>();
	protected HashMap<Integer, SATInput> inputNodesToTubeVars = new HashMap<Integer, SATInput>();
	protected HashMap<Integer, SATInput> inputNodesToSheetVars = new  HashMap<Integer, SATInput>();

	private int separatorNumber = 0;

	protected ArrayList<int[]> traversedEdges = new ArrayList<int[]>();

	
	private int nextBraidNodeNumber()
	{
		return SearchGraph.braidNodeNumber--;
	}
	
	public void computeSheets(HashMap<Integer, Coordinate> ncoord)
	{
		for(CorrSurface cy : cycles)
		{
			cy.computeSheets(ncoord);
		}
	}
	
	public ArrayList<Integer> findCycle(Integer node, Integer target, HashSet<Integer> nono, ArrayList<Integer> visited)
	{
		ArrayList<Integer> myvisit = new ArrayList<Integer>(visited);
		ArrayList<Integer> neighbours = new ArrayList<Integer>(Graphs.neighborListOf(theGraph, node));
		
//		if((neighbours.size() == 1 || visited.size() > 0) && target.equals(node))
		if(target.equals(node))
		{
			myvisit.add(node);
			System.out.print("gasit gasit");
			return myvisit;
		}
		
		myvisit.add(node);
		for(Integer ngh : neighbours)
		{
			if(!myvisit.contains(ngh) && !nono.contains(ngh))
			{
				ArrayList<Integer> l = findCycle(ngh, target, nono, myvisit);
				if(l != null)
					return l;
			}
		}
		
		return null;
	}
	
	public HashSet<Integer> findTree(Integer firstNode,
			HashSet<Integer> targetNodes, 
			ArrayList<int[]> segments)
	{
		HashSet<Integer> visited = new HashSet<Integer>();
		ArrayDeque<Integer> toVisit = new ArrayDeque<Integer>();
		
		/*
		 * It is assumed that firstNode is not a target
		 * The node from which the search of a tree starts is 
		 * in the case of tubes anyway a forbidden node, and the firstNode
		 * is one of its neighbours
		 * If firstNode is in the targetNodes, the entire tree search is useless
		 * and a single segment is returned
		 */
		
		if(targetNodes!=null && targetNodes.contains(firstNode))
		{
			visited.add(firstNode);
			
			/*
			 * The segs collection is not updated, 
			 * because the initial node is not known
			 */
			return visited;
		}
	
		
		/*
		 * The node to start from
		 */
		toVisit.add(firstNode);
		
		while(toVisit.size() > 0)
		{
			Integer current = toVisit.pop();
			
			/*
			 * Add the node to the visited
			 */
			visited.add(current);
			
			//get the neighbours of current
			ArrayList<Integer> neighbours = new ArrayList<Integer>(Graphs.neighborListOf(theGraph, current));
			
			for(Integer ngh : neighbours)
			{
				/*
				 * Do not consider already visited neighbours
				 */
				if(visited.contains(ngh))
					continue;
				
				/*
				 * Add the segments from node to its unvisited neighbours
				 */
				if(segments != null)
					segments.add(new int[]{current, ngh});
				
				/*
				 * Add the ngh to the list to visit
				 * Reached a target. Do not continue from this node.
				 */
				if(targetNodes == null)
				{
					/*
					 * No targets to reach. Classic traversal.
					 */
					toVisit.add(ngh);
				}
				else
				{
					if(targetNodes.contains(ngh))
					{
						/*
						 * It's a target
						 * mark it visited right now, because it will not be processed
						 */
						visited.add(ngh);
					}
					else
					{
						/*
						 * It's not a target
						 */
						toVisit.add(ngh);
					}
				}
			}
		}
		
		return visited;
		
//		
////		if(visited.size() == 1)
////		{
////			//din nodul de plecare doar noduri normale
////			neighbours.removeAll(target);
////		}
//		
//		//visited trebuie sa fie mai mare decat 1 deoarece
//		//in cazul tuburilor pot porni chiar dintr-un braidnode
//		//daca un nod nu are vecini atunci e inutila conditia de sus
//		if(segs== null && target!=null && target.contains(node))
//		{
//			visited.add(node);
////			System.out.println("tree tree");
//			return visited;
//		}
//		
//		visited.add(node);
////		//daca primul este din target atunci sa nu se duca in alt target
////		boolean doNotGoIntoAnotherTargetDirectly = false;
////		if(target.contains(node))
////		{
////			doNotGoIntoAnotherTargetDirectly = true;
////		}
//		for(Integer ngh : neighbours)
//		{
//			if(!visited.contains(ngh))
//			{
////				if(doNotGoIntoAnotherTargetDirectly && target.contains(ngh))
////					continue;
//				findTree(ngh, node, target, visited, segs);
//				if(segs!=null)
//				{
//					segs.add(new int[]{node, ngh});//aici practic e un ciclu?
//				}
//			}
//			else if(segs != null)
//			{
//				if(!ngh.equals(prev))
//				{
//					segs.add(new int[]{node, ngh});//aici practic e un ciclu?
//				}
//			}
//				
//		}
//		
//		return visited;
	}
	
	public void addSheetNode(Integer n1, Integer n2, Coordinate sect, Set<Integer> sheetinputs)
	{
		SNode node1 = nodes.get(n1);
		SNode node2 = nodes.get(n2);
		addSheetNode(node1, node2, sect, sheetinputs);
	}
	
	//intoarce nodul de help care a fost nevoie a fi introdus pentru sheetul care e parametru
	public void addSheetNode(SNode node1, SNode node2, Coordinate sect, Set<Integer> sheetinputs)
	{
		SNode helpNode = null;
		System.out.println("add sheet " + sheetinputs);
		
		// exista deja sheetul adaugat la graful asta?
		String key = sheetinputs.toString();
		//SSheet sh = sheets.get(key);
		SSheet sh = null;
		for(SSheet ss : sheets)
		{
			if(ss.inputs.equals(sheetinputs))
			{
				sh = ss;
				break;
			}
		}
		if(sh == null)
		{
			sh = new SSheet(sheetinputs);
			//sheets.put(key, sh);
			sheets.add(sh);
		}
			
		//daca exista vreun input la coordonata asta
		Coordinate newsect = new Coordinate(sect);
		System.out.println("INPUTS " + inputs);
		if(inputs.keySet().contains(sect.toString()))
		{
			//int axis = node1.co.getDirection(node2.co) / 2;
			//newsect.coord[axis]+=1;//il fac sa nu fie chiar pe input/injection
			//intersectiile sunt +2
			
			//aici iau un input ca vecin? - nu e bine. acuma sunt doua helpnodes pentru fiecare input
			//ia unul dintre helpnodes? - mai degraba ia pe cel cu indexul par in loc de node1
			//dar de unde stiu eu care e cel potrivit fata de node2?
			//aici mai trebuie lucrat
			//il iau pe ala din dreapta?
			for(Integer helpn : nodes.keySet())
			{
				if(nodes.get(helpn).co.equals(newsect) && (Math.abs(helpn)%2 == 0))
				{
					node1 = nodes.get(helpn);
				}
			}
			//node1 = inputs.get(sect.toString());
		}
		
		//acuma se pune intrebarea, de exista deja vreun contact la coordonata asta
		//deocamdata nu verific asta
		SNode ngh1 = node1;
		SNode ngh2 = node2;
		SContact shc = sh.createContact(newsect);
		for(SContact ct : scontacts)
		{
			if(ct.co.equals(newsect))
			{
				//exista deja un contact
				//leaga contactul nou in stanga la parintele celui vechi
				//ia vecinul din dreapta al contactului vechi
				
				shc.chainStart = ct.chainStart;
				ct.chain = shc;
				
				//ngh1 = ct.parent;//il leg la sheetul celui dinainte
				//ngh2 = ct.ngh.get(1);//vechi
				ngh1 = ct.ngh.get(1);
				
				ct.openAllContacts();
				
				//nu stiu ce era inainte, dar asta inseamna ca exista deja un contact pentru coordonata asta
				//atunci baga un helpnode
				
				helpNode = new SNode(newsect);
				helpNode.number = SearchGraph.nrHelpNode--;
				
				//la cel existent, scoate vecinul din dreapta
				//adauga helpNode
				//ct.removeNeighbor(ngh2);
				//ct.addNeighbor(helpNode);
				
				//aici intervine schimbarea, dupa ce cele doua linii de sus comentate
				ngh2.removeNeighbor(ngh1);
				ngh1.removeNeighbor(ngh2);
				ngh2.addNeighbor(helpNode);
				helpNode.addNeighbor(ngh2);

				//la cel nou, adauga in stanga helpNode
				//la cel nou, adauga in dreapta ngh2
				//shc.addNeighbor(helpNode);
				//shc.addNeighbor(ngh2);
				shc.addNeighbor(ngh1);
				shc.addNeighbor(helpNode);
				System.out.println("ttt" + shc.ngh);
				
				//shc.setContactMode(0);
				//ct.setContactMode(0);
				
				break;
			}
		}
		
		//din cauza ca modific aici, trebuie sa modific si sus....???
		if(shc.chainStart == null)
		{
			System.out.println("first " + newsect);
			//there was no initial contact
			//this is the first contact on the segment - all contacts will share the same coordinate
			shc.chainStart = shc;
			
			//m-am gandit ca e mai bine sa adaug doua noduri de help aici
			//asa pot obtine si coordonatele intersectiei..ar trebui sa modific addHelpNodes sa nu mai fie numai pentru inputuri
			//deci...
			
			SNode help1 = new SNode(newsect);
			SNode help2 = new SNode(newsect);
			help1.number = SearchGraph.nrHelpNode--;
			help2.number = SearchGraph.nrHelpNode--;
			
//			System.out.println("a---> ngh1 " + ngh1 + " ===" + ngh1.ngh);
//			System.out.println("a---> ngh2 " + ngh2 + " ===" +  ngh2.ngh);
			ngh1.removeNeighbor(ngh2);
			ngh2.removeNeighbor(ngh1);
//			System.out.println("b---> ngh1 " + ngh1.ngh);
//			System.out.println("b---> ngh2 " + ngh2.ngh);

			
			
			ngh1.addNeighbor(help1);
			ngh2.addNeighbor(help2);
			
//			System.out.println("c---> ngh1 " + ngh1.ngh);
//			System.out.println("c---> ngh2 " + ngh2.ngh);
			
			help1.addNeighbor(ngh1);
			help2.addNeighbor(ngh2);
			
//			System.out.println("h---> h1 " + help1 + " " + help1.ngh);
//			System.out.println("h---> h2 " + help2 + " " + help2.ngh);
			
//			shc.addNeighbor(ngh1);
//			shc.addNeighbor(ngh2);
			
			shc.addNeighbor(help1);
			shc.addNeighbor(help2);
			
			//desi mai jos e un if helpNode != null...ala se refera numai la cazul in care este a doua suprafata
			//chiar ar trebuie sa curat asta
			sheethelps.add(help1);
			sheethelps.add(help2);
			
			//shc.setContactMode(0);
		}
		
		scontacts.add(shc);
		
		if(helpNode != null)
		{
			sheethelps.add(helpNode);
		}
	}
	
	public void mergeSheetHelpNodes()
	{
		for(SNode hn : sheethelps)
		{
			//nodes.put(nrHelpNode--, hn);
			//hn.number = nrHelpNode;
			nodes.put(hn.number, hn);
			//System.out.println("m " + hn + "--->" + hn.ngh);
		}
		sheethelps.clear();
	}
	
	public void setSheetModesOnZero()
	{
		for(SContact ic : icontacts)
			ic.setContactMode(0);
		
		for(SContact is : scontacts)
			is.setContactMode(0);
	}
	
	public SearchGraph(Set<Integer> vertices, HashMap<Integer, Coordinate> coords, int[][] biggraph, HashSet<Integer> allinputs)
	{
	    constructTheGraph(vertices, biggraph);
	    
	    selectInputNodes(allinputs);
	    
	    computeIsPrimal(coords);
	    
	    insertSeparators();
	    
	    constructJunctionsAndSheets(allinputs);
	    
	    removeSeparators();
	    
	    computeSheets(coords);
	    
//		calculateTree();
		
		findTree(theGraph.vertexSet().iterator().next(), 
				null /*target*/, 
				traversedEdges /*edges*/);
	}

	private void removeSeparators() {
		for(int i=this.separatorNumber; i<0; i++)
		{
			List<Integer> ngh = Graphs.neighborListOf(theGraph, i);
			for(Integer n : ngh)
			{
				theGraph.removeEdge(i, n);
			}
			theGraph.removeVertex(i);
			theGraph.addEdge(ngh.get(0), ngh.get(1));//trebuie sa fie intotdeauna 2
			
			for(CorrSurface cycle : cycles)
			{
				if(cycle.removeVertex(i))
				{
					cycle.addEdge(ngh.get(0), ngh.get(1));//trebuie sa fie intotdeauna 2
				}
			}
		}
		
//		for(CorrSurface cycle : cycles)
//		{
//			System.out.print("---->");System.out.println(cycle);
//		}
		
	}

	private void insertSeparators() {
		//edges that connect junctions are split
		
		ArrayList<Integer> junctions = getJunctions2();
		
		Set<DefaultEdge> set = theGraph.edgeSet();
		HashSet<int[]> torem = new HashSet<int[]>();
		HashSet<int[]> toadd = new HashSet<int[]>();
		for(DefaultEdge e : set)
		{
			Integer e1 = theGraph.getEdgeSource(e);
			Integer e2 = theGraph.getEdgeTarget(e);
			if(junctions.contains(e1) && junctions.contains(e2))
			{
				this.separatorNumber--;
				theGraph.addVertex(this.separatorNumber);
				toadd.add(new int[]{e1, this.separatorNumber});
				toadd.add(new int[]{e2, this.separatorNumber});
				torem.add(new int[]{e1, e2});
			}
		}
		for(int[] tr : torem)
			theGraph.removeEdge(tr[0], tr[1]);
		for(int[] ta : toadd)
			theGraph.addEdge(ta[0], ta[1]);
	}

	private void computeIsPrimal(HashMap<Integer, Coordinate> coords) {
		Iterator<Integer> iterator = theGraph.vertexSet().iterator();
	    Integer randomnode = iterator.next();
	    	
	    while(inputNodes.contains(randomnode))
	    	randomnode = iterator.next();
	    
	    Coordinate randomcoord = coords.get(randomnode);
	    int eq = 0;
	    for(int i=0; i<3; i++)
	    	eq += (Math.abs(randomcoord.coord[i])%2);
	    	//eq += (1 - Math.abs(randomcoord.coord[i])%2);
	    
	    this.isPrimal = (eq >= 2);
	}

	private void selectInputNodes(HashSet<Integer> allinputs)
	{
		for(Integer x : theGraph.vertexSet())
	    {
	    	if(allinputs.contains(x))
	    	{
	    		inputNodes.add(x);
	    		//SATInput si = new SATInput(x, null, null);
	    		//SATInputs.add(si);
	    		//inputNodesToInputVars.put(x, si);
	    		
	    		SATInput tubeInput = new SATInput(x);
	    		inputNodesToTubeVars.put(x, tubeInput);
	    		
	    		SATInput sheetInput = new SATInput(x);
	    		inputNodesToSheetVars.put(x, sheetInput);
	    	}
	    }
	}

	private void constructJunctionsAndSheets(HashSet<Integer> allinputs)
	{
		//nu mai stiu de ce e hashmap...
		//HashMap<ArrayList<Integer>, Integer> cycl = new HashMap<ArrayList<Integer>, Integer>();
		//retine care configuratii de la un junction au rezultat in ciclu
	    HashMap<Integer, ArrayList<HashSet<Integer>>> cycexist = new HashMap<Integer, ArrayList<HashSet<Integer>>>();
	    //pentru fiecare junction(integer) exista o multime, de multimi init
	    
	    ArrayList<Integer> junctions =  getJunctions2();
	    System.out.println("JUNCTIONS " + junctions.size());
	    
	    if(junctions.size() == 0)
	    {
	    	//un cerc simplu
	    	//SATVar s = new SATVar(new HashSet<Integer>(theGraph.vertexSet()), SATVar.SHEET);
	    	ArrayList<Integer> path = new ArrayList<Integer>();
	    	
	    	//ia un nod oarecare
	    	Integer node = theGraph.vertexSet().iterator().next();
	    	Iterator<Integer> iter = Graphs.neighborListOf(theGraph, node).iterator();
	    	Integer ngh1 = iter.next();
	    	Integer ngh2 = iter.next();
	    	path.add(node);
	    	path = findCycle(ngh1, ngh2, new HashSet<Integer>(), path);
	    	//path.remove(path.size() - 1);//ultimul e egal cu primul, vezi sus, iar findCycle adauga destinatia la path
	    	
	    	constructSheet(allinputs, path);
	    	//sheetVars.add(s);
	    }
	    
	    //asta nu se mai apeleaza
	    for(Integer junc : junctions)
	    {
	    	cycexist.put(junc, new ArrayList<HashSet<Integer>>());
	    	
	    	System.out.println("J " + junc);
	    	
//	    	int numberOfComputedCyclesInJunction = 0;
	    	
	    	List<Integer> ngh = Graphs.neighborListOf(theGraph, junc);
	    	for(int i=0; i<ngh.size()-1; i++)
	    	{
	    		for(int j=i+1; j<ngh.size(); j++)
	    		{
	    			//perechea i,j
	    			//cauta un ciclu care sa contina nodurile i,j,junc
	    			HashSet<Integer> init = new HashSet<Integer>();
	    			init.add(ngh.get(i));
	    			init.add(ngh.get(j));
	    			System.out.print(init);
	    			
	    			HashSet<Integer> nono = new HashSet<Integer>(ngh);
    		    	nono.removeAll(init);
    		    	nono.remove(junc);
	    			
	    			boolean found = false;
	    			found = (findCorrSurfaceGivenJunction(init, nono) != null);
	    			if(found)
	    			{
	    				//sheetVars.add(new SATVar(init, SATVar.SHEET));
	    				cycexist.get(junc).add(init);
	    				System.out.println(" ...");
//	    				//aici devine ambiguu...pentru ca am doua variabile pentru aceeasi suprafata, 
//	    				//pe cand eu vroiam doar sa marchez ca pentru o junction conf exista un ciclu
//	    				numberOfComputedCyclesInJunction++;
	    			}
	    			else 
	    			{
		    			//daca nu exista cauta-l, tre sa existe	    			
	    				ArrayList<Integer> path = new ArrayList<Integer>();
	    				path.add(junc);
	    				    		    	
	    		    	
	    		    	
	    				path = findCycle(ngh.get(j), ngh.get(i), nono, path);
	    				if(path == null)
	    				{
	    					// nu exita ciclu, bridge simplu
	    					System.out.println("nu ciclu, bridge simplu?");
	    				}
	    				else
	    				{
	    					//mark the path in the set cycl
	    					System.out.println("??" + path);
	    					//cycl.put(path, cycl.size());
	    					cycexist.get(junc).add(init);
	    					//sheetVars.add(new SATVar(init, SATVar.SHEET));
	    					
	    					constructSheet(allinputs, path);
	    					
	    					
//	    					numberOfComputedCyclesInJunction++;
	    				}
	    			}
	    		}
	    	}
	    	
//	    	if(numberOfComputedCyclesInJunction == 1)
//	    	{
//	    		//aici practic nu e un satjunction
//	    		//deocamdata nu tratez cazul...?
//	    	}
	    }

	    for(Integer junc : junctions)
	    {
	    	buildLocalJunctions(cycexist.get(junc));
	    }
	}

	private void constructSheet(HashSet<Integer> allinputs, ArrayList<Integer> path)
	{
		CorrSurface s = new CorrSurface(theGraph, path, allinputs);
		this.cycles.add(s);
		
		for(Integer x : s.inputs)
		{
			inputNodesToSheetVars.get(x).addSATVar(s);
		}
	}

	private void buildLocalJunctions(ArrayList<HashSet<Integer>> cycexist) 
	{
		for(int i=0; i<cycexist.size() - 1; i++)
	    {
	    	for(int j=i+1; j<cycexist.size(); j++)
	    	{
	    		HashSet<Integer> rce = new HashSet<Integer>(cycexist.get(i));
	    		rce.retainAll(cycexist.get(j));
	    		
	    		if(rce.size() == 1)
	    		{
	    			//are doua noduri in comun
	    			HashSet<Integer> union = new HashSet<Integer>(cycexist.get(i));
	    			union.addAll(cycexist.get(j));
	    			union.removeAll(rce);
	    			
	    			if(cycexist.contains(union))
	    			{
	    				//System.out.println(sheetVars.get(i) + " + " + sheetVars.get(j) + " = " + union);
//	    				SATVar s1 = new SATVar(cycexist.get(cycexist.indexOf(union)), SATVar.SHEET);
//	    				SATVar s2 = new SATVar(cycexist.get(i), SATVar.SHEET);
//	    				SATVar s3 = new SATVar(cycexist.get(j), SATVar.SHEET);
//	    				sheetVars.add(s1);sheetVars.add(s2);sheetVars.add(s3);
	    				
	    				//SATJunction satj = new SATJunction(sheetVars.get(sheetVars.indexOf(union)), sheetVars.get(i), sheetVars.get(j));
	    				SATJunction satj = new SATJunction(findCorrSurfaceGivenJunction(union, null), 
	    						findCorrSurfaceGivenJunction(cycexist.get(i), null), 
	    						findCorrSurfaceGivenJunction(cycexist.get(j), null));

	    				boolean exists = false;
	    				for(SATJunction x : SATJunctions)
	    					if(x.equals(satj))
	    						exists = true;
	    				
	    				if(!exists)
	    					SATJunctions.add(satj);
	    			}
	    		}
	    	}
	    }
	}

	private void constructTheGraph(Set<Integer> vertices, int[][] biggraph)
	{
		for(Integer v : vertices)
	    	theGraph.addVertex(v);
	    for(int i=0; i<biggraph.length; i++)
		{
    		int fn = biggraph[i][0];
    		int sn = biggraph[i][1];

    		if(vertices.contains(fn) || vertices.contains(sn))// de ce OR?
			{
    			theGraph.addEdge(fn,sn);
			}
		}
	}
	
	public boolean equals(SearchGraph sg)
	{
		//two searchgraphs are equal if their graphs are equal
		return sg.theGraph.equals(this.theGraph);
	}
	
	protected ArrayList<Integer> getJunctions2()
	{
		ArrayList<Integer> ret = new ArrayList<Integer>();
		for(Integer v : theGraph.vertexSet())
		{
			if(theGraph.degreeOf(v) > 2)
				ret.add(v);
			if(theGraph.degreeOf(v) < 2)
				System.out.println("pula " + v);
				
		}
		return ret;
	}
	
	protected HashSet<Integer> getNodes()
	{
		return new HashSet<Integer>(theGraph.vertexSet());
	}
	
	protected ArrayList<Integer> getNeighbours(Integer node)
	{
		return (ArrayList<Integer>)Graphs.neighborListOf(theGraph, node);
	}
	
	protected CorrSurface findCorrSurfaceGivenJunction(HashSet<Integer> init, HashSet<Integer> nono)
	{
		for(CorrSurface set : this.cycles)//exista vreun ciclu care contine cele doua noduri care definesc intersectia la junction?
		{
			if(set.vertexSet().containsAll(init) && (nono==null || !set.vertexSet().containsAll(nono)))
			{
//				System.out.println("exista! deja" + set);
//				cycexist.add(init);
				return set;
			}
		}
		return null;
	}
	
//	public void addInputNodes(HashSet<Integer> inputs2)
//	{
//				
//	}

//	public ArrayList<Coordinate> getJunctions(Set<Integer> vertices, HashMap<Integer, Coordinate> coords, int[][] biggraph)
//	{
//		ArrayList<Coordinate> ret = new ArrayList<Coordinate>();
//		HashMap<Integer, Integer> countedges = new HashMap<Integer, Integer>();
//		for(int i=0; i<biggraph.length; i++)
//		{
//    		int fn = biggraph[i][0];
//    		int sn = biggraph[i][1];
//
//    		if(vertices.contains(fn) || vertices.contains(sn))// de ce OR?
//			{
//    			countedges.put(fn, (countedges.get(fn) == null ? 0 : countedges.get(fn)) + 1);
//    			countedges.put(sn, (countedges.get(sn) == null ? 0 : countedges.get(sn)) + 1);
//			}
//		}
//		
//		
//		for(Integer k : countedges.keySet())
//	    {
//	    	if(countedges.get(k) > 2)
//	    	{
//	    		//System.out.println("%%%% junction at " + k + " are " + countedges.get(k) + " sheets");
//	    		ret.add(new Coordinate(coords.get(k)));
//	    	}
//	    }
//		
//		return ret;
//	}
	
//	protected void addInputNodes(HashSet<Integer> ins)
//	{
//		//un nod care e input se afla pe un segment, deci are doar doi vecini
//		//System.out.println("++++" + nodes.keySet());
//		
//		List<Integer> torem = new ArrayList<Integer>();
//		List<SNode> allHelps = new ArrayList<SNode>();
//		for(Integer n : nodes.keySet())
//		{
//			//System.out.println("change " + n);
//			if(ins.contains(n))
//			{
//				//System.out.println("contained");
//				
//				SNode ninp = nodes.get(n);
//				SInput nsin = new SInput(ninp.co);
//				nsin.number = n;//inca un cacat...pentru a stii pentru care input este nodul asta
//				//poate ar trebui sa tin in noduri numere din astea
//				if(ninp.ngh.size() != 2)
//				{
//					System.out.println("Problem! the node should have only 2 neighbors!");
//				}
//				
//				List<SNode> helps = addHelpNodes(ninp);
//				allHelps.addAll(helps);
//				
//				inputs.put(nsin.co.toString(), nsin);
//				//inputs.put(n, nsin);
//				
//				SContact scin = nsin.createContact(ninp.co);
//				
//				//voi face deocamdata asa:
//				//exista un nod de input cu aceeasi coordonata precum un nod normal
//				//doar unul din vecinii celui cu coordonata normala devine vecin
//				
//				scin.addNeighbor(helps.get(0));
//				scin.addNeighbor(helps.get(1));
//				
////				for(SNode sn : ninp.ngh)
////				{
////					scin.addNeighbor(sn);
////					sn.removeNeighbor(ninp);
////				}
//				
//				icontacts.add(scin);
//				
//				//nsin.setContactModes(new int[]{0});
//				//nodes.put(n, scin);
//				//nodes.remove(ninp);//nu mai e nod normal...acuma e input
//				torem.add(n);
//			}
//		}
//		
//		for(Integer n : torem)
//			nodes.remove(n);
//		
//		for(SNode h : allHelps)
//		{
//			nodes.put(h.number, h);
//		}
//	}
	
//	public List<SNode> addHelpNodes(SNode inputNode)
//	{
//		//verifica de nu exista deja un nod cu coordonata asta
//		//for(SNode n : nodes.values())
//		//{
//		//	if(n.co.equals(c))
//		//		return n;
//		//}
//		
//		//acuma helpnodurile sunt folosite pentru a inlocui punctele de input
//		//teoretic maxim doua asemenea noduri pot exista pentru o anumita coordonata
//		//deci daca exista deja un helpnode
//		//al doilea il ia drept vecin
//		
//		//primul helpnode ia vecinii celui cu indice pozitiv (input)
//		//al doilea helpnode ia doar un vecin de la cel cu indice negativ (primul helpnode)
//		//sper sa nu ajung sa chem metoda asta de trei ori ca am belit pula
//		SNode node1 = null;
//		SNode node2 = null;
////		for(Entry<Integer, SNode> e : nodes.entrySet())
//		{
////			if(e.getValue().co.equals(c))//aici e inputul
//			{
////				input = e.getValue();
//				node1 = inputNode.ngh.get(SNode.LEFT);
//				node2 = inputNode.ngh.get(SNode.RIGHT);
//			}
//		}
//		
//		//aici ar fi bine sa fie cele trei diferite de null
//		
//		//fa doua noduri
//		SNode help1 = new SNode(inputNode.co);
//		help1.number = SearchGraph.nrHelpNode--;
//		SNode help2 = new SNode(inputNode.co);
//		help2.number = SearchGraph.nrHelpNode--;
//		
//		//desfa legaturile intre node1, node2 si input
//		node1.removeNeighbor(inputNode);
//		node2.removeNeighbor(inputNode);
//		
//		//hai sa desfac si intre input si node1 si node2
//		inputNode.removeNeighbor(node1);
//		inputNode.removeNeighbor(node2);
//		
//		//astea raman intr-un singur vecin deocamdata - ca si cum ar fi desfacute
//		help1.addNeighbor(node1);
//		help2.addNeighbor(node2);
//		
//		node1.addNeighbor(help1);
//		node2.addNeighbor(help2);
//		
//		List<SNode> ret = new ArrayList<SNode>();
//		ret.add(help1);
//		ret.add(help2);
//		
//		return ret;
//	}
	
	public boolean containsVertex(int nr)
	{
		//return nodes.keySet().contains(nr);
		return theGraph.containsVertex(nr);
	}
	
//	public SNode getNode(int nr)
//	{
//		return nodes.get(nr);
//	}
	
//	public void dfs(SNode vis, Set<SNode> visited, SearchTarget target)
//	{
//		if(visited.contains(vis))
//		{
//			return;
//		}
//		visited.add(vis);
//		//System.out.println("visit " + vis);
//		
//		if(vis.getClass() == SInput.class)
//		{
//			if(vis.number == 19)
//				System.out.println("iuhu...aici...!");
//			target.tubes.add(vis.number);
//		}
//		if(vis.getClass() == SSheet.class)
//		{
//			for(Integer x : ((SSheet)vis).inputs)
//			{
//				if(target.sheets.contains(x))
//					target.sheets.remove(x);
//				else
//					target.sheets.add(x);
//			}
//			
//		}
//		
//		List<SNode> ngh = vis.ngh;
//		for(SNode n : ngh)
//			dfs(n, visited, target);
//		if(vis.number == 19)
//			System.out.println("SYSO " + vis + "->" + vis.ngh);
//		if(visited.containsAll(vis.ngh) && vis.ngh.size() == 1)
//		{
//			//System.out.println("here I am " + vis);
//			target.allLeafsAreInput = target.allLeafsAreInput 
//				&& ((vis.getClass() == SInput.class) || (vis.getClass() == SSheet.class)
//						|| vis.ngh.get(0).getClass() == SInput.class) || (vis.ngh.get(0).getClass() == SSheet.class);
//		}
//			
//		
//	}
	
	public boolean getPrimalStatus()
	{
		//HARDCODED
//		return (cycles.size() == 2);
		
		//trebuie sa fie ceva legat de coordonate
		return isPrimal;
	}
	
//	public static SATVar findVar(Integer inputs)
//	{
//		//aici mai trebuei sv.istube()?
////		for(SATVar sv: SearchGraph.vars)
////			if(sv.contains(inputs) && sv.isTube() == true)
////				return sv;
//		return null;
//	}
	
//	public static SATVar findVar(Set<Integer> nrs)
//	{
//		//aici mai trebuei sv.istube()?
//		for(SATVar sv: SearchGraph.vars)
//			if(sv.equals(nrs) && sv.isTube() == false)
//				return sv;
//		return null;
//	}
	
	public int addBraidNode(Integer node, Integer ngh, CorrSurface surf)
	{
		int bn = nextBraidNodeNumber();
		theGraph.addVertex(bn);
		braidNodes.add(bn);
		
//		System.out.println("add " + bn + "between " + node + " and " + ngh);
//		
//		System.out.println("inainte [" + bn + "->" + Graphs.neighborListOf(theGraph, bn).size() + "] AND " +
//				node + "->" +Graphs.neighborListOf(theGraph, node).size()  + " AND " +
//				ngh + "->" +Graphs.neighborListOf(theGraph, ngh).size());
		
		DefaultEdge edg = theGraph.getEdge(node, ngh);
		theGraph.removeEdge(edg);
		theGraph.addEdge(node,  bn);
		theGraph.addEdge(bn, ngh);
		
//		System.out.println("dupa [" + bn + "->" +Graphs.neighborListOf(theGraph, bn).size() + "] AND " +
//				node + "->" +Graphs.neighborListOf(theGraph, node).size()  + " AND " +
//				ngh + "->" +Graphs.neighborListOf(theGraph, ngh).size());
//		System.out.println();
		
		SATBraid braid = new SATBraid(surf, null, null);
		SATBraids.add(braid);//dar lipsesc tuburile
		
		braidNodesToBraidVars.put(bn, braid);
		
		return bn;
	}

	public void finalizeConstruction()
	{
		// aici generez tuburile
		
		HashSet<Integer> forbiddenNodes = new HashSet<Integer>(braidNodes);
		forbiddenNodes.addAll(inputNodes);
		
		//for(Integer node : theGraph.vertexSet())
		for(Integer node : forbiddenNodes)
		{
			//if(forbiddenNodes.contains(node))
				//continue;
			
			//din fiecare forbiddennode pleaca exact doua tree-uri
			//chiar sunt doua?
			int nrTrees = Graphs.neighborListOf(theGraph, node).size();
			System.out.println("from " + node + " there are " + nrTrees + " trees");
			for(Integer nextnode : Graphs.neighborListOf(theGraph, node))
			{
				HashSet<Integer> path = findTree(nextnode, forbiddenNodes, null);
				
				if(path.size() == 1)
				{
					/*
					 * This is a short tree. A single segment. 
					 */
					path.add(node);
				}
				
				SATVar tube = constructTube(path);
				
				attachTube(tube, path);
			}
		}
	}

//	private void calculateTree() {
////		GraphIterator<Integer, DefaultEdge> gi = new DepthFirstIterator<Integer, DefaultEdge>(theGraph);
////		EdgeTraversalCollection coll = new EdgeTraversalCollection();
////		gi.addTraversalListener(coll);
////		//traverse
////		while(gi.hasNext())
////			gi.next();
////		
////		for(DefaultEdge edg : coll.getEdgeList())
////		{
////			int[] seg = new int[]{theGraph.getEdgeSource(edg), theGraph.getEdgeTarget(edg)};
////			traversedEdges.add(seg);
////		}
//		
//		//Integer
//		
////		findTree(theGraph.vertexSet().iterator().next(), 
////				null /*target*/, 
////				new HashSet<Integer>()/*visited*/,
////				traversedEdges /*edges*/);
//	}

	private SATVar constructTube(HashSet<Integer> path) {
		//exista aceasta variabila in satvars?
		for(SATVar v : tubeVars)
		{
			if(v.vertexSet().equals(path))
			{
				System.out.println("tubul exista " + v.getNumber());
				return v;
			}
		}
		
		SATVar tube = new SATVar(theGraph, new ArrayList<Integer>(path), SATVar.TUBE);
		tubeVars.add(tube);
		
		return tube;
	}

	private void attachTube(SATVar tube, HashSet<Integer> path) {
		//pentru fiecare nod care e in braidnodes si in SATVar actualizeaza SATBraids
		HashSet<Integer> braids = new HashSet<Integer>(path);
		braids.retainAll(braidNodes);
		for(Integer bb : braids)
		{
			//System.out.print(bb + "t ");
			braidNodesToBraidVars.get(bb).addTubeVar(tube);
		}
		
		HashSet<Integer> inputs = new HashSet<Integer>(path);
		inputs.retainAll(inputNodes);
		//inputurile astea sunt pe acelasi tub si imi ies mai multe inputuri...
		for(Integer input : inputs)
		{
//			SATInput si = inputNodesToTubeVars.get(input);
//			if(si == null)
//			{
//				//si = new SATInput(input, null, null);
//				si = new SATInput(input);//, null, null);
//				inputNodesToTubeVars.put(input, si);
//			}
			inputNodesToTubeVars.get(input).addSATVar(tube);
		}
	}
	
	public HashSet<int[]> getCNF(boolean iAmSearchingPrimal, HashSet<Integer> xSearch, HashSet<Integer> zSearch, HashMap<Integer, HashSet<Integer>> sheetXor) {
		HashSet<int[]> ret = new HashSet<int[]>();
		
		/*
		 * urmatoarea observatie din teza
		 * A. tuburile din primal si suprafetele din dual formeaza spatiul primal
		 * B. suprafetele din primal si tuburile din dual formeaza spatiul dual
		 * iAmSearchingPrimal == true indica spatiul primal -> A,
		 *  deci tuburile (X) din sg primale si suprafetele (X) din sg duale
		 *  si suprafetele (Z) din sg primale si tuburile (Z) din sg duale
		 * iAmSearchingPrimal == false indica spatiul dual -> B
		 *  deci suprafetele (Z) din sg primale si tuburile (Z) din duale
		 *  si suprafetele (X) din sg primale si tuburile (X) din sg duale
		 *  
		 *  acuma: cand caut in spatiul primal inseamna ca trebuie sa iau
		 */
		
		if(iAmSearchingPrimal)
		{
			if(getPrimalStatus())//caut primal si este primal
			{
				//ia X-urile din tuburi de primal
				getTubeAndInputCNF(xSearch, ret);
				//getSheetCNF(zSearch, ret, sheetXor);
			}
			else//caut primal si este dual
			{
				//ia Z-urile din sheeturi de dual
				//getTubeAndInputCNF(zSearch, ret);
				getSheetCNF(xSearch, ret, sheetXor);
			}
		}
		else
		{
			if(getPrimalStatus())//caut dual si este primal
			{
				//ia X-urile din tuburi de primal
				//getTubeAndInputCNF(xSearch, ret);
				getSheetCNF(zSearch, ret, sheetXor);
				//ia X-urile din sheeturi de dual
				//getSheetCNF(xSearch, ret, sheetXor);
			}
			else//caut dual si este dual
			{
				//ia Z-urile din tuburi de dual
				getTubeAndInputCNF(zSearch, ret);
			}
		}
		
		return ret;
	}

	private void getSheetCNF(HashSet<Integer> sheetSearch, HashSet<int[]> ret, HashMap<Integer, HashSet<Integer>> sheetXors)
	{
		//intr-un search graph inputnodurile de sheet se impart in doua
		//care apartin unui sheet ce trebuie inclus: le voi marca cu numere pozitive
		//care nu apartin unui sheet ce trebuie inclus: le voi marca cu numere negative
		
		for(SATJunction v : SATJunctions)
		{
//			System.out.println(v);
			ret.addAll(v.getCNF());
		}
		
		for(Integer inp : inputNodes)
		{
			SATInput v = inputNodesToSheetVars.get(inp);
//			System.out.print("cautat " + inp);
			if(sheetSearch.contains(inp))
			{
//				System.out.println("gasit");
				ret.addAll(v.getCNF(SATInput.ISXOR));//xor
			}
			else
			{
//				System.out.println();
				ret.addAll(v.getCNF(SATInput.ISEQUIV));//no xor
			}
//			System.out.println(v);
		}
	}

	private void getTubeAndInputCNF(HashSet<Integer> tubeSearch, HashSet<int[]> ret)
	{
		for(SATBraid v : SATBraids)
		{
//			System.out.println(v);
			ret.addAll(v.getCNF());
		}
		
		for(Integer inp : inputNodes)
		{
			SATInput v = inputNodesToTubeVars.get(inp);
//			System.out.print("cautat " + inp);
			if(tubeSearch.contains(inp))
			{
//				System.out.println("gasit");
				ret.addAll(v.getCNF(SATInput.ISXOR));//xor
			}
			else
			{
//				System.out.println();
				ret.addAll(v.getCNF(SATInput.ISEQUIV));//no xor
			}
//			System.out.println(v);
		}
	}
}

//public void searchCycles(HashSet<Integer> allinputs)
//{
//	HashMap<Set<Integer>, List<Integer>> cycles = new HashMap<Set<Integer>, List<Integer>>();
//	
//	for(SNode node : nodes.values())
//	{
//		List<Integer> visited = new ArrayList<Integer>();
//		findCycle(node, visited, node.number, cycles);
//	}
//	
//	for(List<Integer> nodes : cycles.values())
//	{
//		CorrSurface s = new CorrSurface(nodes, allinputs);
//		this.cycles.add(s);
//	}
//	
//	//
//	//int takesheets = (int)Math.ceil(Math.log(cycles.size())/Math.log(2));
//	int takesheets = (int)Math.ceil(Math.sqrt(cycles.size()));
//	System.out.println("this SearchGraph has nrcycles " + cycles.size() + "->" + takesheets);
//	
//	//aici le sortez in functie de numarul de inputuri.
//	for(int i=0; i<this.cycles.size()-1; i++)
//	{
//		for(int j=i+1; j<this.cycles.size(); j++)
//		{
//			CorrSurface one = this.cycles.get(i);
//			CorrSurface two = this.cycles.get(j);
//			if(one.inputs.size() > two.inputs.size())
//			{
//				CorrSurface three = one;
//				this.cycles.set(i, two);
//				this.cycles.set(j, three);
//			}
//		}
//	}
//	
//	//aici le selectez
//	HashSet<CorrSurface> torem = new HashSet<CorrSurface>();
//	for(int i=takesheets; i<this.cycles.size(); i++)
//	{
//		torem.add(this.cycles.get(i));
//	}
//	this.cycles.removeAll(torem);//ceea ce banuiesc eu ca nu sunt importante elimin...desi mai stii?
//	
//	for(CorrSurface cy : this.cycles)
//		System.out.println(cy);
//
//}
//
//public void findCycle(SNode node, List<Integer> visited, int start, HashMap<Set<Integer>, List<Integer>> cycles)
//{
//	List<Integer> myvisit = new ArrayList<Integer>(visited);
//	
//	if(node.number == start && myvisit.size() > 2)
//	{
//		HashSet<Integer> key = new HashSet<Integer>(myvisit);
//		if(!cycles.keySet().contains(key))
//		{
//			cycles.put(key, myvisit);
//			//System.out.println("f2 " + start + " " + myvisit);
//		}
//	}
//	
//	if(visited.contains(node.number))
//	{
//		//System.out.println(node.number + " in " + visited);
//		return;
//	}
//	
//	myvisit.add(node.number);
//	for(SNode ngh : node.ngh)
//	{
//		//if(ngh.number != start || visited.size() > 1)
//		findCycle(ngh, myvisit, start, cycles);
//	}
//}