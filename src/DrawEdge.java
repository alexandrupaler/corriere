import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class DrawEdge extends ArrayList<int[]> 
{
	public DrawInputs din = null;
	
	public DrawEdge findEdges(int node)
	{
		DrawEdge ret = new DrawEdge();
		for(int[] e : this)
		{
			if(e[0] == node || e[1] == node)
			{
				ret.add(e);
			}
		}
		
		return ret;
	}
	
	public HashSet<Integer> getNeighbours(int node)
	{
		HashSet<Integer> ret = new HashSet<Integer>();
		
		DrawEdge edges = findEdges(node);
		for(int[] e : edges)
		{
			ret.add(e[0] != node ? e[0] : e[1]);
		}
		
		return ret;
	}
	
	public List<Integer> getConnectedComponent(Integer node)
	{
		ArrayList<Integer> ret = new ArrayList<Integer>();
		
		HashSet<Integer> vis = new HashSet<Integer>();
		dfs(node, vis);
		ret.addAll(vis);
		
		return ret;
	}
	
	public DrawEdge getConnectedComponentAsDrawEdge(Integer node)
	{
		DrawEdge ret = new DrawEdge();
		List<Integer> cc = getConnectedComponent(node);
		for(Integer n : cc)
		{
			ret.addAll(findEdges(n));
		}
		return ret;
	}
	
	private void dfs(Integer vis, Set<Integer> visited)
	{
		if(visited.contains(vis))
		{
			return;
		}
		visited.add(vis);
		
		HashSet<Integer> ngh = getNeighbours(vis);
		for(Integer n : ngh)
			dfs(n, visited);
	}
	
	public void removeEdge(int[] edge)
	{
		for(int[] e : this)
		{
			if((e[0]==edge[0] && e[1] == edge[1])
					|| (e[0]==edge[1] && e[1] == edge[0]))
			{
				this.remove(e);
				return;
			}
		}
	}
	
	public int[][] getIntIntArray()
	{
		return this.toArray(new int[0][]);
	}
	
	public void addEdge(int fromPos)
	{
		for(int i=0; i<this.size(); i++)
		{
			int[] e = this.get(i);
			if(e[0] >= fromPos)
				e[0]++;
			if(e[1] >= fromPos)
				e[1]++;
			
			this.set(i, e);
		}
		
		this.add(new int[]{fromPos, fromPos + 1});
		
		if(din != null)
			din.shiftUp(fromPos);

	}
	
	public double[][] getEdgeColors(List<double[]> points)
	{
		List<double[]> ret = new ArrayList<double []>();
		
		for(int[] e : this)
		{
//			int ie = (int)points.get(e[0])[0];
//			boolean primal = (Math.abs(ie))%2 == 0;
			
			boolean primal = VisProps.isPrimal(points.get(e[0])) || VisProps.isPrimalInput(points.get(e[0])); 
			ret.add(primal ? VisProps.primalEdgeColor : VisProps.dualEdgeColor);
		}
		return ret.toArray(new double[0][]);
	}

	public void shiftDown(int index) {
		for(int i=0; i<this.size(); i++)
		{
			int[] e = this.get(i);
			if(e[0] > index)
				e[0]--;
			if(e[1] > index)
				e[1]--;
			this.set(i, e);
		}
		
		if(din != null)
			din.shiftDown(index);
	}
	
	public boolean containsPoint(List<double[]> points, double[] p)
	{
		boolean ret = false;
		Vis.pa(p, "punctul");
		for(int[] e : this)
		{
			ret = ret || VisProps.areColinear(points.get(e[0]), points.get(e[1]), p);
		}
		
		return ret;
	}
}
