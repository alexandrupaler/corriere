import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Robot;
import java.awt.TextField;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.xml.internal.fastinfoset.util.StringArray;

import corr.Coordinate;
import corr.Correlations;
import corr.SearchGraph;
import corr.XorCnfConstruct;

import de.jreality.geometry.FrameFieldType;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.io.JrScene;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewerUtility;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.Inspector;
import de.jreality.plugin.content.ContentLoader;
import de.jreality.plugin.content.ContentTools;
import de.jreality.plugin.content.DirectContent;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.Geometry;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.event.ToolEvent;
import de.jreality.scene.event.ToolListener;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.AxisState;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.tools.AxisTranslationTool;
import de.jreality.tools.ClickWheelCameraZoomTool;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.jreality.toolsystem.ToolUtility;
//import de.jreality.tutorial.intro.Icosahedron;
//import de.jreality.tutorial.viewer.JRViewerVR;
import de.jreality.ui.JRealitySplashScreen;
import de.jreality.ui.viewerapp.actions.file.ExportPDF;
import de.jreality.ui.viewerapp.actions.file.ExportPS;
import de.jreality.ui.viewerapp.actions.file.ExportSVG;
import de.jreality.util.CameraUtility;
import de.jreality.writer.pdf.WriterPDF;

public class Vis extends AbstractTool {

	public static List<double[]> points = new ArrayList<double[]>();
	static IndexedLineSetFactory lsf = new IndexedLineSetFactory();

	static IndexedFaceSetFactory sheets = new IndexedFaceSetFactory();
	static IndexedLineSetFactory tubes = new IndexedLineSetFactory();
	static IndexedLineSetFactory braids = new IndexedLineSetFactory();
	static IndexedLineSetFactory junctions = new IndexedLineSetFactory();

	public static HashSet<Integer> tubeSearch = new HashSet<Integer>();
	public static HashSet<Integer> sheetSearch = new HashSet<Integer>();
	public static File file;
	public static SceneGraphComponent cmp = new SceneGraphComponent();
	public static JRViewer v;
	
	public static String appTitle = "TQC";
	private static int lastPointIndex = -1;//used for creating edges

	public static DrawEdge dEdges = new DrawEdge();
	public static DrawInputs dInputs = new DrawInputs();
	
	public static VerificationReader verif;
	protected static int currentVerif = -1;
	protected static int whichVar = -1;
	
	public Vis() {
		addCurrentSlot(InputSlot.SHIFT_LEFT_BUTTON, "add a new point");
		addCurrentSlot(InputSlot.SHIFT_MIDDLE_BUTTON, "ooo");
		addCurrentSlot(InputSlot.SHIFT_RIGHT_BUTTON, "ooo");
		addCurrentSlot(InputSlot.META_LEFT_BUTTON, "oooo2");
		addCurrentSlot(InputSlot.META_MIDDLE_BUTTON, "oooo2");
		addCurrentSlot(InputSlot.META_RIGHT_BUTTON, "oooo3");
		
		//face legatura?
		dEdges.din = dInputs;
	}

	public static void pa(double[] ar, String msg) {
		System.out.print(msg + "---> ");
		for (int i = 0; i < ar.length; i++)
			System.out.print(ar[i] + " ");
		System.out.println();
	}
	
	public static int equalCoord(double[] p1, double[] p2)
	{
		Coordinate c1 = new Coordinate(p1);
		Coordinate c2 = new Coordinate(p2);
		return c1.findConstCoord1(c2)[2];
	}
	
	/**
	 * removes colinear points
	 */
	public static void cleanPointList()
	{
		//ineficient insa sigur?
		boolean again = true;
		while(again && points.size() > 2)
		{
			for(int i=0; i<points.size(); i++)
			{
				int next = (i + 1) % points.size();
				int prev = (i + points.size() - 1) % points.size();
				double[] avg = new double[3];
				for(int j=0; j<3; j++)
					avg[j] = (points.get(next)[j] + points.get(prev)[j])/2;
				if(equalCoord(avg, points.get(i)) == 2)
				{
					points.remove(i);
					again = true;
					break;
				}
			}
			again = false;
		}
		lsf.setVertexCount(points.size());
		lsf.setVertexCoordinates(points.toArray(new double[0][]));
		computeEdges();
	}
	
	public static double[] thirdPoint(Coordinate p1, Coordinate p2, int level)
	{
		double[] ret = new double[4];
		ret[3] = 1;
		boolean next = true;
		for(int i=0; i<3; i++)
		{
			if(p1.coord[i] == p2.coord[i])
			{
				ret[i] = p1.coord[i];
			}
			else
			{
				ret[i] = next ? p1.coord[i] : p2.coord[i];
				next = false;
			}
		}
		
		int cindex = -1;
		for(int i=0; i<points.size(); i++)
		{
			if(points.get(i)[0] == ret[0] && points.get(i)[1] == ret[1] && points.get(i)[2] == ret[2])
			{
				cindex = i;
				break;
			}
		}
		if(cindex != -1 && level < 2)
		{
			System.out.println("already a point at this coordinate");
			return thirdPoint(p2, p1, level + 1);
		}
		
		
		return ret;
	}
	
	public static double[][] findCorners(Coordinate p1, Coordinate p2)
	{
		double[][] ret = null;
		int[] r = p1.findConstCoord1(p2);
		if(r[2] == 0)
		{
			 ret = new double[2][4];
			 Coordinate tmp = new Coordinate(p2);
			 tmp.coord[0] = p1.coord[0];
			 for(int i=0; i<3; i++) ret[0][i] = tmp.coord[i];
			 ret[0][3] = 1;
			 ret[1] = thirdPoint(tmp, p1, 0);
		}
		else if (r[2] == 1)
		{
			ret = new double[1][4];
			ret[0] = thirdPoint(p1, p2, 0);
		}
		
		return ret;
	}
	
	public static void movePointMaybeInsert(int index, double[] position)
	{
		points.set(index, position);
			
		//ia vecinii
		
		DrawEdge ngh = dEdges.findEdges(index);
		for(int i=0; i<ngh.size(); i++)
		{
			int other = ngh.get(i)[0];
			if(other == index)
				other = ngh.get(i)[1]; 
			
			Coordinate p1 = new Coordinate(points.get(index));
			Coordinate p2 = new Coordinate(points.get(other));
			
			double[][] r = findCorners(p2, p1);
			if(r!=null)
			{
				System.out.println("add " + r.length + " nodes");
				dEdges.removeEdge(new int[]{index, other});
				for(int j=0; j<r.length; j++)
				{
					points.add(r[j]);
				}
				dEdges.add(new int[]{index, points.size() - r.length});
				if(r.length == 2)
					dEdges.add(new int[]{points.size() - 2, points.size() - 1});
				dEdges.add(new int[]{points.size() - 1, other});
			}
		}
		
//		int next = (index + 1) % cpo.length;
//		int prev = (index + cpo.length - 1) % cpo.length;
//		
//		Coordinate p1 = new Coordinate(cpo[index]);
//		Coordinate p2 = new Coordinate(cpo[next]);
//		Coordinate p3 = new Coordinate(cpo[prev]);
//		
//		double[][] r1 = points.size() > 1 ? findCorners(p1, p2) : null;
//		double[][] r2 = findCorners(p3, p1);
//		if(r1 != null)
//		{
//			for(int i=0; i<r1.length; i++)
//			{
//				points.add(next, r1[i]);
//				dEdges.addEdge(next);
//			}
//			
//		}
//		if(r2 != null)
//		{
//			for(int i=0; i<r2.length; i++)
//			{
//				int nindex = next==0 ? index+((r1==null)?0:r1.length) : index;
//				points.add(nindex, r2[i]);
//				dEdges.addEdge(nindex);
//			}
//		}
//		
		lsf.setVertexCount(points.size());
		lsf.setVertexCoordinates(points.toArray(new double[0][]));
		lsf.setEdgeCount(dEdges.size());
		lsf.setEdgeIndices(dEdges.getIntIntArray());
		
		updateColors();
	}
	
	public static void computeEdges()
	{
		int[][] edges = new int[points.size()][2];
		for(int i=0; i<points.size(); i++)
		{
			edges[i][0] = i;
			edges[i][1] = (i+1)%points.size();
		}
		lsf.setEdgeCount(points.size());
		lsf.setEdgeIndices(edges);
		
		lsf.update();
	}

	@Override
	public void perform(ToolContext tc) {
		if(tc.getAxisState(InputSlot.SHIFT_MIDDLE_BUTTON).isPressed())
		{
			//redenumeste un input
			if(tc.getCurrentPick() != null && tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_POINT)
			{
				dInputs.toggleActiveIndex(tc.getCurrentPick().getIndex());
				dInputs.updateActiveIndex(dInputs.getActiveIndex() + "_");
				updateLabels();
			}
		}
		if(tc.getAxisState(InputSlot.META_LEFT_BUTTON).isPressed())
		{
			if(tc.getCurrentPick() != null && tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_POINT)
			{
				int index = tc.getCurrentPick().getIndex();
				//delete point
				points.remove(index);
				
				DrawEdge edg = dEdges.findEdges(index);
				dEdges.removeAll(edg);
				
				ArrayList<Integer> nghn = new ArrayList<Integer>(edg.getNeighbours(index));
				for(int k=0; k<nghn.size(); k++)
					if(nghn.get(k) > index)
						nghn.set(k, nghn.get(k) - 1);
				
				dEdges.shiftDown(index);
				
				//edg.shiftDown(index);
				
				//only one neighbour looks like the initial circle
				if(nghn.size() == 1)
				{
					ArrayList<Integer> theNeighbourOftheNeighbour = new ArrayList<Integer>(dEdges.getNeighbours(nghn.get(0)));
					//has the neighbour also only one neighbour?
					if(theNeighbourOftheNeighbour.size() <= 1)
					{
						//remove the neighbour :)
						points.remove((int)nghn.get(0));
						dEdges.shiftDown(nghn.get(0));
						//the edge was already removed
					}
				}
				else
				{
					for(int i=0; i<nghn.size(); i++)
					{
						dEdges.add(new int[]{nghn.get(i), nghn.get((i+1) % nghn.size())});
					}
				}
				
				lsf.setVertexCount(points.size());
				if(points.size() > 0)
					lsf.setVertexCoordinates(points.toArray(new double[0][]));
				
				lsf.setEdgeCount(dEdges.size());
				if(dEdges.size() > 0)
					lsf.setEdgeIndices(dEdges.getIntIntArray());
				
				if(points.size() > 0)
					updateColors();
				else
					lsf.update();//just update
				
				return;
			}
			
				
			//adauga un punct fara a construi o linie
			
			double[] pos1 = getCoordinateNode(tc, true);
			double[] pos2 = getCoordinateNode(tc, true);
			//prima oara punctul este primal
			//prima oara punctul este primal
			//alte puncte cum le mut? -deja create? tot cu tasta m?
			
			//adauga doua puncte
			points.add(pos1);
			points.add(pos2);
			
			lsf.setVertexCount(points.size());
			lsf.setVertexCoordinates(points.toArray(new double[0][]));
			
			dEdges.add(new int[]{points.size() - 2, points.size() - 1});
			dEdges.add(new int[]{points.size() - 1, points.size() - 2});
			
			
			lsf.setEdgeCount(dEdges.size());
			lsf.setEdgeIndices(dEdges.getIntIntArray());
			
			updateColors();
			
			return;
		}
		if(tc.getAxisState(InputSlot.META_MIDDLE_BUTTON).isPressed())
		{
			if(tc.getCurrentPick() != null && tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_POINT)
			{
				int cindex = tc.getCurrentPick().getIndex();
				if(lastPointIndex == -1)
				{
					lastPointIndex = cindex;
				}
				else
				{
					int primal1 = Math.abs((int)points.get(cindex)[0])%2;
					int primal2 = Math.abs((int)points.get(lastPointIndex)[0])%2;
					
					if(primal1 == primal2)
					{
						dEdges.add(new int[]{lastPointIndex, cindex});
						lsf.setEdgeCount(dEdges.size());
						lsf.setEdgeIndices(dEdges.getIntIntArray());
						
						movePointMaybeInsert(lastPointIndex, points.get(lastPointIndex));
					}
					lastPointIndex = -1;
				}
			}
		}
		if(tc.getAxisState(InputSlot.META_RIGHT_BUTTON).isPressed())
		{
			//muta in primal/dual o structura geometrica
			if(tc.getCurrentPick() != null && tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_POINT)
			{
				ArrayList<Integer> comp = new ArrayList<Integer>(dEdges.getConnectedComponent(tc.getCurrentPick().getIndex()));
				for(Integer ni : comp)
				{
					System.out.println(ni);
					double[] p = points.get(ni);
					for(int i=0; i<3; i++)
						p[i] += -2*((Math.abs((int)p[i]))%2) + 1;
					points.set(ni, p);
				}
				lsf.setVertexCoordinates(points.toArray(new double[0][]));
				updateColors();
			}
		}
		if(tc.getAxisState(InputSlot.SHIFT_RIGHT_BUTTON).isPressed())
		{
			//add input node
			if(tc.getCurrentPick() != null && tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_LINE)
			{
				int lindex = tc.getCurrentPick().getIndex();
				int[] edg = dEdges.get(lindex);
				
				double[] middle = new double[4];
				middle[3] = 1;
				for(int i=0; i<3; i++)
				{
					middle[i] = (points.get(edg[0])[i] + points.get(edg[1])[i])/2;
				}
				
				points.add(middle);
				dEdges.add(new int[]{points.size() - 1, edg[0]});
				dEdges.add(new int[]{points.size() - 1, edg[1]});
				dEdges.remove(edg);
				
				//adauga la drawinputs
				dInputs.add(points.size() - 1);
				
				lsf.setVertexCount(points.size());
				lsf.setVertexCoordinates(points.toArray(new double[0][]));
				
				lsf.setEdgeCount(dEdges.size());
				lsf.setEdgeIndices(dEdges.getIntIntArray());
				
				updateColors();
			}
			if(tc.getCurrentPick() != null && tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_POINT)
			{
				//delete point
			}
		}
		
		if (!tc.getAxisState(InputSlot.SHIFT_LEFT_BUTTON).isPressed())
			return;

		int idx = -1;
		if (tc.getCurrentPick() != null
				&& tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_POINT) {
			idx = tc.getCurrentPick().getIndex();

			double[][] colors = CircuitReader.ps.getPointSet()
					.getVertexAttributes(Attribute.COLORS)
					.toDoubleArrayArray(null);
			String[] labels = CircuitReader.ps.getPointSet()
					.getVertexAttributes(Attribute.LABELS)
					.toStringArray(null);
			String label = labels[idx].split("\\(")[0].trim();
			System.out.println("injection " + label + "  " + idx);
			
			//aici nou 19.04.2014
			//inca nu e totul complet calumea legat
			//ia din dInputs id-ul pentru un anume string
			
//			int lblInt = Integer.parseInt(label); //19.04.2014
			int lblInt = dInputs.getIdForName(label);
			
			int what = 0;
			what = what | (tubeSearch.contains(lblInt) ? 1 : 0);
			what = what | (sheetSearch.contains(lblInt) ? 2 : 0);
			switch (what) {
			case 0:
				tubeSearch.add(lblInt);
				colors[idx] = VisProps.redColor;
				labels[idx] = label + "(T)";
				break;
			case 1:
				tubeSearch.remove(lblInt);
				sheetSearch.add(lblInt);
				colors[idx] = VisProps.greenColor;
				labels[idx] = label + "(S)";
				break;
			case 2:
				sheetSearch.remove(lblInt);
				colors[idx] = VisProps.grayColor;
				labels[idx] = label;
				break;
			default:
				break;
			} 
			
			CircuitReader.ps.getPointSet().setVertexAttributes(
					Attribute.COLORS,
					StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(colors));
			CircuitReader.ps.getPointSet().setVertexAttributes(
					Attribute.LABELS,
					StorageModel.STRING_ARRAY.createReadOnly(labels));
		}

	}

	private static void cleanScreen() {
		dInputs.clear();
		points.clear();
		dEdges.clear();
		lsf.setVertexCount(0);
		lsf.setEdgeCount(0);
		lsf.update();
	}

	private static void updateColors() 
	{
		ArrayList<double[]> pcolor = new ArrayList<double[]>();
		for(double[] p : points)
		{
			boolean primal = VisProps.isPrimal(p) || VisProps.isPrimalInput(p);
			pcolor.add(primal ? VisProps.primalEdgeColor : VisProps.dualEdgeColor);
		}
		lsf.setVertexColors(pcolor.toArray(new double[0][]));
				
		lsf.setEdgeColors(dEdges.getEdgeColors(points));
		
		updateLabels();
		
		lsf.update();
	}
	
	public static void updateLabels()
	{
		List<String> vlabels = new ArrayList<String>();
		for(int i=0; i<points.size(); i++)
		{
			vlabels.add(dInputs.getName(i));
		}
		if(vlabels.size() > 0)
		{
			lsf.setVertexLabels(vlabels.toArray(new String[0]));
			lsf.update();
		}
	}

	public static double[] getCoordinateNode(ToolContext tc, boolean primal) {
		return getCoordinate(tc, 2, primal);
	}
	
	public static double[] getCoordinateInput(ToolContext tc) {
		return getCoordinate(tc, 1, false);
	}
	
	public static double[] getCoordinate(ToolContext tc, int step, boolean primal) {
		Matrix m = new Matrix(tc.getTransformationMatrix(InputSlot.POINTER_TRANSFORMATION));
		
		// we compute the coordinates of the new point in world coordinates
		double[] foot = m.getColumn(3);
		double[] dir = m.getColumn(2);
		double[] offset = Rn.times(null, -5, dir);
		double[] pos = Rn.add(null, foot, offset);
		
		
		Vis.pa(pos, "pre-pos");
		
		pos = ToolUtility.worldToLocal(tc, pos);
		
		Vis.pa(pos, "post-pos1");
		
		for(int i=0; i<3; i++)
			pos[i] = ((int)Math.round(pos[i])/step)*step + (primal ? 0 : 1);
		
		Vis.pa(pos, "post-pos2 " + primal + " " + step);
		
		return pos;
	}

	public static int diffCoord(double[] p1, double[] p2) {
		int nr = 0;
		for (int i = 0; i < 3; i++)
			if (p1[i] != p2[i])
				nr++;

		return nr;
	}

	public static void main(String[] args) {

		String[] files;// = new String[]{args[0], args[1]};
		//String[] files = new String[]{"A30_2.txt", "A30_2_verif3.txt"};//ptr debug
		//String[] files = new String[]{"graph.txt", "graph_verif.txt"};//ptr debug
		
		PrintStream out = System.out;
		if(args.length != 2)
		{
			files = new String[]{"A30_2.txt", "A30_2_verif3.txt"};//ptr debug
			
			Vis vtool = new Vis();
			cmp.addTool(vtool);
			cmp.setPickable(true);
			cmp.setGeometry(lsf.getGeometry());
			
			//tubeSearch.add(9);
			//sheetSearch.add(16);sheetSearch.add(20);sheetSearch.add(30);
			//Correlations.search(file.getAbsolutePath(), new int[]{9}, new int[]{16,20,30});
			//Correlations.search(file.getAbsolutePath(), convertToInt(tubeSearch), convertToInt(sheetSearch));

			//drawCircuit(cmp, file);
			//drawSearch(cmp, file);

			startView(cmp);
			drawSearch(cmp);
			drawBraids(cmp);
			
			files = showFileOpenDialog(files);
			
			lsf.update();
		}
		else
		{
			files = new String[]{args[0], args[1]};
			//supress out
			System.setOut(new PrintStream(new OutputStream() {
			    @Override public void write(int b) throws IOException {}
			}));
		}

		loadCircuit(files[0]);
		verif = new VerificationReader(files[1], dInputs);
		
		if(args.length == 2)
		{
			boolean total = true;
			for(SearchParameters sp : verif)
			{
				boolean verif = Correlations.solveSAT(file.getAbsolutePath(), sp.xSearch, sp.zSearch, "");
				total = total && verif;
				System.err.println(verif);
				try
				{
					Thread.sleep(1000);
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			//put prev out back
			System.setOut(out);
			
			System.out.println("\n\nTOTAL " + total);
		}
	}

	private static String[] showFileOpenDialog(String[] files) {
		final JPanel panel = new JPanel(new BorderLayout(5,5));

        JPanel labels = new JPanel(new GridLayout(0,1,2,2));
        labels.add(new JLabel("Geometry", SwingConstants.RIGHT));
        labels.add(new JLabel("Specification", SwingConstants.RIGHT));
        panel.add(labels, BorderLayout.WEST);

        JPanel controls = new JPanel(new GridLayout(0,1,2,2));
        final JTextField geometrytext = new JTextField(files[0]);
        geometrytext.setColumns(30);
        controls.add(geometrytext);
        final JTextField specificationtext = new JTextField(files[1]);
        specificationtext.setColumns(30);
        controls.add(specificationtext);
        panel.add(controls, BorderLayout.CENTER);
        
        JPanel buttons = new JPanel(new GridLayout(0,1,2,2));
        JButton s1 = new JButton("...");
        buttons.add(s1);
        JButton s2 = new JButton("...");
        buttons.add(s2);
        panel.add(buttons, BorderLayout.EAST);
        
        s1.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
        	//Create a file chooser
        	  final JFileChooser fc = new JFileChooser();
        	  //In response to a button click:
        	  int returnVal = fc.showOpenDialog(panel);
        	  if (returnVal == JFileChooser.APPROVE_OPTION) {
                  geometrytext.setText(fc.getSelectedFile().getAbsolutePath());
              } else {
                  geometrytext.setText("");
              }
          }
        });
        
        s2.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
        	//Create a file chooser
        	  final JFileChooser fc = new JFileChooser();
        	  //In response to a button click:
        	  int returnVal = fc.showOpenDialog(panel);
        	  if (returnVal == JFileChooser.APPROVE_OPTION) {
                  specificationtext.setText(fc.getSelectedFile().getAbsolutePath());
              } else {
            	  specificationtext.setText("");
              }
          }
        });
        
		JOptionPane.showMessageDialog(
			    null, panel, "Select Files", JOptionPane.PLAIN_MESSAGE);
		
		return new String[]{geometrytext.getText(), specificationtext.getText()};
	}

	public static void startView(SceneGraphComponent cmp) {
		v = new JRViewer();
		JRViewer.setApplicationTitle(Vis.appTitle);

		// v.registerPlugin(new DirectContent());
		ContentTools ct = new ContentTools();
		v.registerPlugin(ct);
		
		ct.setSnapToGrid(false);
		ct.setDragEnabled(true);
		ct.setRotationEnabled(true);
		
		MySnap t = new MySnap();
		t.setMoveChildren(false);
		t.setSnapToGrid(true);
		t.setSnapToGridInterval(1);
		
		t.addPointDragListener(new PointDragListener() {
			
			@Override
			public void pointDragged(PointDragEvent e) {
			}
			
			@Override
			public void pointDragStart(PointDragEvent e) {
				System.out.println("am facut click");
			}
			
			@Override
			public void pointDragEnd(PointDragEvent e) {
				Vis.movePointMaybeInsert(e.getIndex(), e.getPosition());
			}
		});
		
		cmp.addTool(t);
		
		
		Appearance apx = new Appearance();
		setupAppearance(apx, false, true, true, Color.blue, 0.1);
		cmp.setAppearance(apx);
		
		v.setContent(cmp);
		ImageIcon imageIcon = new ImageIcon("splash.png");
		JRealitySplashScreen splash = new JRealitySplashScreen(imageIcon);
		v.setSplashScreen(splash);
		splash.setVisible(true);
		
		v.startup();
//		splash.setVisible(false);
		
		v.setShowMenuBar(false);
		v.setShowPanelSlots(false, false, false, false);
		
		ClickWheelCameraZoomTool zoom = new ClickWheelCameraZoomTool();
		v.getViewer().getSceneRoot().addTool(zoom);

		v.getViewer().getSceneRoot().getAppearance()
				.setAttribute("backgroundColor", Color.white);
		
		((Camera)v.getViewer().getCameraPath().getLastComponent().getCamera()).setPerspective(false);
		
		java.awt.Component comp = (java.awt.Component)v.getViewer().getViewingComponent();
		comp.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				
				if(dInputs.isActiveIndex())
				{
					String aiS = dInputs.getActiveIndex();
					String prefix = aiS.substring(0, aiS.length() - 1);
					
					if(e.getKeyCode() == KeyEvent.VK_ENTER)
					{
						dInputs.updateActiveIndex(prefix);
						dInputs.toggleActiveIndex(-1);
					}
					else
					{
						if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
							prefix = prefix.substring(0, prefix.length() > 0 ? prefix.length() - 1 : 0);
						else
							prefix += e.getKeyChar();
						
						dInputs.updateActiveIndex(prefix + "_");
	
						System.out.println("w: " + prefix);
					}

					Vis.updateLabels();

					return;
				}
								
				System.out.println(e.getKeyChar());

				if(e.getKeyChar() == 'd')
				{
					cleanScreen();
				}
				if(e.getKeyChar() == 'v')
				{
					cleanSearch();
					int nr = 0;
					//System.out.println("VerifSize" + verif.size());
					
					currentVerif++;
					if(currentVerif  >= verif.size())
						currentVerif = 0;
					SearchParameters param = verif.get(currentVerif);
					
					//for(SearchParameters param : verif)
					{
						//System.out.println("tubes " + param.tubeSearch);
						//System.out.println("sheets " + param.sheetSearch);
						Correlations.solveSAT(file.getAbsolutePath(), param.xSearch, param.zSearch, "");
						drawSearch(null);
					}
					
					//SearchGraph.vars.clear();
					//SearchGraph.CNF.clear();
				}
				if(e.getKeyChar() == 'n')
				{
					Correlations.nextSolution("");
					drawSearch(null);
					
					//SearchGraph.vars.clear();
					//SearchGraph.CNF.clear();
				}
				if(e.getKeyChar() == 'q')
				{
					//iterate over the variables
					Correlations.debugVariable(-1);
					cleanSearch();
					drawSearch(null);
				}
				if(e.getKeyChar() == 't')
				{
					//iterate over the variables from the solution
					whichVar = Correlations.iterateSolutionVariable(whichVar);
					cleanSearch();
					drawSearch(null);
				}
				if(e.getKeyChar() == 'a')
				{
					//for a given sheet var draw the sheet and the tubes connecting to it
					String input = JOptionPane.showInputDialog("");
					Correlations.sheetAndTubes(Integer.parseInt(input));
					cleanSearch();
					drawSearch(null);
				}
				if(e.getKeyChar() == 'w')
				{
					//input variable number and visualise it
					String input = JOptionPane.showInputDialog("");
					Correlations.debugVariable(Integer.parseInt(input));
					cleanSearch();
					drawSearch(null);
				}
				if(e.getKeyChar() == 'l')
				{
					final JList list = new JList(verif.getStringArray());
					list.addMouseListener(new MouseAdapter() {
					    public void mouseClicked(MouseEvent e) {
					    	//la double click inchide fereastra
					        if (e.getClickCount() == 2) {
					            //int index = list.locationToIndex(e.getPoint());
					            Window win = SwingUtilities.getWindowAncestor(list);
					            win.dispose();
					         }
					    }
					});
					list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					list.setSelectedIndex(Vis.currentVerif);
					
					JOptionPane.showMessageDialog(
					  null, list, "Verify", JOptionPane.PLAIN_MESSAGE);
					Vis.currentVerif = list.getSelectedIndices()[0];
					SearchParameters param = verif.get(Vis.currentVerif);
					Correlations.solveSAT(file.getAbsolutePath(), param.xSearch, param.zSearch, "");
					cleanSearch();
					drawSearch(null);
				}
				if(e.getKeyChar() == 'r')
				{
					String additional = JOptionPane.showInputDialog("");
					SearchParameters param = verif.constructSearchParam(dInputs, additional);
					Correlations.solveSAT(file.getAbsolutePath(), param.xSearch, param.zSearch, "");
					drawSearch(null);
				}
				if(e.getKeyChar() == 'b')
				{
					List<corr.Coordinate> b = Correlations.search(file.getAbsolutePath());
					double[][] bvertex = new double[b.size()][3];
					for(int i=0; i<b.size(); i++)
					{
						for(int j=0; j<3; j++)
							bvertex[i][j] = b.get(i).coord[j];
					}
					braids.setVertexCount(b.size());
					braids.setVertexCoordinates(bvertex);
					braids.update();
					
					//cand au fost calculate braidurile au iesit si junctionurile
					
					List<corr.Coordinate> jj= Correlations.getJunctionCoordinates();
					double[][] jvertex = new double[jj.size()][3];
					for(int i=0; i<jj.size(); i++)
					{
						for(int j=0; j<3; j++)
							jvertex[i][j] = jj.get(i).coord[j];
					}
					junctions.setVertexCount(jj.size());
					junctions.setVertexCoordinates(jvertex);
					junctions.update();
				}
				if(e.isControlDown() && (e.getKeyCode() == KeyEvent.VK_P)){
					ExportSVG export = new ExportSVG("file.svg", Vis.v.getViewer(), 
							(Component)Vis.v.getViewer().getViewingComponent());
					export.actionPerformed(null);
				}
				if(e.getKeyChar() == 'x' || e.getKeyChar() == 'y' || e.getKeyChar() == 'z' )
				{
					setView(e.getKeyChar());
				}
				if(e.getKeyChar() == 'c')
				{
					cleanSearch();
				}
//				if(e.getKeyChar() == 's')
//				{
//					if(file == null)
//						return;
//					//search
//					//Correlations.search(file.getAbsolutePath(), convertToInt(tubeSearch), convertToInt(sheetSearch));
//					//drawSearch(null);
//				}
				if(e.isControlDown() && (e.getKeyCode() == KeyEvent.VK_O))
				{
					//open
					JFileChooser c = new JFileChooser();
					int rVal = c.showOpenDialog(null);
					if (rVal == JFileChooser.APPROVE_OPTION) {
						String path = c.getSelectedFile().getAbsolutePath();
						
						loadCircuit(path);
					}
				}
				if(e.isControlDown() && (e.getKeyCode() == KeyEvent.VK_S))
				{
					//save
					JFileChooser c = new JFileChooser();
					int rVal = c.showSaveDialog(null);
					if (rVal == JFileChooser.APPROVE_OPTION) {
						file = new File(c.getSelectedFile().getAbsolutePath());
						try {
							saveCircuit(file);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
			@Override
			public void keyTyped(KeyEvent e) {	
			}
		});
	}
	
	private static int[] convertToInt(HashSet<Integer> set)
	{
		int[] ret = new int[set.size()];
		Integer[] s = set.toArray(new Integer[0]);
		for (int i=0; i<set.size(); i++)
			ret[i] = s[i];
		return ret;
	}

	private static void setupAppearance(Appearance ap, boolean faces,
			boolean lines, boolean points, Color linecol, double radius) {
		//for savinf the thesis
		//radius *= 10;
		
		DefaultGeometryShader dgs;
		DefaultLineShader dls;
		DefaultPointShader dpts;
		DefaultPolygonShader dps;
		dgs = ShaderUtility.createDefaultGeometryShader(ap, true);
		dgs.setShowFaces(faces);
		dgs.setShowLines(lines);
		dgs.setShowPoints(points);
		
		dls = (DefaultLineShader) dgs.createLineShader("default");
//		dls.setDiffuseColor(linecol);
		de.jreality.shader.Color arg0 = new de.jreality.shader.Color(linecol.getRed()/(float)255.0, 
				linecol.getGreen()/(float)255.0, 
				linecol.getBlue()/(float)255.0, 
				linecol.getAlpha()/(float)255.0);
		dls.setDiffuseColor(arg0);
		dls.setTubeRadius(radius * 0.6);
		
		dpts = (DefaultPointShader) dgs.createPointShader("default");
		//dpts.setDiffuseColor(linecol);
		dpts.setDiffuseColor(arg0);
		//dpts.setPointRadius(.1);
		dpts.setPointRadius(radius);
		
		
		dps = (DefaultPolygonShader)dgs.createPolygonShader("default");
		dps.setTransparency(0.5);
		//dps.setDiffuseColor(linecol);
		dps.setDiffuseColor(arg0);
		
		
		//DefaultTextShader dts = (DefaultTextShader) AttributeEntityUtility.getAttributeEntity(DefaultTextShader.class, "", ap, true);
		//dts.setShowLabels(true);
		//dts.setDiffuseColor(Color.white);
	}
	
	public static void cleanSearch()
	{
		sheets.setVertexCount(0);
		sheets.setFaceCount(0);
		tubes.setVertexCount(0);
		tubes.setEdgeCount(0);
		braids.setVertexCount(0);
		
		sheets.update();
		tubes.update();
		braids.update();
	}

	public static void drawSearch(SceneGraphComponent cmp) {
		/**
		 * draw rectangles
		 */
		if(Correlations.draw.size() > 0)
		{
			int nrfaces = Correlations.draw.size();
			sheets.setFaceCount(nrfaces);
			sheets.setVertexCount(nrfaces * 4);
			double[][] vertices = new double[nrfaces * 4][3];
			int vi = 0;
			int[][] faceIndices = new int[nrfaces][4];
			int fi = 0;
			for (corr.Rectangle r : Correlations.draw) {
				corr.Coordinate corrs[] = r.getFourPoints();
				for (corr.Coordinate c : corrs) {
					for (int i = 0; i < 3; i++)
						vertices[vi][i] = c.coord[i];
					vi++;
				}
				for (int i = 0; i < 4; i++)
					faceIndices[fi][i] = vi - 1 - i;
				fi++;
			}
			sheets.setVertexCoordinates(vertices);
			sheets.setFaceIndices(faceIndices);
			//sheets.setGenerateEdgesFromFaces(true);
			//sheets.setGenerateFaceNormals(true);
			sheets.update();
		}
	
		if(cmp != null)
		{
			SceneGraphComponent cmp3 = new SceneGraphComponent();
			cmp.addChild(cmp3);
			cmp3.setGeometry(sheets.getGeometry());
			cmp3.setPickable(false);
			Appearance ap3 = new Appearance();
			cmp3.setAppearance(ap3);
//			ap3.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
			setupAppearance(ap3, true, false, false, Color.red, .03);
		}
		
		/**
		 * draw tubes
		 */
		if(Correlations.tubes.size() > 0)
		{
			int nrvert = Correlations.tubes.size();
			tubes.setVertexCount(nrvert);
			tubes.setEdgeCount(nrvert / 2);
			double[][] tvertices = new double[nrvert][3];
			int vi = 0;
			int[][] tubeIndices = new int[nrvert / 2][2];
	
			for (corr.Coordinate c : Correlations.tubes) {
				for (int i = 0; i < 3; i++)
					tvertices[vi][i] = c.coord[i];
				vi++;
				if (vi % 2 == 1) {
					tubeIndices[vi / 2][0] = vi - 1;
					tubeIndices[vi / 2][1] = vi;
				}
			}
			tubes.setVertexCoordinates(tvertices);
			tubes.setEdgeIndices(tubeIndices);
			tubes.update();
		}
		if(cmp != null)
		{
			SceneGraphComponent cmp4 = new SceneGraphComponent();
			cmp.addChild(cmp4);
			cmp4.setGeometry(tubes.getGeometry());
			cmp4.setPickable(false);
			Appearance ap4 = new Appearance();
			cmp4.setAppearance(ap4);
			setupAppearance(ap4, false, true, true, Color.green, .14);
		}
	}

	public static void drawCircuit(SceneGraphComponent cmp, File f) {
		CircuitReader.readFile(f.getAbsolutePath());
		
		SceneGraphComponent cmp1 = new SceneGraphComponent();
		cmp.addChild(cmp1);
		cmp1.setGeometry(CircuitReader.ils.getGeometry());
		Appearance ap = new Appearance();
		cmp1.setAppearance(ap);
		cmp1.setPickable(false);
		setupAppearance(ap, false, true, false, Color.yellow, .08);

		SceneGraphComponent cmp2 = new SceneGraphComponent();
		cmp.addChild(cmp2);
		cmp2.setGeometry(CircuitReader.ps.getGeometry());
		Appearance ap2 = new Appearance();
		cmp2.setAppearance(ap2);
		cmp2.setPickable(true);
		ap2.setAttribute(CommonAttributes.TEXT_SCALE, 0.005);
		//ap2.setAttribute(CommonAttributes.TEXT_SCALE, 0.05);
		ap2.setAttribute(CommonAttributes.TEXT_OFFSET, new double[]{.05, .05, .05});
		setupAppearance(ap2, false, false, true, Color.black, .1);
	}
	
	public static void drawBraids(SceneGraphComponent cmp) {
		SceneGraphComponent cmpb = new SceneGraphComponent();
		cmp.addChild(cmpb);
		cmpb.setGeometry(Vis.braids.getGeometry());
		Appearance ap = new Appearance();
		cmpb.setAppearance(ap);
		cmpb.setPickable(false);
		setupAppearance(ap, false, false, true, Color.pink, .2);
		
		SceneGraphComponent cmpj = new SceneGraphComponent();
		cmp.addChild(cmpj);
		cmpj.setGeometry(Vis.junctions.getGeometry());
		Appearance apj = new Appearance();
		cmpj.setAppearance(apj);
		cmpj.setPickable(false);
		setupAppearance(apj, false, false, true, Color.BLACK, .1);
	}
	
	public static void setView(char ch)
	{
		Matrix aMatrix = MatrixBuilder.euclidean().rotate(ch=='z' ? 0 : Math.PI/2, 
				ch=='z' ? 1 : 0, 
				ch=='y' ? 1 : 0, 
				ch=='x' ? 1 : 0).getMatrix();
		Vis.v.getViewer().getSceneRoot().getChildComponent(1).getTransformation().setMatrix(aMatrix.getArray());
		
		CameraUtility.encompass(Vis.v.getViewer());
	}
	
	private static void saveCircuit(File file) throws IOException 
	{
		if(file.createNewFile())
		{
			file.delete();
			file.createNewFile();
		}
		
		String nl = System.getProperty("line.separator");
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        
		writer.write(dInputs.size() + nl);//injections
		writer.write(dEdges.size() + nl);
		writer.write(points.size() + nl);
		
		if(dInputs.size() == 0)
		{
			writer.write(nl);
		}
		else
		{
			writer.write((dInputs.get(0)+1)+"");
			for(int i=1; i<dInputs.size(); i++)
			{
				writer.write("," + (dInputs.get(i)+1));
			}
			writer.write(nl);
		}
		
		for(int[] e : dEdges)
		{
			writer.write((e[0]+1) + ", " + (e[1]+1) + nl);
		}
		for(int i=0; i< points.size(); i++)
		{
			writer.write( (i+1) + ", " + (long)(points.get(i)[0]+1)+ ", " + (long)(points.get(i)[1]+1)+ ", " + (long)(points.get(i)[2]+1) + nl);
		}
		
		HashMap<Integer, String> injnames = dInputs.getNames();
		for(Integer n : injnames.keySet())
		{
			String name = injnames.get(n);
			writer.write((n+1) + "," + URLEncoder.encode(name, "UTF-8") + nl);
		}
		
        writer.close();
	}

	private static void loadCircuit(String path) {
		cleanScreen();
		file = new File(path);
		drawCircuit(Vis.cmp, file);
		if(Vis.v != null)
			CameraUtility.encompass(Vis.v.getViewer());
	}
}