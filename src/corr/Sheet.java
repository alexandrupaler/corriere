package corr;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.text.DefaultEditorKit.InsertBreakAction;

public class Sheet extends ArrayList<Rectangle>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public boolean markdelete = false;

	public Sheet()
	{
		super();
	}
	
	public Sheet(Sheet sh)
	{
		for(Rectangle r : sh)
		{
			add(new Rectangle(r));
		}
	}
	
	public boolean add(Sheet sht)
	{
		for(Rectangle rct : sht)
			add(rct);
		return true;
	}
	
	public boolean add(Rectangle rct)
	{
		//System.out.println("add: " + rct);
		if(size() == 0)
		{
			super.add(rct);
			return true;
		}
		
		List<Rectangle> rects = xor(rct);

		//rectangles.insert(it, res[0].begin(), res[0].end());
		//it = rectangles.erase(it);
		//it--;
		
		//list<startstop>::iterator prev = itr;
		//rects.insert(++itr, res[1].begin(), res[1].end());
		//itr = prev;
		//itr = rects.erase(itr);
				
		//rectangles.insert(rectangles.end(), rects.begin(), rects.end());
		this.addAll(rects);
		
		//System.out.println(this);
		
		return true;
	}

	private List<Rectangle> xor(Rectangle rct) 
	{
		List<Rectangle> rects = new ArrayList<Rectangle>();
		rects.add(rct);
		
		int idxRects = 0;
		while(idxRects < rects.size())
		{
			Rectangle prevr = rects.get(idxRects);
			int idxThis = 0;
			while(idxThis < this.size() && rects.size() > 0 && idxRects < rects.size())
			{
				List<List<Rectangle>> res = this.get(idxThis).cutWithRectangle(rects.get(idxRects));
				
				this.remove(idxThis);
				this.addAll(idxThis, res.get(0));
				idxThis += res.get(0).size();
				
				rects.remove(idxRects);
				rects.addAll(idxRects, res.get(1));
			}
			if(rects.size() > 0 && idxRects < rects.size() && prevr.equals(rects.get(idxRects)))
				idxRects++;
		}
		
		return rects;
	}
	
	public int countEmpty()
	{
		int nr = 0;
		for(Rectangle r : this)
			if(r.isEmpty())
				nr++;
		return nr;
	}
	
	public Sheet substract(Sheet other)
	{
		Sheet ret = new Sheet(this);
		for(Rectangle r : other)
		{
			ret.xor(r);
		}
		return ret;
	}
	
	public boolean includes(Sheet other)
	{
		Sheet diff1 = other.substract(this);
		
		return diff1.size() == 0;
	}
	
	public boolean equals(Sheet other)
	{
		Sheet diff1 = other.substract(this);
		Sheet diff2 = this.substract(other);
		
		if(diff1.size() == 0 && diff2.size() == 0)//means the sheets are equal
			return true;
		
		return false;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("-- Sheet: {");
		for(Rectangle r : this)
			sb.append(r.toString() + "\n");
		sb.append("}\n");
		return sb.toString();
	}
}
