package corr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Correlations {

	//static GearsApplet app = new GearsApplet();

	public static Cycle2 cyle = null;
	public static Sheet draw = new Sheet();
	public static List<List<Coordinate>> contours = new ArrayList<List<Coordinate>>();
	public static List<Coordinate> tubes = new ArrayList<Coordinate>();
	private static int debuggedVar = 1;
	
	public static ArrayList<String> getVars(HashSet<Integer> xSearch, HashSet<Integer> zSearch)
	{

		return cyle.constructCompleteSet(xSearch, zSearch);
		
		//tubesearch: pentru fiecare element cauta inputul corespunzator,  si tuburile care sunt vecine cu el, xor din ele
		//sheetsearch: pentru fiecare element cauta ciclul corespunzator, xor din toate
	}
	
	public static boolean solveSAT(String path, HashSet<Integer> xSearch, HashSet<Integer> zSearch, String additional)
	{
		if(cyle == null)
		{
			cyle = new Cycle2(path);
			cyle.constructSearchGraphs();
		}
		
		//reset
		draw.clear();
		contours.clear();
		tubes.clear();
		
		ArrayList<String> cnf = getVars(xSearch, zSearch);
		
		if(cnf == null)
		{
			System.out.println("NO SOLUTION!");
			return false;
		}
		
		if(!additional.equals(""))
		{
			String[] splits = additional.split(",");
			for(String s : splits)
				cnf.add(s);
		}
		
		StringBuilder sb = new StringBuilder();
		for(String s : cnf)
		{
			sb.append(s);
			sb.append("\r\n");
		}
		
		int[] sol = solver.IterateAll.solve(sb.toString());
		
		if(sol != null)
		{
			System.out.println("-- Solution");
			compCoordForSolution(sol, true);
			return true;
		}
		else
			System.out.println("-- No solution");
		
		return false;
		
		//System.out.println("X: " + xSearch + " | Z: " + zSearch);
	}
	
	//incer sa iau si alte solutii in afara de prima. poate arata mai bine. 19.08.2014
	public static void nextSolution(String path)
	{
		if(cyle == null)
		{
			cyle = new Cycle2(path);
			cyle.constructSearchGraphs();
		}
				
		//reset
		draw.clear();
		contours.clear();
		tubes.clear();
		
		int[] sol = solver.IterateAll.solve("");
		
		if(sol != null)
		{
			compCoordForSolution(sol, true);
		}
	}
	
	public static void compCoordForSolution(int[] sol, boolean translate)
	{
		draw.clear();
		contours.clear();
		tubes.clear();
		
		HashSet<SATVar> ret = new HashSet<SATVar>();

		//System.err.println("begin model");
		for(int i=0; i<sol.length; i++)
		{
			if(sol[i] < 0)
				continue;
			
			SATVar var = cyle.findVarUsingNumber(sol[i], translate);
			
			if(var == null)
			{
				System.out.println("CE PULA MEA?");
				continue;
			}
			ret.add(var);
			
//			if(var.isTube())
//			{
//				for(Integer si : var.vertexSet())//asta e ciudat aici oricum. 2015
//				{
//				}
//			}
//			else
//			{
//				ret.add(var);
//			}
		}
		//System.err.println("\nfin model");
				
		computeDrawAndTubes(ret, cyle);
	}
	
//	private static int getSheetConf(SearchGraph sg, HashSet<Integer> searchsheets)
//	{
//		int maxcomb = 1 << sg.cycles.size();
//		int inactiveConf = 0;//allsheets are inactive
//		for(int i=0; i<maxcomb; i++)
//		{
//			HashSet<Integer> res = new HashSet<Integer>();
//			//for(int j=0; j<sg.sheets.size(); j++)
//			for(int j=0; j<sg.cycles.size(); j++)
//			{
//				if (((i>>j) & 1) == 1)
//				{
//					HashSet<Integer> intersect = new HashSet<Integer>(sg.cycles.get(j).inputs);
//					intersect.retainAll(res);
//					res.addAll(sg.cycles.get(j).inputs);
//					res.removeAll(intersect);
//				}
//			}
//			
//			if(res.equals(searchsheets))
//			{
//				//should be valid sheet configuration
//				//i = inactiveConf;
//				System.out.println("Conf To search " + i + " " + res);
//				return i;
//			}
//		}
//		
//		return 0;
//	}

	public static List<Coordinate> search(String path)
	{
		if(cyle == null)
		{
			cyle = new Cycle2(path);
			cyle.constructSearchGraphs();
		}
		
		//reset
		draw.clear();
		contours.clear();
		tubes.clear();
		
//		for(SearchGraph s : cyle.searches)
//		{
//			for(SATBraid b : s.SATBraids)
//				System.out.println(b);
//			for(SATJunction b : s.SATJunctions)
//				System.out.println(b);
//			for(SATInput b : s.inputNodesToInputVars.values())
//				System.out.println(b);
//		}

		return cyle.braidPoints;
	}

	private static void computeDrawAndTubes(HashSet<SATVar> ret, Cycle2 cyle) {
		//HashSet<SNode> vis = new HashSet<SNode>();

		for(SATVar sn : ret)
		{
			if(!sn.isTube())
			{
				CorrSurface cs = (CorrSurface)sn;
				
				System.out.println(sn.getNumber() + " ]]] sheets? " + cs.sheets.size());			
				
				draw.add(cs.sheets.get(0));
				
//				List<Coordinate> contour = new ArrayList<Coordinate>();
//				for(int n : cs.allnodes)
//				{
//					contour.add(cyle.coords.get(n));
//				}
//
//				contours.add(contour);
				
//				for(SearchGraph sg : cyle.searches)
//				{
//					for(CorrSurface cs : sg.cycles)
//					{
//						if(cs.inputs.equals(((SSheet)sn).inputs))
//						{
//							System.out.println(cs);
//							draw.add(cs.sheets.get(0));//sa imi bag pula ce complicat ajung la asta
//						}
//
//						List<Coordinate> contour = new ArrayList<Coordinate>();
//						for(int n : cs.allnodes)
//						{
//							contour.add(cyle.coords.get(n));
//						}
//
//						contours.add(contour);
//					}
//				}
			}
			else
			{
				HashSet<Integer> vis = new HashSet<Integer>();
				cyle.getTubeCoordinates(sn, sn.vertexSet().iterator().next(), null, tubes, vis);
				
//				if(!vis.contains(sn))
//					drawTubes((SNode)sn, null, tubes, vis);
//				System.out.println("vis " + sn);
			}
		}
	}
//
//	public static void main(String[] args)
//	{
//		System.out.println("Hello World!");
//		
////		HashSet<Integer> t = new HashSet<Integer>();
////		t.add(9);
////		HashSet<Integer> s = new HashSet<Integer>();
////		s.add(16);s.add(20);s.add(30);
////		ArrayList<String> l = getVars("graph.txt", t, s);
////		StringBuilder sb = new StringBuilder();
////		for(String s1 : l)
////		{
////			sb.append(s1);
////			sb.append("\r\n");
////		}
////		System.out.println(sb.toString());
//		
//		//IterateAll.solve(sb.toString());
//		
//		//return;
//
////		/// vizualizare...sa vedem ce iese
////
////		app.init(draw, contours, tubes);
//		//search("19,20", "8,9");
//		//search("9", "16,20,30");
//
////		NewJApplet newj = new NewJApplet();
////		newj.jPanel1.add(app);
////		newj.app = app;
////		app.setBounds(0, 0, 400, 400);
////		newj.setSize(450,450);
////		newj.setVisible(true);
////		newj.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//	}
	
	

	public static List<Coordinate> getJunctionCoordinates()
	{
		return cyle.junctionPoints;
	}

	public static void debugVariable(int varNr)
	{
		//reset
		draw.clear();
		contours.clear();
		tubes.clear();
		
		int[] sol =  null;
		if(varNr == -1)
		{
			sol = new int[]{Correlations.debuggedVar ++};
			if(debuggedVar >= SATVar.currNumber)
				debuggedVar = 1;
		}
		else
		{
			sol = new int[]{varNr};
		}
		
		if(sol != null)
		{
			compCoordForSolution(sol, false);
		}
	}
	
	public static int iterateSolutionVariable(int current)
	{
		//reset
		draw.clear();
		contours.clear();
		tubes.clear();
		
		int[] sol = solver.IterateAll.models.get(solver.IterateAll.index);
		
		int useVariable = -1;
		
		if(sol != null)
		{
			useVariable = current + 1;
			
			int[] partSol = new int[]{sol[useVariable]};
			
			compCoordForSolution(partSol, true);
		}
		
		//reached the end of the solution. go back to the first.
		if(current + 1 == sol.length)
			useVariable = -1;
		
		return useVariable;
	}
	
	public static void sheetAndTubes(int sheetNr)
	{
		//reset
		draw.clear();
		contours.clear();
		tubes.clear();
		
		HashSet<SATVar> l = cyle.findBraidsHavingSheet(sheetNr);
		
		computeDrawAndTubes(l, cyle);
	}
}

/////**
////* RESET
////*/
//////int nrVar = 1; //the start number for variables
////for(SearchGraph sg : cyle.searches)
////sg.vars.clear();
////
////SearchGraph.CNF.clear();
////
////for(SearchGraph sg : cyle.searches)
////{
//////open all contacts
////for(SContact sc : sg.icontacts)
////	sc.setContactMode(-3);
////for(SContact sc : sg.scontacts)
////	sc.setContactMode(-3);
////}
////
/////**
////* Prepare
////*/
//////first search which searchgraphs contain the inputs from sheetSearch
////ArrayList<SearchGraph> forSheets = new ArrayList<SearchGraph>();
////ArrayList<HashSet<Integer>> perSheetInputs = new ArrayList<HashSet<Integer>>();
////for(SearchGraph sg : cyle.searches)
////{
////HashSet<Integer> sgIn = new HashSet<Integer>();
////for(CorrSurface cs : sg.cycles)
////{
////	for(Integer s : sheetSearch)
////		if(cs.inputs.contains(s))
////			sgIn.add(s);
////}
////if(!sgIn.isEmpty())
////{
////	forSheets.add(sg);
////	perSheetInputs.add(sgIn);
////}
////}
////
//////first search which searchgraphs contain the inputs from tubeSearch
////ArrayList<SearchGraph> forTubes = new ArrayList<SearchGraph>();
////ArrayList<HashSet<Integer>> perTubeInputs = new ArrayList<HashSet<Integer>>();
////for(SearchGraph sg : cyle.searches)
////{
////HashSet<Integer> sgIn = new HashSet<Integer>();
////for(CorrSurface cs : sg.cycles)
////{
////	for(Integer s : tubeSearch)
////		if(cs.inputs.contains(s))
////			sgIn.add(s);
////}
////if(!sgIn.isEmpty())
////{
////	forTubes.add(sg);
////	perTubeInputs.add(sgIn);
////}
////}
////
////
//////are there valid configurations of sheets for each searchgraph?
////ArrayList<Integer> sgSheetConf = new ArrayList<Integer>();
////for(int i=0; i < forSheets.size(); i++)
////{
////int conf = getSheetConf(forSheets.get(i), perSheetInputs.get(i));
////sgSheetConf.add(conf);
////}
////
/////**
////* CONSTRUCT SHEET VARS
////*/
//////20.08.2014 - this is misleading. it results in errors when only sheets are searched
//////where to take the sheets from?
////boolean takeSheetsFrom = false;
////if(forSheets.size() > 0)
////takeSheetsFrom = forSheets.get(0).getPrimalStatus();
////
//////where to take the tubes from?
////boolean takeTubesFrom = false;		
////if(forTubes.size() > 0)
////takeTubesFrom = forTubes.get(0).getPrimalStatus();
////
////
////takeSheetsFrom = !takeTubesFrom;
////takeTubesFrom = !takeSheetsFrom;
////
//////for each searchgraph - introduce variables for the cycles
//////these are the same names the SSheets will later use, because the 
//////set of inputs is the key for associating the vars to the geometry
//////for(SearchGraph sg : forSheets)
////for(SearchGraph sg: cyle.searches)
//////for(SearchGraph sg: forSheets)
////{
//////if(sg.getPrimalStatus() == takeSheetsFrom)
////{
////	for(CorrSurface cs : sg.cycles)
////	{
////		SATVar sv = new SATVar(SATVar.SHEET);
////		sv.addAll(cs.getAllNodesAsSet());
//////		sv.tube = false;
////		//sv.number = nrVar++;
////		SearchGraph.vars.add(sv);
////		//sg.vars.add(sv);
////		
////		System.out.println("=== sheet var " + sv.getNumber());
////		System.out.println(sv);
////	}
////}
////}
////
/////**
////* Special case - special case am futut la 19.08.2014
////*/
////if(tubeSearch.isEmpty() && forSheets.size() == 1)
////{
//////caz particular: caut sa vad daca din ciclurile de pe o suprafata bridged pot construi o anumita combinatie de inputuri
////for(int i=0; i<forSheets.get(0).cycles.size(); i++)
////{
////	//SATVar v = SearchGraph.findVar(forSheets.get(0).cycles.get(i).getAllNodesAsSet());
////	SATVar v = SearchGraph.findVar(forSheets.get(0).cycles.get(i).getAllNodesAsSet());
////	System.out.println(forSheets.get(0).cycles.get(i).getAllNodesAsSet());
////	if(((sgSheetConf.get(0)>>i)&1)==1)
////		SearchGraph.CNF.add(v.getNumber() + " 0");	
////	else
////		SearchGraph.CNF.add(-v.getNumber() + " 0");
////}
////return SearchGraph.constructCompleteSet();
////}
////
/////**
////* CONSTRUCT TUBE VARS
////*/
//////foreach searchgraph compute the tube variables
//////for(SearchGraph sg : forTubes)
////for(SearchGraph sg : cyle.searches)
////{
////if(sg.getPrimalStatus() == takeTubesFrom)
////{
////	for(SNode sn : sg.nodes.values())
////	{
////		Set<SNode> visited = new HashSet<SNode>();
////		SearchTarget st = new SearchTarget();
////		sg.dfs(sn, visited, st);
////		
////		SATVar var = new SATVar(SATVar.TUBE);
////		for(SNode visn : visited)
////		{
////			var.add(visn.number);
////		}
////		
////		if(!SearchGraph.vars.contains(var))
////		{
////			//var.number = sg.vars.size() + 1;
////			//var.number = nrVar++;
//////			var.tube = true;
////			SearchGraph.vars.add(var);
////			System.out.println("=== tube var " + var.getNumber());
////			System.out.println(var);
////			System.out.println();	
////		}
////	}
////}
////}
////
/////**
////* Construct the CNF
////* The construction starts from the braiding points, and these are found in the SearchGraph of each sg in forTubes
////*/
////
//////for(SearchGraph sg : forTubes)
////for(SearchGraph sg : cyle.searches)
////{
////if(sg.getPrimalStatus() != takeTubesFrom)
////	continue;
////
//////intai pentru inputuri
////for(SContact sc : sg.icontacts)
////{
////	System.out.println("contact number " + sc.parent.number);
////	//nu verific de null
////	int n1 = SearchGraph.findVar(sc.ngh.get(SNode.LEFT).number).getNumber();
////	int n2 = SearchGraph.findVar(sc.ngh.get(SNode.RIGHT).number).getNumber();
////	
////	if(tubeSearch.contains(sc.parent.number))
////	{
////		SearchGraph.CNF.add(n1 + " " + n2 + " 0");
////		SearchGraph.CNF.add((-n1) + " " + (-n2) + " 0");
////	}
////	else
////	{
////		SearchGraph.CNF.add(n1 + " " + (-n2) + " 0");
////		SearchGraph.CNF.add((-n1) + " " + (n2) + " 0");
////	}
////}
////
//////apoi pentru suprafete
////for(SContact sc : sg.scontacts)
////{
////	int p = SearchGraph.findVar(((SSheet)sc.parent).inputs).getNumber();
////	int n1 = SearchGraph.findVar(sc.ngh.get(SContact.LEFT).number).getNumber();
////	int n2 = SearchGraph.findVar(sc.ngh.get(SContact.RIGHT).number).getNumber();
////	
////	SearchGraph.CNF.add(p + " " + n1 + " " + (-n2) + " 0");
////	SearchGraph.CNF.add(p + " " + (-n1) + " " + n2 + " 0");
////	SearchGraph.CNF.add((-p) + " " + n1 + " " + n2 + " 0");
////	SearchGraph.CNF.add((-p) + " " + (-n1) + " " + (-n2) + " 0");
////}
////}
////
/////**
////* For having the CNF complete, the sheet configurations have to be specified 
////*/
////for(int i=0; i<forSheets.size(); i++)
////{
////for(int si=0; si<forSheets.get(i).cycles.size(); si++)
////{
////	if(((sgSheetConf.get(i)>>si)&1)==1)
////	{
////		SATVar v = SearchGraph.findVar(forSheets.get(i).cycles.get(si).getAllNodesAsSet());
////		SearchGraph.CNF.add(v.getNumber() + " 0");
////	}
////}
////}
//
//
//////intr-o prima faza trebuie sa vad de exista vreun searchgraph care contine integral sheeturile cautate
//////asta ar corespunde cu un bridged geometry
//////sunt cazuri in care vreau sa vad daca o suprafata exista, dar ea nu este pur si simplu braided
//////iar atunci nu apare nicaieri
////if(tubeSearch.isEmpty())
////{
//////dar exista o configuratie de sheeturi - plec de la prezumtia ca e o singura componenta
////for(SearchGraph sg : cyle.searches)
////{
////	for(Integer s : sheetSearch)
////	{
////		for(SInput in : sg.inputs.values())
////		{
////			if(in.number == s)
////				System.out.println("contains " + s);
////		}
////	}
////}
////}
////
//////urmatoarele parti o sa ma distruga - va trebui sa le reprogramez?
//////findSearchGraph ar trebui sa fie findSearchGraphs
//////deoarece sunt mai multe componente ale circuitului care pot contine rahaturile astea de variabile
////
//////partea cu sat o fac acolo unde sunt braiduri, practic unde mai multe sg-uri sunt legate prin braiduri
//////faptul ca de aici in jos folosesc un singur searchgraph este din cauza ca am presupus tot timpul ca exista o
//////singura geometrie primala si una singura duala...lucru neadevarat in general...insa las deocamdata asa
////
////SearchGraph sg = cyle.findSearchGraph(tubeSearch, sheetSearch);
////
////int sheetConfiguration = getSheetConf(sg, sheetSearch);
////if(sheetConfiguration == 0)
////{
////System.out.println("No existing sheet configuration (do it with SAT?)");
////return null;
////}
////
////
//////for(SearchGraph sg : cyle.searches)
////{
//////reset
////SearchGraph.vars.clear();
////SearchGraph.CNF.clear();
////
//////open all contacts
////for(SContact sc : sg.icontacts)
////	sc.setContactMode(-3);
////for(SContact sc : sg.scontacts)
////	sc.setContactMode(-3);
////
////for(SNode sn : sg.nodes.values())
////{
////	Set<SNode> visited = new HashSet<SNode>();
////	SearchTarget st = new SearchTarget();
////	sg.dfs(sn, visited, st);
////	
////	SATVar var = new SATVar();
////	for(SNode visn : visited)
////	{
////		var.add(visn.number);
////	}
////	
////	if(!SearchGraph.vars.contains(var))
////	{
////		//var.number = sg.vars.size() + 1;
////		var.number = nrVar++;
////		var.tube = true;
////		SearchGraph.vars.add(var);
////		System.out.println("=== tube var " + var.number);
////		System.out.println(var);
////		System.out.println();	
////	}
////}
////
//////am gasit variabilele pentru sg-ul asta. acuma sa formam clauze
//////nu unesc sg-urile deoarece ar iesi o formula mult prea complicata
////
//////adauga sheeturile ca variabile
//////for(SSheet sh : sg.sheets)
//////{
//////	SATVar sv = new SATVar();
//////	sv.addAll(sh.inputs);
//////	sv.tube = false;
//////	sv.number = nrVar++;
//////	SearchGraph.vars.add(sv);
//////	
//////	System.out.println("=== sheet var " + sv.number);
//////	System.out.println(sv);
//////}
////
//////intai pentru inputuri
////for(SContact sc : sg.icontacts)
////{
////	System.out.println("contact number " + sc.parent.number);
////	//nu verific de null
////	int n1 = SearchGraph.findVar(sc.ngh.get(SNode.LEFT).number).number;
////	int n2 = SearchGraph.findVar(sc.ngh.get(SNode.RIGHT).number).number;
////	
//////	sg.CNF.add(n1 + " " + n2 + " 0");
//////	sg.CNF.add((-n1) + " " + (-n2) + " 0");
////	
////	if(tubeSearch.contains(sc.parent.number))
////	{
////		SearchGraph.CNF.add(n1 + " " + n2 + " 0");
////		SearchGraph.CNF.add((-n1) + " " + (-n2) + " 0");
////	}
////	else
////	{
////		SearchGraph.CNF.add(n1 + " " + (-n2) + " 0");
////		SearchGraph.CNF.add((-n1) + " " + (n2) + " 0");
////	}
////}
////
//////apoi pentru suprafete
////for(SContact sc : sg.scontacts)
////{
////	int p = SearchGraph.findVar(((SSheet)sc.parent).inputs).number;
////	int n1 = SearchGraph.findVar(sc.ngh.get(SContact.LEFT).number).number;
////	int n2 = SearchGraph.findVar(sc.ngh.get(SContact.RIGHT).number).number;
////	
//////	sg.CNF.add(p + " " + n1 + " 0");
//////	sg.CNF.add(p + " " + n2 + " 0");
//////	sg.CNF.add(n1 + " " + n2 + " 0");
//////	sg.CNF.add(p + " " + n1 + " " + (-n2) + " 0");
//////	sg.CNF.add((-p) + " " + (-n1) + " " + (-n2) + " 0");
////	SearchGraph.CNF.add(p + " " + n1 + " " + (-n2) + " 0");
////	SearchGraph.CNF.add(p + " " + (-n1) + " " + n2 + " 0");
////	SearchGraph.CNF.add((-p) + " " + n1 + " " + n2 + " 0");
////	SearchGraph.CNF.add((-p) + " " + (-n1) + " " + (-n2) + " 0");
////}
////
////
////for(int i=0; i<sg.sheets.size(); i++)
////{
////	if(((sheetConfiguration>>i)&1)==1)
////	{
////		SATVar v = SearchGraph.findVar(sg.sheets.get(i).inputs);
////		SearchGraph.CNF.add(v.number + " 0");
////		System.out.println("this sheet " + v);
////	}
////}
////
//////cautarea variabilei care corespunde unei suprafete
//////se bazeaza pe faptul ca exista toate suprafetele
//////si nu doar chestia aia cum o numesc eu cu log din numarul lor
//////aici mai e de munca...deocamdata pentru exemplul meu merge?
////
//////System.out.println(sg.CNF);
//////conditie de cacat
//////if(sg.vars.size() > 0)
//////	break;
////}
////