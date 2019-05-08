package corr;

import java.util.ArrayList;

import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;

public class EdgeTraversalCollection implements TraversalListener<Integer, DefaultEdge>
{
	ArrayList<DefaultEdge> list = new ArrayList<DefaultEdge>();
	public ArrayList<DefaultEdge> getEdgeList()
	{
		return list;
	}
	
	@Override
	public void vertexTraversed(VertexTraversalEvent<Integer> arg0) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void vertexFinished(VertexTraversalEvent<Integer> arg0) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void edgeTraversed(EdgeTraversalEvent<Integer, DefaultEdge> arg0) {
		//System.out.println("traversez" + arg0.getEdge());
		list.add(arg0.getEdge());
	}
	
	@Override
	public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {
		// TODO Auto-generated method stub
	}
}
