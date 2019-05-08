import java.util.HashSet;


public class SearchParameters{
	public HashSet<Integer> xSearch = new HashSet<Integer>();
	public HashSet<Integer> zSearch = new HashSet<Integer>();
	public String name;
	
	@Override
	public String toString()
	{
		return name;
	}
}
