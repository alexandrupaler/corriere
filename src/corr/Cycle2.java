package corr;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.print.attribute.standard.Finishings;

import org.jgrapht.Graphs;

public class Cycle2 {

	//  Graph modeled as list of edges
	protected int[][] graph;
	//each vertex has a 3d coordinate
	protected HashMap<Integer, Coordinate> coords;
	//some vertices are input/outputs of the circuit
	protected HashSet<Integer> inputs;
	
	public List<Coordinate> braidPoints = new ArrayList<Coordinate>();//added 7.4.2014 for vis purposes. nu stiu de era pe undeva, dar eu pun aici
	public List<Coordinate> junctionPoints = new ArrayList<Coordinate>();

	//public List<CorrSurface> cycles = new ArrayList<CorrSurface>();
	
	public List<SearchGraph> searches = new ArrayList<SearchGraph>();
	private HashMap<Integer, Integer> newNumberToOldNumber = null;
	
	public Set<Integer> getNodes()
	{
		return coords.keySet();
	}
	
	public Cycle2(String path)
	{
		BufferedReader br = null;
		try 
		{
			br = new BufferedReader(new FileReader(path));
			//br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(path)));
			int nrinputs = Integer.parseInt(br.readLine());
			int nrgraph = Integer.parseInt(br.readLine());
			int nrcoords = Integer.parseInt(br.readLine());

			inputs = new HashSet<Integer>();
			if(nrinputs > 0)
			{
				String[] ins = br.readLine().split(",");
				for(int i=0; i<nrinputs; i++)
					inputs.add(Integer.parseInt(ins[i].trim()));
			}

			graph = new int[nrgraph][2];
			for(int i=0; i<nrgraph; i++)
			{
				String[] edge = br.readLine().split(",");
				graph[i][0] = Integer.parseInt(edge[0].trim());
				graph[i][1] = Integer.parseInt(edge[1].trim());
			}

			coords = new HashMap<Integer, Coordinate>(nrcoords);
			for(int i=0; i<nrcoords; i++)
			{
				String line = br.readLine();
				String[] coord = line.split(",");
				int[] icoord = new int[3];
				
				for(int j=1; j<4; j++)
					icoord[j-1] = Integer.parseInt(coord[j].trim());
				
				coords.put(Integer.parseInt(coord[0].trim()), new Coordinate(icoord));
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
//	public void search() {
//
//		for (int i = 0; i < graph.length; i++)
//			for (int j = 0; j < graph[i].length; j++)
//			{
//				findNewCycles(new int[] {graph[i][j]});
//			}
//		
//		System.out.println("XXX " + cycles.size());
//		
//		for(int i=0; i<cycles.size()-1; i++)
//		{
//			for(int j=i+1; j<cycles.size(); j++)
//			{
//				CorrSurface one = cycles.get(i);
//				CorrSurface two = cycles.get(j);
//				if(one.inputs.size() > two.inputs.size())
//				{
//					CorrSurface three = one;
//					cycles.set(i, two);
//					cycles.set(j, three);
//				}
//			}
//		}
//		
//		HashSet<Integer> leftinputs = new HashSet<Integer>(inputs);
//		HashSet<CorrSurface> torem = new HashSet<CorrSurface>();
//		for(int i=0; i<cycles.size(); i++)
//		{
//			if(leftinputs.removeAll(cycles.get(i).inputs))
//			{
//				System.out.println(cycles.get(i));
//			}
//			else
//			{
//				torem.add(cycles.get(i));
//			}
//		}
//		cycles.removeAll(torem);//ceea ce banuiesc eu ca nu sunt importante elimin...desi mai stii?
//
//		//construct for each cycle all the possible corr surfaces
//		for(CorrSurface cy : cycles)
//		{
//			//System.out.println("sheets for:");
//			//System.out.println(cy);
//			cy.computeSheets(coords);
//		}
//	
//		//remove corr surface objects without sheets
//		cleanCorrSurfaces();
//		
//		//for(CorrSurface cy : cycles)
//		//	System.out.println(cy);
//		
//		//constructSearchGraphs();
//	}
//	

	protected void constructSearchGraphs()
	{
		Set<Integer> res = new TreeSet<Integer>();
		Set<Integer> allnodes = getNodes();
				
		for(Integer node : allnodes)
		{
			if(res.contains(node))
				continue;
			Set<Integer> oldres = new TreeSet<Integer>(res);
			dfs(node, res);
			
			Set<Integer> difres = new TreeSet<Integer>(res);
			difres.removeAll(oldres);
			System.out.println("difres " + difres);
			
			if(difres.size() > 2)//hardcoded pentru ca am un cacat in fisierul de Astate
			{
				SearchGraph sg = new SearchGraph(difres, coords, graph, this.inputs);
				for(Integer index : sg.getJunctions2())
				{
					junctionPoints.add(coords.get(index));	
				}
				
				searches.add(sg);
			}
		}
		
		System.out.println("\n============= Intersect \n");
		//intersecteaza segmente cu suprafete
		intersectSearchGraphs();
		
		for(SearchGraph sg : searches)
		{
			//acuma pot calcula tuburile
			sg.finalizeConstruction();
		}
	}
	
//	public HashSet<SNode> searchSurface(HashSet<Integer> searchtubes, HashSet<Integer> searchsheets)
//	{
//			
//		//for(SearchGraph sg : searches)
//				
//		//oricum trebuie sa unesc searchgraphurile dupa sheet-uri
//		//adica un searchgraph pentru primal si unul pentru dual
//		//in cazul meu acuma cel dual ar trebui sa aiba index 0, si primal are index 1
//		//for(SearchGraph sg : searches)
//		
//		SearchGraph sg = findSearchGraph(searchtubes, searchsheets);
//		
////		System.out.println("------------------");
////		for(SContact sc : sg.scontacts)
////			System.out.println(sc + " ---> " + sc.ngh);
////		System.out.println("++++++++++++++");
////		for(SContact sc : sg.icontacts)
////			System.out.println(sc + " ---> " + sc.ngh);
////		
//		
//		//returns a configuration where bit=0 means the sheet is inactive
//		//and bit=1 means a sheet is active
//		int maxcomb = 1 << sg.sheets.size();
//		int inactiveConf = 0;//allsheets are inactive
//		for(int i=0; i<maxcomb; i++)
//		{
//			HashSet<Integer> res = new HashSet<Integer>();
//			for(int j=0; j<sg.sheets.size(); j++)
//			{
//				if (((i>>j) & 1) == 1)
//				{
//					HashSet<Integer> intersect = new HashSet<Integer>(sg.sheets.get(j).inputs);
//					intersect.retainAll(res);
//					res.addAll(sg.sheets.get(j).inputs);
//					res.removeAll(intersect);
//				}
//			}
//			
//			if(res.equals(searchsheets))
//			{
//				//should be valid sheet configuration
//				inactiveConf = i;
//				System.out.println("Conf To search " + i + " " + res);
//				break;
//			}
//		}
//		
//		HashSet<SNode> keysForLater = new HashSet<SNode>();
//		List<SContact> necessary = new ArrayList<SContact>();
//		for(int i=0; i<sg.sheets.size(); i++)
//		{
//			if(((inactiveConf>>i)&1)==1)
//			{
//				keysForLater.add(sg.sheets.get(i));
//				
//				List<SContact> curr = sg.sheets.get(i).contacts;
//				if(necessary.size() == 0)
//					necessary.addAll(curr);
//				else
//				{
//					for(SContact scur : curr)
//					{
//						boolean wasthere = false;
//						for(SContact sc : necessary)	
//						{
//							if(sc.co.equals(scur.co))
//							{
//								wasthere = true;
//								necessary.remove(sc);
//								break;
//							}
//						}
//						if(!wasthere)
//							necessary.add(scur);
//					}
//				}
//			}
//		}
//		
//		SSheet toSearchOn = new SSheet(searchsheets);
//		for(SContact c : necessary)
//		{
//			c.newParent(toSearchOn);
//			toSearchOn.contacts.add(c);
//			//System.out.println("fill " + c);
//		}
//		
//		for(SContact sc : sg.scontacts)
//		{
//			if(!necessary.contains(sc))
//			{
//				sc.setContactMode(0);
//			}
//		}
//		
//		boolean finishTheCircus = false;
//		HashSet<SNode> ret = null;
//		
//		//calculeaza suprafetele care sunt generate de rezultatul inactiveConf
//		//int sconfig = (1 << sg.scontacts.size());
//		int sconfig = (1<<necessary.size());
//		for(int sfg=0; sfg<sconfig && !finishTheCircus; sfg++)
//		{
//			//reset visited flag on contacts
//			//mark unneeded input sheets with contact=0
//			//combine all other sheets
//			
//			//for(int sc=0; sc<sg.scontacts.size(); sc++)
//			for(int sc=0; sc<necessary.size(); sc++)
//			{
//				int smode = ((sfg >> sc) & 1) + 1;
//				//sg.scontacts.get(sc).setContactMode(smode);
//				necessary.get(sc).setContactMode(smode);
//			}
//
////				for(int inactivei =0; inactivei < sg.sheets.size(); inactivei++)
////				{
////					//if(ss.inputs.contains(18))
////					if(((inactiveConf>>inactivei)&1) == 0)
////					{
////						//bag pula in ce prostie am facut aici
////						SSheet ss = sg.sheets.get(inactivei);
////						System.out.println(ss);
////						for(SContact ssc : ss.contacts)
////							ssc.setContactMode(0);
////					}
////				}
//			
//			//sa blochez deocamdata toate sheeturile. le scot din cautare
//						
//			//System.out.println("== SearchGraph " + sg.toString() + " with " + sg.inputs.size());
//			
//			int iconfig = (1 << sg.icontacts.size());
//			for(int ifg=0; ifg<iconfig && !finishTheCircus; ifg++)
//			//int ifg = 2;
//			{
//				System.out.println("Configuration shs:" + sfg + " ins:" + ifg);
//				
//				//set input contacts configuration
//				for(int ic = 0; ic < sg.icontacts.size(); ic++)
//				{
//					int mode = ((ifg >> ic) & 1) * 3;
//					sg.icontacts.get(ic).setContactMode(mode);
//				}
//				
//				//dfs
//				HashSet<SNode> visited = new HashSet<SNode>();
//				HashSet<SNode> lastVisited = new HashSet<SNode>();
//				
//				for(SContact sin : sg.icontacts)
//				{
//					for(SNode visnode : sin.ngh)
//					{
//						System.out.println("-------------------------------------------------");
//						SearchTarget target = new SearchTarget();
//						sg.dfs(visnode, visited, target);
//						
//						//sa scot inputurile din visited
//											
//						HashSet<SNode> dif = new HashSet<SNode>(visited); 
//						dif.removeAll(lastVisited);
//						System.out.println(dif);
//						if(dif.size() > 0 && target.allLeafsAreInput == true)
//						{
//							//if(target.sheets.contains(16) && target.sheets.contains(20))
//							System.out.println("FOUND1 " + target.tubes + " | " + target.sheets);
//							System.out.println("FOUND2 " + searchtubes + " | " + searchtubes.equals(target.tubes));
//
//							if(target.sheets.equals(searchsheets) && target.tubes.equals(searchtubes))
//							{
//								System.out.println("---visit from " + visnode + " leafsinputs " + target.allLeafsAreInput);
//								System.out.println(dif);
//								System.out.println(target.tubes);
//								System.out.println(target.sheets);
//								System.out.println();
//								
//								finishTheCircus = true;
//								ret = dif;
//								break;
//							}
//						}
//						if(finishTheCircus)
//							break;
//						
//						//scot inputul deoarece a fost vizitat, si trebuie si in cautarea urmatoare
//						//visited.remove(sin.parent);
//						visited.removeAll(sg.inputs.values());
//						lastVisited.addAll(visited);
//					}
//					if(finishTheCircus)
//						break;
//				}
//			}
//		}
//		
//		for(SContact c : necessary)
//		{
//			c.revertParent();
//		}
//		if(finishTheCircus)
//		{
//			ret.remove(toSearchOn);
//			ret.addAll(keysForLater);
//		}
//		
//		return ret;
//	}

	public SearchGraph findSearchGraph(HashSet<Integer> searchtubes,
			HashSet<Integer> searchsheets) {
		SearchGraph sg = null;
		for(SearchGraph sgc : searches)
		{
			if(searchtubes.size() > 0)
			{
				int nexttube = searchtubes.iterator().next();
				System.out.println(nexttube);
				if(searchtubes.size() > 0)
				{
					for(SInput si : sgc.inputs.values())
					{
						if(si.number == nexttube)
						{
							sg = sgc;
							break;
						}
					}
					if(sg != null)
						break;
				}
			}
			if(searchsheets.size() > 0)
			{
				//for(SSheet ss : sgc.sheets)
				for(CorrSurface cs : sgc.cycles)
				{
					System.out.println("sheet has inputs" + cs.inputs);
					//if(ss.inputs.contains(searchsheets.iterator().next()))
					if(cs.inputs.contains(searchsheets.iterator().next()))
					{
						sg = sgc;
						break;
					}
				}
				if(sg != null)
					break;
			}
		}
		return sg;
	}
	
	public void intersectSearchGraphs()
	{
		
		//intersectez segmentele din sg1 cu suprafetele din sg2
		for(SearchGraph sg1 : searches)
		{
//			HashSet<Integer> visitedNodes = new HashSet<Integer>();
			for(SearchGraph sg2 : searches)
			{
//				visitedNodes.clear();
				
				HashSet<Integer> nodesAddedInSg1 = new HashSet<Integer>();
				
				if(sg1.equals(sg2) || sg1.getPrimalStatus() == sg2.getPrimalStatus())
					continue;
				
				System.out.println("\n**** Intersect " + sg1 + " and " + sg2);
				
				//pentru asta iau fiecare nod din sg1
				//ia nodurile initiale, inainte de a adauga vreunul nou
				HashSet<Integer> noduriInitiale = sg1.getNodes();
				for(int[] seg : sg1.traversedEdges)
//				for(Integer node : noduriInitiale)
				{
//					visitedNodes.add(node);//asta a fost folosit ca start, deci nu il mai folosi ca stop la edge
					
					//ii iau si vecinii
//					for(Integer ngh : sg1.getNeighbours(node))
					{
//						if(nodesAddedInSg1.contains(ngh))//asta e un nod nou, ce pula mea?
//							continue;
//						if(visitedNodes.contains(ngh))//practic segmentul asta a fost procesat
//							continue;
						
						//si rezulta un segment
//						int[] seg = new int[] {node, ngh};
						//int[] seg = new int[] {node, ngh};

						List<Coordinate> icord = new ArrayList<Coordinate>();
						List<CorrSurface> isurf = new ArrayList<CorrSurface>();
						//pe care il intersectez cu sg2 si rezulta coordonatele intersectiei si suprafetele intersectate
						System.out.println("SEGMENT " + seg[0] + " ßßßßß " + seg[1]);
						intersect(sg2, seg, icord, isurf);
						
						int node = seg[0];
						int nextnode = seg[1];
						//nodurile nu sunt ordonate dupa coordonate inca
						
						sortIntersections(seg, icord, isurf);
						
						Iterator<Coordinate> itc = icord.iterator();
						for(CorrSurface surf : isurf)
						{
							nextnode = sg1.addBraidNode(node, nextnode, surf);

							//adaugand nodurile intersectiei la sg1, astea se vor putea regasi in lista de vecini
							//cand intersectez din nou cu sg2 un segment nou
							nodesAddedInSg1.add(nextnode);
							
							//adauga coordonatele?
							coords.put(nextnode, itc.next());
						}
						
						//si braidPoints
						braidPoints.addAll(icord);
					}
				}
			}
		}
	}
	
	private void sortIntersections(int[] seg, List<Coordinate> icord, List<CorrSurface> isurf)
	{
		Coordinate p[] = {coords.get(seg[0]), coords.get(seg[1])};
		//afla axa care nu este egala
		int axis = -1;
		for(int i=0; i<3; i++)
		{
			if(p[0].coord[i] != p[1].coord[i])
				axis = i;
		}
		
		//bubble sort
		for(int i=0; i<icord.size()-1; i++)
		{
			for(int j=i+1; j<icord.size(); j++)
			{
				if(icord.get(i).coord[axis] > icord.get(j).coord[axis])
				{
					//swap
					Coordinate sw = icord.get(i);
					icord.set(i, icord.get(j));
					icord.set(j, sw);
					
					CorrSurface sws = isurf.get(i);
					isurf.set(i, isurf.get(j));
					isurf.set(j, sws);
				}
			}
		}
		
	}

	//intersects a segment consisting of two graph edges with all the possible sheets
	protected void intersect(SearchGraph sg, int[] vertices, List<Coordinate> coordinates, List<CorrSurface> surfs)
	{
		Coordinate p[] = {coords.get(vertices[0]), coords.get(vertices[1])};
		//HashMap<Coordinate, CorrSurface> intersectedSheets = new HashMap<Coordinate, CorrSurface>();
		
		int ctc[] = p[0].findConstCoord1(p[1]);
		if(ctc[2] == 2)//daca au doua coordonate constante, adica daca sunt pe una din cele trei axe
		{
			//for(SearchGraph sg : searches)//acuma fac, dar problema este ca e clar ca unul nu va contine nimic
			{	//las asa ca sa nu mai modific prin cod
				for(CorrSurface cy : sg.cycles)
				{
					//for(Sheet sy : cy.sheets)//ar trebui numa pentru una dintre sheeturile echivalente
					if(cy.sheets.size() == 0)
						continue;
					Sheet sy = cy.sheets.get(0);//aici iau numa una...
					{
						for(Rectangle ry : sy)
						{
							Coordinate sect = ry.intersects(p); 
							if(sect != null && !sect.equals(p[1]))//capatul din dreapta, p[1] sa nu fie intersectia
							{
								System.out.println(ry + "/" + p[0] + " " + p[1] + " at " + sect);
								//intersectedSheets.put(sect, cy);
								coordinates.add(sect);
								surfs.add(cy);
							}
						}
					}
				}
			}
		}
		else
		{
			System.out.println("graphul fara inputuri e gresit!");
		}
	}

//	public void findNewCycles(int[] path)
//	{
//		int n = path[0];
//		int x;
//		int[] sub = new int[path.length + 1];
//
//		for (int i = 0; i < graph.length; i++)
//			for (int y = 0; y <= 1; y++)
//				if (graph[i][y] == n)
//					//  edge refers to our current node
//				{
//					x = graph[i][(y + 1) % 2];
//					if (!visited(x, path))
//						//  neighbor node not on path yet
//					{
//						sub[0] = x;
//						System.arraycopy(path, 0, sub, 1, path.length);
//						//  explore extended path
//						findNewCycles(sub);
//					}
//					else if ((path.length > 2) && (x == path[path.length - 1]))
//						//  cycle found
//					{
//						int[] p = normalize(path);
//						int[] inv = invert(p);
//
//						//deocamdata inca un hack
//						CorrSurface cp = new CorrSurface(p, inputs);
//						CorrSurface cinv = new CorrSurface(inv, inputs);
//
//						if (isNew(cp) && isNew(cinv))
//						{
//							cycles.add(cp);
//						}
//					}
//				}
//	}
	
//	//  check of both arrays have same lengths and contents
//	protected Boolean equals(int[] a, int[] b)
//	{
//		Boolean ret = (a[0] == b[0]) && (a.length == b.length);
//
//		for (int i = 1; ret && (i < a.length); i++)
//		{
//			if (a[i] != b[i])
//			{
//				ret = false;
//			}
//		}
//
//		return ret;
//	}

//	//  create a path array with reversed order
//	public int[] invert(int[] path)
//	{
//		int[] p = new int[path.length];
//
//		for (int i = 0; i < path.length; i++)
//		{
//			p[i] = path[path.length - 1 - i];
//		}
//
//		return normalize(p);
//	}

//	//  rotate cycle path such that it begins with the smallest node
//	public int[] normalize(int[] path)
//	{
//		int[] p = new int[path.length];
//		int x = smallest(path);
//		int n;
//
//		System.arraycopy(path, 0, p, 0, path.length);
//
//		while (p[0] != x)
//		{
//			n = p[0];
//			System.arraycopy(p, 1, p, 0, p.length - 1);
//			p[p.length - 1] = n;
//		}
//
//		return p;
//	}

	//  compare path against known cycles
	//  return true, iff path is not a known cycle
//	public Boolean isNew(CorrSurface path)
//	{
//		Boolean ret = true;
//
//		for(CorrSurface p : cycles)
//		{
//			if (path.equals(p))
//			{
//				ret = false;
//				break;
//			}
//		}
//
//		return ret;
//	}

//	//  return the int of the array which is the smallest
//	public int smallest(int[] path)
//	{
//		int min = path[0];
//
//		for (int p : path)
//		{
//			if (p < min)
//			{
//				min = p;
//			}
//		}
//
//		return min;
//	}

//	//  check if vertex n is contained in path
//	public Boolean visited(int n, int[] path)
//	{
//		Boolean ret = false;
//
//		for (int p : path)
//		{
//			if (p == n)
//			{
//				ret = true;
//				break;
//			}
//		}
//
//		return ret;
//	}
	
	protected List<Integer> getNeighbours(Integer n)
	{
		int nn = n;
		List<Integer> ret = new ArrayList<Integer>();
		
		for(int i=0; i<graph.length; i++)
		{
			for(int j=0; j<2; j++)
				if(graph[i][j] == nn)
					ret.add(graph[i][(j+1)%2]);
		}
		
		return ret;
	}
	
	public void shortDfs(Integer vis, List<Integer> visited)
	{
		visited.remove(vis);
		List<Integer> ngh = getNeighbours(vis);
		for (Integer n : ngh)
		{
			if(visited.contains(n))
			{
				shortDfs(n, visited);
			}
		}
	}
	
	public void dfs(Integer vis, Set<Integer> visited)
	{
		if(visited.contains(vis))
		{
			return;
		}
		visited.add(vis);
		
		List<Integer> ngh = getNeighbours(vis);
		for(Integer n : ngh)
			dfs(n, visited);
	}

	public ArrayList<String> constructCompleteSet(HashSet<Integer> xSearch, HashSet<Integer> zSearch) {
		
		//what am I searching?
		boolean iAmSearchingPrimal = computeSearchingSpace(xSearch, zSearch);
		System.out.println("-> caut in spatiul " + iAmSearchingPrimal);
		
		ArrayList<int[]> tmpRet = new ArrayList<int[]>();
		HashMap<Integer, HashSet<Integer>> sheetXors = new HashMap<Integer, HashSet<Integer>>();
		for(SearchGraph sg : searches)
		{
			tmpRet.addAll(sg.getCNF(iAmSearchingPrimal, xSearch, zSearch, sheetXors));
		}
		
		for(Integer onSheetNumber : sheetXors.keySet())
		{
			HashSet<Integer> toXor = sheetXors.get(onSheetNumber);
			int[] terms = new int[toXor.size()];
			int pos = 0;
			for(Integer t : toXor)
			{
				terms[pos] = t;
				pos++;
			}
			tmpRet.addAll(XorCnfConstruct.xorCNF(terms));
		}
		
		this.newNumberToOldNumber = generateAndTranslate(tmpRet);
		ArrayList<String> ret = cnfFromIntToString(tmpRet);
		
		//variabilele din tmpRet sunt pentru tot circuitul, dar eu construiesc un CNF doar pentru
		//partea primala sau cea duala, iar atunci din cauza ca in CNF variabilele trebuie numerotate
		//de la 1..n, iar astea din tmpRet sunt aiurea, trebuie sa le traduc cu un HashMap
		
		
		ret.add(0, "c test");
		ret.add(1, "p cnf " + newNumberToOldNumber.size() + " " + tmpRet.size());
		
		return ret;
	}
	
	public SATVar findVarUsingNumber(Integer nr, boolean translate)
	{
		//foloseste traducerea
		Integer number = nr;
		if(translate)
			number = newNumberToOldNumber.get(nr);
		
		System.out.print(number + ", ");
		
		//apoi pentru numarul initial al variabilei cauta
		for(SearchGraph sg : this.searches)
		{
			for(SATVar sv: sg.cycles)
				if(sv.getNumber().equals(number))
					return sv;
			for(SATVar sv: sg.tubeVars)
				if(sv.getNumber().equals(number))
					return sv;
		}
		
		return null;
	}
	
	public HashSet<SATVar> findBraidsHavingSheet(Integer sheetNr)
	{
		HashSet<SATVar> ret = new HashSet<SATVar>();
		
		for(SearchGraph sg : this.searches)
		{
			for(SATBraid sv: sg.SATBraids)
			{
				if(sv.ss.getNumber() == sheetNr)
				{
					ret.add(sv.ss);
					ret.add(sv.tt[0]);
					ret.add(sv.tt[1]);
				}
			}
		}
		return ret;
	}
	

	private ArrayList<String> cnfFromIntToString(ArrayList<int[]> tmpRet)
	{
		ArrayList<String> ret = new ArrayList<String>();
		for(int[] clause: tmpRet)
		{
			String stringClause = "";
			for(int i=0; i<clause.length; i++)
			{
				stringClause += clause[i] + " ";
			}
			stringClause += " 0";
			ret.add(stringClause);
		}
		return ret;
	}

	private HashMap<Integer, Integer> generateAndTranslate(ArrayList<int[]> tmpRet)
	{
		HashMap<Integer, Integer> newNumberToOldNumber = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> reversedMap = new HashMap<Integer, Integer>();
		
		for(int[] clause: tmpRet)
		{
			for(int i=0; i<clause.length; i++)
			{
				Integer reversed = reversedMap.get((Integer)Math.abs(clause[i]));
				if(reversed == null)
				{
					//+1 pentru ca variabilele incep de la 1, iar "0" reprezinta sfarsit de linie
					newNumberToOldNumber.put(newNumberToOldNumber.size()+1, Math.abs(clause[i]));
					reversedMap.put(Math.abs(clause[i]), newNumberToOldNumber.size());
					reversed = newNumberToOldNumber.size();
				}
				clause[i] = (int)Math.signum(clause[i])*reversed;
			}
		}
		return newNumberToOldNumber;
	}

	private boolean computeSearchingSpace(HashSet<Integer> xSearch, HashSet<Integer> zSearch)
	{
		boolean iAmSearchingPrimal = true;

		if(xSearch.size() > 0)
		{
			Integer node = xSearch.iterator().next();
			for(SearchGraph sg : searches)
			{
				if(sg.containsVertex(node))
				{
					iAmSearchingPrimal = sg.getPrimalStatus();
					break;
				}
			}
		}
		else if(zSearch.size() > 0)
		{
			Integer node = zSearch.iterator().next();
			for(SearchGraph sg : searches)
			{
				if(sg.containsVertex(node))
				{
					iAmSearchingPrimal = !sg.getPrimalStatus();
					break;
				}
			}
		}
		return iAmSearchingPrimal;
	}
	
	public void getTubeCoordinates(SATVar c, Integer sn, Integer prev, List<Coordinate> tubes, HashSet<Integer> vis)
	{
		if(prev != null)
		{
			tubes.add(coords.get(prev));
			tubes.add(coords.get(sn));
		}

		if(!vis.contains(sn))
		{
			vis.add(sn);

			for(Integer n : Graphs.neighborListOf(c, sn))
			{
				//if(n.getClass() != SSheet.class)
//				{
					getTubeCoordinates(c, n, sn, tubes, vis);
//				}
			}
		}
	}
	
//	protected void cleanCorrSurfaces()
//	{
//		List<Integer> corrtorem = new ArrayList<Integer>();
//		for(int i=0; i<cycles.size(); i++)
//		{
//			CorrSurface cy = cycles.get(i);
//			//System.out.println(i + ". For cycle:" + cy);
//			if(cy.sheets.size() == 0)
//			{
//				corrtorem.add(i);
//			}
//			/*
//			for(Sheet sy : cy.sheets)
//			{
//				System.out.println(sy);
//			}
//			*/
//		}
//		//remove empty corr surfaces
//		for(Integer pos : corrtorem)
//		{
//			cycles.remove(pos);
//		}
//	}
}
