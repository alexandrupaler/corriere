import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class DrawInputs extends ArrayList<Integer>{
	
	private int activeIndex = -1;
	
	public HashMap<Integer, String> names = new HashMap<Integer, String>();
	
	public HashMap<Integer, String> getNames()
	{
		HashMap<Integer, String> ret = new HashMap<Integer, String>();
		ret.putAll(names);
		
		//injection/inputurile care au un nume este salvat
		//alea care nu au nume primesc id-ul punctului
		for(Integer i : this)
		{
			if(!names.keySet().contains(i))
			{
				ret.put(i+1, (i+1)+"");
			}
		}
		
		return ret;
	}
	
	public String getName(Integer id)
	{
		String name = names.get(id);
		return  name == null ? "" : name;
	}
	
	public void  putName(Integer id, String newname)
	{
		names.put(id, newname);
	}
	
	public void updateActiveIndex(String name)
	{
		if(activeIndex != -1)
			names.put(activeIndex, name);
	}
	
	public String getActiveIndex()
	{
		if(activeIndex != -1)
			return getName(activeIndex);
		
		return "";
	}
	
	public void toggleActiveIndex(int index)
	{
		activeIndex = index;
	}
	
	public boolean isActiveIndex()
	{
		return (activeIndex != -1);
	}
	
	public void shiftUp(int index)
	{
		shift(index, +1);
	}
	
	public void shiftDown(int index)
	{
		shift(index, -1);
	}
	
	private void shift(int fromPos, int direction)
	{
		for(int i=0; i<this.size(); i++)
		{
			int e = (int)this.get(i);
			
			String name = names.get(e);
			names.remove(e);
			
			if(e >= fromPos)
				e+= direction;
			
			this.set(i, e);
			//pune index negativ, incat sa nu existe coliziune
			names.put(-e, name);
		}
		
		HashSet<Integer> negkeys = new HashSet<Integer>();
		for(Integer k : names.keySet())
		{
			if(k < 0)
			{
				names.remove(-k);
				negkeys.add(k);
			}
		}
		for(Integer nk : negkeys)
		{
			String name = names.get(nk);
			names.put(-nk, name);
		}
	}

	public int getIdForName(String label) {
		//trebuie sa am grija. pana acuma desenez cu id-uri incepand de la 0, 
		//iar in vechiul prog am id-uri incepand cu 1
		for(Integer s : this)
		{
			if (label.equals(names.get(s)))
				//return s;
				//de aceea aici face s+1, pentru ca merg in softul vechi
				return s+1;
		}
		return -1;
	}
}
