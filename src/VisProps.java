import java.awt.Color;


public class VisProps {
	public static double[] redColor = new double[] { 1, 0, 0 };
	public static double[] greenColor = new double[] { 0, 1, 0 };
	public static double[] grayColor = new double[] { 0.5, 0.5, 0.5 };
	
	public static double[] primalEdgeColor = new double[]{1,1,0}; 
	public static double[] dualEdgeColor = new double[]{0,0,1};
	
	public static boolean isPrimal(double[] pos)
	{
		return isPrimal(pos, 0);
	}
	
	private static boolean isPrimal(double[] pos, int i)
	{
		return Math.abs((int)pos[i])%2 == 0;
	}
	
	public static boolean isPrimalInput(double[] pos)
	{
		//un input are cel mult o coordonata care nu da bine fata de primal dual
		int nrprimal = 0;
		for(int i=0; i<3; i++)
			nrprimal += isPrimal(pos, i) ? 1 : 0;
		if(nrprimal >= 2)
			return true;
		
		return false;
	}
	
	public static boolean areColinear(double[] a, double[] b, double[] c)
	{
		boolean ret = true;
		double r = (b[0] - a[0]) / (c[0] - b[0]);
		for(int i=1; i<3; i++)
		{
			double rt = (b[i] - a[i]) / (c[i] - b[i]);
			ret = ret && (rt == r);
		}
		return ret;
	}
}
