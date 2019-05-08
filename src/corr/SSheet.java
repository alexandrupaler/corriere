package corr;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;


public class SSheet extends SNode
{
	HashSet<Integer> inputs;
	public boolean enabled = true;
	
	public SSheet(Set<Integer> inputs)
	{
		super();
		this.inputs = new HashSet<Integer>(inputs);
	}

	public boolean equals(SSheet other)
	{
		return inputs.equals(other.inputs);
	}
	
	public String toString()
	{
		return inputs.toString();
	}
	
	public void resetVisitedFlagOnContacts()
	{
		for(SNode c : ngh)
		{
			((SContact)c).visited = false;
		}
	}
}
