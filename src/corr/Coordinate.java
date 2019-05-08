package corr;
import java.util.Arrays;

import com.sun.org.apache.bcel.internal.generic.InstructionComparator;


public class Coordinate{

	public int[] coord = new int[3];
	
	public Coordinate()
	{
		coord[0] = coord[1] = coord[2] = 0;
	}
	
	public Coordinate(int[] c)
	{
		this.coord = Arrays.copyOf(c, 3);
	}
	
	public Coordinate(double[] c)
	{
		for(int i=0; i<3; i++)
			this.coord[i] = (int)c[i];
	}
	
	public Coordinate(Coordinate c)
	{
		this(c.coord);
	}
	
	public int[] findConstCoord1(Coordinate other)
	{
		int ct[] = {-1, -1, 0};//last element is number of elements in array
		
		for(int i=0; i<3; i++)
		{
			if(coord[i] == other.coord[i])
				ct[ct[2]++] = i;
		}
		
		return ct;
	}
	
	public int findConstCoord2(Coordinate other1, Coordinate other2)
	{
		int[] ct1 = findConstCoord1(other1);
		int[] ct2 = findConstCoord1(other2);
		
		for(int i=0; i<ct1[2]; i++)
		{
			for(int j=0; j<ct2[2]; j++)
			{
				if(ct1[i] == ct2[j])
					return ct1[i];
			}
		}
		
		return -1;
	}
	
	public String toString()
	{
		String s = "[x:"+coord[0]+",y:"+coord[1]+",z:"+coord[2]+"]";
		return s;
	}
	
	//Segments are constructed from coordinates that have only one coordinate different
	public Coordinate computeOffset(Coordinate other)
	{
		Coordinate ret = new Coordinate();
		
		for(int i=0; i<3; i++)
			ret.coord[i] = coord[i] - other.coord[i];
		
		return ret;
	}
	
	public void addOffset(Coordinate off)
	{
		for(int i=0; i<3; i++)
			coord[i] += off.coord[i];	
	}
	
	//Segments are constructed from coordinates that have only one coordinate different
	public int getSegmentLength(Coordinate other)
	{
		int dist = 0;
		for(int i=0; i<3; i++)
		{
			dist += (coord[i] - other.coord[i]);
		}
		
		return dist;
	}
	
	public int getDirection(Coordinate other)
	{
		Coordinate d = computeOffset(other);
		int r = -1;
		
		for(int i=0; i<3; i++)
		{
			if(d.coord[i] != 0)
			{
				r = 2*i;
			}
			if(d.coord[i] < 0)
			{
				r = r + 1;//0,2,4 sunt directii pozitive, 1,3,5 sunt negative - nu mai tin minte cum le aveam in C
			}
		}
		
		return r;
	}
	
	public boolean equals(Coordinate other)
	{
		return ((coord[0]==other.coord[0]) && (coord[1]==other.coord[1]) && (coord[2]==other.coord[2]));
	} 
}
