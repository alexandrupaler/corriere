package corr;
import java.io.*;
import java.util.ArrayList;


public class SATRunner {

	public int[] runSAT(ArrayList<String> cnf, int nrvars)
	{
		createCnfFile(cnf, nrvars);

		try {
			Process process = new ProcessBuilder(
					"./minisat","cnf.cnf","cnf.cnf.out").start();
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;


			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return readCnfOutput();
	}

	private int[] readCnfOutput()
	{
		try {
			BufferedReader br = new BufferedReader(new FileReader("cnf.cnf.out"));

			String line = br.readLine();
			if(!line.trim().equals("SAT"))
				return null;//unsat
			
			line = br.readLine();
			
			String[] vars = line.split(" ");
			int varints[] = new int[vars.length];
			for(int i=0; i < varints.length; i++)
				varints[i] = Integer.parseInt(vars[i]);
			
			return varints;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return null;
	}

	private void createCnfFile(ArrayList<String> cnf, int nrvars)
	{
		PrintWriter writer;
		try {
			writer = new PrintWriter("cnf.cnf", "UTF-8");
			writer.println("c comment");
			//nr vars and nr clausels
			writer.println("p cnf " + (nrvars) + " " + (cnf.size()));

			for(String s : cnf)
				writer.println(s);

			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
