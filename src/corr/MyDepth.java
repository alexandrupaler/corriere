package corr;

import java.util.ArrayList;

import org.jgrapht.Graph;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.traverse.DepthFirstIterator;


public class MyDepth<V, E> extends DepthFirstIterator<V, E> {

	public V target = null;
	public int nr = 0;
	public ArrayList<V> init = null;
	
	@Override
	protected V provideNextVertex()
	{
		if(nr < init.size())
		{
			return init.get(nr++);
		}
		return super.provideNextVertex();
	}
	 
	public MyDepth(Graph<V, E> g, ArrayList<V> init)
	{
		super(g);
		this.init = new ArrayList<V>(init);
		
		//super(g, init.get(0));
//		encounterVertex(init.get(0), null);
//
//		for(int i=0; i<init.size()-1; i++)
//		{
//			E e = g.getEdge(init.get(i), init.get(i+1));
//			encounterVertex(init.get(i+1), e);
//		}
	}
	
	@Override protected void encounterVertexAgain(V vertex, E edge)
	{
		VisitColor color = getSeenData(vertex);
		if (color != VisitColor.WHITE && vertex.equals(init.get(0))) {
		// We've already visited this vertex; no need to mess with the
		// stack (either it's BLACK and not there at all, or it's GRAY
		// and therefore just a sentinel).
			return;
		}
		super.encounterVertex(vertex, edge);
	}
	
//	public ArrayList<V> getPath ()
//	{
//		ArrayList<V> visi = new ArrayList<V>();
//		while(this.hasNext())
//		{
//			V curr = next();
//			if(visi.size() >0 && visi.get(0).equals(curr))
//			{
//				//return visi;
//				return getSeenData(vertex)
//			}
//			visi.add(curr);
//		}
//		return null;
//	}
}
