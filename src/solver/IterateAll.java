package solver; 

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.sat4j.core.*;
import org.sat4j.minisat.*;
import org.sat4j.reader.*;
import org.sat4j.specs.*;
import org.sat4j.tools.*;

public class IterateAll
{
    public static List<int[]> models = null;
    public static int index = 0;
    
//	static String readFile(String path, Charset encoding) 
//	  throws IOException 
//	{
//	  byte[] encoded = Files.readAllBytes(Paths.get(path));
//	  return encoding.decode(ByteBuffer.wrap(encoded)).toString();
//	}
	
	public static int[] solve(String cnfstring)
	{
        try {
            if(!cnfstring.isEmpty())
	        {
            	ISolver solver = SolverFactory.newDefault();
	    	    ModelIterator mi = new ModelIterator(solver);
	    	    IProblem problem;
	    	    
	    	    solver.setTimeout(3600); // 1 hour timeout
    	    
            	//System.out.println(cnfstring);
            	LecteurDimacs in = new LecteurDimacs(mi); 
            	InputStream ins = new ByteArrayInputStream(cnfstring.getBytes("US-ASCII"));
            	problem = in.parseInstance(ins);
            	
            	models = new ArrayList<int[]>();
            	index = -1;
//            	if(problem.isSatisfiable())
        		while(problem.isSatisfiable())
            	{
            		models.add(problem.model());
//            		for(int i=0; i<models.get(models.size() - 1).length; i++)
//            			System.out.print(models.get(models.size() - 1)[i]+" ");
//            		System.out.println(models.size() + "-----");
            	}
            	solver.expireTimeout();
            	
            }
            if(models.size() > 0 && index < models.size())
            {
            	if(index + 1 == models.size())
            	{
            		index = -1;
            	}
            	index++;
//            	
//              System.out.println("== MODEL: ");
//              for(int i=0; i<models.get(index).length; i++)
//            	  if(models.get(index)[i] > 0)
//            		  System.out.print(models.get(index)[i]+" ");
//              System.out.println();
            	
            	return models.get(index);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
	} 
	
//	public static void main(String[] args)
//	{
//		try {
//			solve(readFile("/home/alexandru/minisat/stab.cnf", StandardCharsets.US_ASCII));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
