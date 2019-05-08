package corr;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Rectangle {
	Coordinate[] corners = new Coordinate[2];
	
	public Rectangle(Rectangle rect)
	{
		corners[0] = rect.corners[0];
		corners[1] = rect.corners[1];
	}
	
	public Rectangle(Coordinate c1, Coordinate c2, Coordinate offset)
	{
		if(c1.findConstCoord1(c2)[2] == 2)//the points are colinear
		{
			corners[0] = new Coordinate(c1);
			corners[1] = new Coordinate(c2);
			corners[1].addOffset(offset);
		}
		else
		{
			System.out.println("points are not colinear:");
			System.out.println(c1);
			System.out.println(c2);
		}
	}
	
	public Rectangle(Coordinate c1, Coordinate c2)
	{
		if(c1.findConstCoord1(c2)[2] == 2)
		{
			//the points are colinear -> not a valid rectangle
			System.out.println("points are colinear:");
			System.out.println(c1);
			System.out.println(c2);
		}
		corners[0] = new Coordinate(c1);
		corners[1] = new Coordinate(c2);
	}
	
	public int getConstCoord()
	{
		return corners[0].findConstCoord1(corners[1])[0];
	}
	
	public int[] getNonConstCoord()
	{
		int c = getConstCoord();
		int ret[] = {(c + 1)%3, (c+2)%3};
		if(ret[0] > ret[1])
		{
			int x = ret[1];
			ret[1] = ret[0];
			ret[0] = x;
		}
		
		return ret;
	}
	
	public int[] getMinMax()
	{
		int[] minmax = new int[4];
			
		int[] ci = getNonConstCoord();
		
		minmax[0] = corners[0].coord[ci[0]];
		minmax[1] = corners[1].coord[ci[0]];
		minmax[2] = corners[0].coord[ci[1]];
		minmax[3] = corners[1].coord[ci[1]];
		
		if(minmax[0] > minmax[1])
		{
			int ex = minmax[1];
			minmax[1] = minmax[0];
			minmax[0] = ex;
		}

		if(minmax[2] > minmax[3])
		{
			int ex = minmax[3];
			minmax[3] = minmax[2];
			minmax[2] = ex;
		}

		return minmax;
	}
	
	public Coordinate[] getFourPoints()
	{
		Coordinate ret[] = new Coordinate[4];

		int mm[] = getMinMax();
		int[] ci = getNonConstCoord();
		int ct = getConstCoord();
		int val = corners[0].coord[ct];
		
		for(int i=0; i<4; i++)
		{
			ret[i] = new Coordinate();
			ret[i].coord[ci[0]] = mm[(i%2 + (i/2))%2];
			ret[i].coord[ci[1]] = mm[2 + i/2];
			ret[i].coord[ct] = val;
		}
		
		return ret;
	}
	
	public boolean containsPoint(double[] p)
	{
		int[] minmax = getMinMax();
		int[] ci = getNonConstCoord();
		
		if(p[ci[0]] > minmax[0] && p[ci[0]] < minmax[1]
				&& p[ci[1]] > minmax[2] && p[ci[1]] < minmax[3])
		{
			return true;
		}
		return false;
	}
	
	public boolean containsPoint(Coordinate point)
	{
		int[] minmax = getMinMax();
		int[] ci = getNonConstCoord();
		
		if(point.coord[ci[0]] > minmax[0] && point.coord[ci[0]] < minmax[1]
				&& point.coord[ci[1]] > minmax[2] && point.coord[ci[1]] < minmax[3])
		{
			return true;
		}
		return false;
	}
	
	public List<List<Rectangle>> cutWithRectangle(Rectangle rect)
	{
		int ctc = getConstCoord();
		int ctr = rect.getConstCoord();
		
		List<List<Rectangle>> res = new ArrayList<List<Rectangle>>();
		res.add(new ArrayList<Rectangle>());
		res.add(new ArrayList<Rectangle>());
		
		if(ctc != ctr)//dreptunghiurile nu sunt paralele
		{
			res.get(0).add(this);
			res.get(1).add(rect);
			return res;
		}
		if(corners[0].coord[ctc] != rect.corners[0].coord[ctr])
		{
			//dreptunghiurile nu sunt in acelasi plan
			res.get(0).add(this);
			res.get(1).add(rect);
			return res;
		}
		
		int[] ci = getNonConstCoord();
		int[] mm1 = getMinMax();
		int[] mm2 = rect.getMinMax();
		
		ArrayList<Integer> fcoord = new ArrayList<Integer>();
		fcoord.add(mm1[0]);fcoord.add(mm1[1]);fcoord.add(mm2[0]);fcoord.add(mm2[1]);
		//nu am gasit mod mai usor de a insera
		Collections.sort(fcoord);
		
		ArrayList<Integer> scoord = new ArrayList<Integer>();
		scoord.add(mm1[2]);scoord.add(mm1[3]);scoord.add(mm2[2]);scoord.add(mm2[3]);
		//nu am gasit mod mai usor de a insera
		Collections.sort(scoord);
		
		//Coordinate middle = new Coordinate();
		//p1.coord[ctc] = p2.coord[ctc] = middle.coord[ctc] = corners[0].coord[ctc];

		for(int i=0; i<3; i++)
		{
			for(int j=0; j<3; j++)
			{
				Coordinate p1 = new Coordinate();
				Coordinate p2 = new Coordinate();
				p1.coord[ctc] = p2.coord[ctc] = corners[0].coord[ctc];
				
				p1.coord[ci[0]] = fcoord.get(i);
				p1.coord[ci[1]] = scoord.get(j);
				p2.coord[ci[0]] = fcoord.get(i+1);
				p2.coord[ci[1]] = scoord.get(j+1);

				double middle[] = new double[3];
				middle[ci[0]] = (p1.coord[ci[0]] + p2.coord[ci[0]])/2.0;
				middle[ci[1]] = (p1.coord[ci[1]] + p2.coord[ci[1]])/2.0;
				
				boolean frect = containsPoint(middle);
				boolean srect = rect.containsPoint(middle);
				//System.out.println(p1 + " " + p2);
				//System.out.println("middle: " + middle[0] +" " +middle[1] +" " +middle[2] +" " + frect + srect);
	/*			printCoord(p1);
				printCoord(p2);
				printCoord(middle);

				printf("--- %d %d\n", frect, srect);
	*/
				Rectangle nrect = null;
				if(frect != srect && frect == true)
				{
					nrect = new Rectangle(p1, p2);
					if(!nrect.isEmpty())
					{
						res.get(0).add(nrect);
						//System.out.println("-> " + nrect);
					}
				}
				else if(frect != srect && srect == true)
				{
					//startstop rect = makeRectFromPoints(p1, p2, ctcoord);
					nrect = new Rectangle(p1, p2);
					if(!nrect.isEmpty())
					{
						res.get(1).add(nrect);
						//System.out.println("-> " + nrect);
					}
				}
			}
		}
				
		return res;
	}
	
	public boolean isEmpty()
	{
		return corners[0].equals(corners[1]);
	}
	
	public boolean equals(Rectangle other)
	{
		if(getConstCoord() != other.getConstCoord())
			return false;
		int mm1[] = getMinMax();
		int mm2[] = other.getMinMax();
		for(int i=0; i<mm1.length; i++)
			if(mm1[i] != mm2[i])
				return false;

		return true;
	}
	
	public String toString()
	{
		String s = "Rectangle [" + corners[0].toString() + " # " + corners[1].toString() + "]";
		return s;
	}
	
	public Coordinate intersects(Coordinate[] p)
	{		
		int ctc[] = p[0].findConstCoord1(p[1]);
		
		//is the rectangle of opposite type with the sheet?
//		int evenr = corners[0].coord[0] % 2;
//		int evenl = p[0].coord[0] % 2;
//		if(evenl == evenr)
//			return null;//are of same type, because the coordinates are both even, or both odd
		
		int[] rctc = getNonConstCoord();
		for(int i=0; i<2; i++)
			if(rctc[i] != ctc[i])
				return null;
	
		//daca coordonata constanta a dreptunghiului se afla intre capetele neconstante ale liniei
		if(p[0].coord[getConstCoord()] < corners[0].coord[getConstCoord()]
		     && p[1].coord[getConstCoord()] < corners[0].coord[getConstCoord()])
			return null;
		if(p[0].coord[getConstCoord()] > corners[0].coord[getConstCoord()]
 		     && p[1].coord[getConstCoord()] > corners[0].coord[getConstCoord()])
 			return null;
		
		//daca coordonatele constante ale liniei sunt in dreptunghi
		Coordinate pix = new Coordinate(p[0]);
		pix.coord[getConstCoord()] = corners[0].coord[getConstCoord()];
		
		if(containsPoint(pix))
			return pix;
		
		return null;
	}
}
