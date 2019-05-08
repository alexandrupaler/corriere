package corr;
import java.util.ArrayList;
import java.util.HashSet;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.Subgraph;


/**
 * 
 * @author alexandru
 * Un SATVar poate fi de doua tipuri
 * pentru inputuri sau pentru braiduri
 * fiecare variabila are un numar
 */

//public class SATVar extends ArrayList<Integer> {
public class SATVar extends Subgraph<Integer, DefaultEdge, UndirectedGraph<Integer,DefaultEdge>>{
	
	public final static boolean TUBE = true;
	public final static boolean SHEET = false;
	
	protected static int currNumber = 1; 
	
	private int number;
	private boolean tube = true;
	
	public SATVar(UndirectedGraph<Integer,DefaultEdge> graph, ArrayList<Integer> s, boolean istube)
	{
		super(graph, new HashSet<Integer>(s));
		tube = istube;
		number = currNumber++;
	}
	
//	public SATVar(boolean istube)
//	{
//		super();
//		tube = istube;
//		number = currNumber++;
//	}
	
	public void negate()
	{
		number *= -1;
	}
	
	public Integer getNumber()
	{
		return number;
	}

	public boolean isTube() {
		return tube;
	}
	
	@Override
	public boolean equals(Object a)
	{
		//un rahat, dar din cauza ca corrsurface extends asta,
		//si acolo imi trebuie ordinea parcurgerii nodurilor ajung la tampenia asta
//		HashSet<Integer> tmp = new HashSet<Integer>(this);
//		return tmp.equals(new HashSet<Integer>((ArrayList<Integer>)a));
		return this.equals((UndirectedGraph<Integer, DefaultEdge>)a);
	}

	public static int getNumberOfVars() {
		return currNumber;
	}
}
