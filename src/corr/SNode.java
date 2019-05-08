package corr;
import java.util.ArrayList;
import java.util.List;


public class SNode{
	public static int LEFT = 0;
	public static int RIGHT = 1;
	
	public List<SNode> ngh = new ArrayList<SNode>();
	public List<SContact> contacts = new ArrayList<SContact>();
	public Coordinate co;
	
	public int number = Integer.MAX_VALUE;
	
	public SNode()
	{
		
	}
	
	public SNode(Coordinate c)
	{
		co = new Coordinate(c);
	}
	
	public List<SNode> visit(SearchTarget target)
	{
		System.out.println("not implemented! returns the list of neighbours (both visited and unvisited)");
		return null;
	}
	
	public void addNeighbor(SNode ng)
	{
		this.ngh.add(ng);
	}
	
	public void removeNeighbor(SNode ng)
	{
		this.ngh.remove(ng);
	}
	
	public boolean equals(SNode other)
	{
		return co.equals(other.co);
	}
	
	public SContact createContact(Coordinate c)
	{
		if(this.getClass() == SNode.class)
			return null;
		
		SContact ct = new SContact(c, this);
		
		//addNeighbor(ct);//??? de ce
		//deoarece contactele sunt copii unui nod de tip input sau sheet
		//dar asta e bine?
		
		attachContact(ct);
		
		return ct;
	}
	
	public void attachContact(SContact ct)
	{
		contacts.add(ct);
		//ct.addNeighbor(this);	
	}
	
	public void setContactModes(int[] modes)
	{
		if(this.getClass() == SNode.class)
			return;
		
		for(int i=0; i<modes.length; i++)
		{
			SContact snc = (SContact)contacts.get(i);
			//if(snc.parent == this)//adica daca sheetul asta este cel care este parintele acestui contact
				snc.setContactMode(modes[i]);
		}
	}
	
	public String toString()
	{
		return co.toString() + "%"+number;
	}
	
	public void removeLeft()
	{
		ngh.set(LEFT, null);
	}
	
	public void removeRight()
	{
		ngh.set(RIGHT, null);
	}
	
	public void addLeft(SNode n)
	{
		ngh.set(LEFT, n);
	}
	
	public void addRight(SNode n)
	{
		ngh.set(RIGHT, n);
	}
}
