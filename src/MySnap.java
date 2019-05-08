import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragEventMulticaster;
import de.jreality.tools.PointDragListener;
import de.jreality.toolsystem.ToolUtility;

public class MySnap extends AbstractTool {

	private boolean moveChildren;
	private boolean snapToGrid = true;
	private double snapToGridInterval = 1;

	transient boolean directionDetermined;
	transient int direction;

	transient EffectiveAppearance eap;
	transient private int metric;
	transient Matrix startMatrix = new Matrix();
	transient double dx, dy, dz;
	transient Matrix local2world = new Matrix();
	transient Matrix dragFrame;
	transient Matrix pointer = new Matrix();
	transient protected SceneGraphComponent comp;

	static PointSet ps;
	static int index = -1;
	static double[] pos;
	static boolean primal = true;
	DrawEdge currentDrawEdge = null;
	
	transient private boolean dragInViewDirection;

	static InputSlot activationSlot = InputSlot.getDevice("DragActivation");
	static InputSlot alongPointerSlot = InputSlot
			.getDevice("DragAlongViewDirection");
	static InputSlot evolutionSlot = InputSlot.getDevice("PointerEvolution");
	
	protected PointDragListener pointDragListener;

	public MySnap() {
		super(activationSlot);
		addCurrentSlot(evolutionSlot);
		addCurrentSlot(alongPointerSlot);
	}
	
	public boolean isAlongDrawEdge()
	{
		return false;
	}

	public void activate(ToolContext tc) {
		if(tc.getCurrentPick().getPickType() != PickResult.PICK_TYPE_POINT)
			return;
		
		index = tc.getCurrentPick().getIndex();
		if(Vis.dInputs.contains(index))
		{
			// it is a input node
			//currentDrawEdge = Vis.dEdges.getConnectedComponentAsDrawEdge(index);
			// do not move
			index = -1;
			return;
		}
		
		ps = (PointSet) tc.getCurrentPick().getPickPath().getLastElement();
		
		double[][] points = new double[ps.getNumPoints()][];
		ps.getVertexAttributes(Attribute.COORDINATES)
				.toDoubleArrayArray(points);

		pos = points[index];
		Vis.pa(pos, "oldp");
		
		primal = VisProps.isPrimal(pos);
		
		//firePointDragStart(pos);
		
//		
//
//		comp = (moveChildren ? tc.getRootToLocal() : tc
//				.getRootToToolComponent()).getLastComponent();
//		if (comp.getTransformation() == null)
//			comp.setTransformation(new Transformation());
//		try {
//			if (tc.getAxisState(alongPointerSlot).isPressed()) {
//				dragInViewDirection = true;
//			} else {
//				dragInViewDirection = false;
//			}
//		} catch (Exception me) {
//			// no drag in zaxis
//			dragInViewDirection = false;
//		}
//		if (eap == null
//				|| !EffectiveAppearance.matches(eap, tc
//						.getRootToToolComponent())) {
//			eap = EffectiveAppearance.create(tc.getRootToToolComponent());
//		}
//		metric = eap.getAttribute("metric", Pn.EUCLIDEAN);
//		comp.getTransformation().getMatrix(startMatrix.getArray());
//		dx = 0;
//		dy = 0;
//		dz = 0;
//		directionDetermined = false;
//		dragInViewDirection = true;
	}

	public void perform(ToolContext tc) {
		if(index == -1)
			return;
		

		double pos[] = null;
		if(currentDrawEdge != null)
		{
			pos = Vis.getCoordinateInput(tc);
		}
		else
		{
			pos = Vis.getCoordinateNode(tc, primal); 
		}
		
		if(tc.getAxisState(alongPointerSlot).isPressed())
		{
			//cauta maxima schimbare
			int delta = Integer.MIN_VALUE;
			int idelta = 0;
			for(int i=0; i<3; i++)
				if((int)Math.abs(MySnap.pos[i] - pos[i]) > delta)
				{
					delta = (int)Math.abs(MySnap.pos[i] - pos[i]);
					idelta = i;
				}
			for(int i=0; i<3; i++)
				if(i != idelta)
					pos[i] = MySnap.pos[i];
		}
		
		//Vis.pa(pos, " @ " + idelta + " "+ delta);
		
//		
//		
//		if(currentDrawEdge != null)
//		{
//			//verifica de e pe contur
//			boolean onContour = currentDrawEdge.containsPoint(Vis.points, pos);
//			System.out.println(onContour);
//			if(!onContour)
//				pos = null;
//		}
//			
		//Vis.getCoordinateNode(tc, primal);
		
		if(pos != null)
		{
			double[][] points = new double[ps.getNumPoints()][];
			ps.getVertexAttributes(Attribute.COORDINATES)
					.toDoubleArrayArray(points);
			points[index] = pos;
			ps.setVertexAttributes(Attribute.COORDINATES,
					StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(points));
		}
		return;
		
//		if (tc.getSource() == alongPointerSlot) {
//			if (tc.getAxisState(alongPointerSlot).isPressed()) {
//				dragInViewDirection = true;
//			} else {
//				dragInViewDirection = false;
//			}
//			return;
//		}

//		Matrix evolution = new Matrix(tc.getTransformationMatrix(evolutionSlot));
		// need to convert from euclidean to possibly non-euclidean translation
		// if (metric != Pn.EUCLIDEAN)
		// MatrixBuilder.init(null,
		// metric).translate(evolution.getColumn(3)).assignTo(evolution);
		//
		// (moveChildren ?
		// tc.getRootToLocal():tc.getRootToToolComponent()).getMatrix(local2world.getArray());

//		double[][] points = new double[ps.getNumPoints()][];
//		ps.getVertexAttributes(Attribute.COORDINATES)
//				.toDoubleArrayArray(points);
//
//		double[] t = evolution.getColumn(3);
//		dx += t[0];
//		dy += t[1];
//		dz += t[2];
//		double tx = dx, ty = dy, tz = dz;
//		if (snapToGrid) {
//			tx = snapToGridInterval * Math.round(dx / snapToGridInterval);
//			ty = snapToGridInterval * Math.round(dy / snapToGridInterval);
//			tz = snapToGridInterval * Math.round(dz / snapToGridInterval);
//		}
//		double a[] = new double[3];
//		a[0] = Math.abs(tx);
//		a[1] = Math.abs(ty);
//		a[2] = Math.abs(tz);
//		int howmanyzero = 0;
//		int greaterzero = 0;
//		for(int i=0; i<3; i++)
//		{
//			if(a[i] == 0)
//				howmanyzero++;
//			if(a[i] > 0)
//				greaterzero++;
//		}
//		System.out.println(howmanyzero + "  " + greaterzero);
//		Vis.pa(a, "deltas " + directionDetermined);
//		//if (a[0] >= 0 || a[1] >= 0 || a[2] >= 0) {
//		//if(greaterzero > 0 || howmanyzero >= 2){
//			if (!dragInViewDirection) {
//				if (!directionDetermined) {
//					direction = 0;
//					if (a[1] > a[0])
//						direction = 1;
//					if (a[2] > a[1])
//						direction = 2;
//					directionDetermined = true;
//				}
//				if (direction != 0)
//					tx = 0;
//				if (direction != 1)
//					ty = 0;
//				if (direction != 2)
//					tz = 0;
//				System.out.println("direction " + direction);
//			}
//			 Matrix result = new Matrix(startMatrix);
//			 result.multiplyOnRight(local2world.getInverse());
//			 result.multiplyOnRight(MatrixBuilder.euclidean().translate(tx, ty, tz).getMatrix());
//			 result.multiplyOnRight(local2world);
//			 //comp.getTransformation().setMatrix(result.getArray());
//			 
////			Matrix result = new Matrix(startMatrix.getInverse());
////			 result.multiplyOnRight(local2world.getInverse());
////			 result.multiplyOnRight(MatrixBuilder.euclidean().translate(tx, ty, tz).getMatrix());
////			 result.multiplyOnRight(local2world);
//			 //result.multiplyOnRight(startMatrix);
//			
//			
////			Matrix result = new Matrix();
////			result.multiplyOnLeft(MatrixBuilder.euclidean().translate(tx, ty, tz).getMatrix());
//			//result.multiplyOnLeft(startMatrix);
//			 double[] np = result.multiplyVector(pos);
////
//			points[index] = np;
//			Vis.pa(np, "np");
//			ps.setVertexAttributes(Attribute.COORDINATES,
//					StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(points));
//		//}
	}
	
	@Override
	public void deactivate(ToolContext tc)
	{
		if(index == -1)
			return;
		
		double[][] points = new double[ps.getNumPoints()][];
		ps.getVertexAttributes(Attribute.COORDINATES)
				.toDoubleArrayArray(points);
		
		boolean eq = true;
		for(int i=0; i<3; i++)
			eq = eq && (pos[i] == points[index][i]);
			
		if(!eq)
			firePointDragEnd(points[index]);
		
		index = -1;
		directionDetermined = false;
		currentDrawEdge = null;
	}

	public boolean getMoveChildren() {
		return moveChildren;
	}

	public void setMoveChildren(boolean moveChildren) {
		this.moveChildren = moveChildren;
	}

	public boolean isSnapToGrid() {
		return snapToGrid;
	}

	public void setSnapToGrid(boolean snapToGrid) {
		this.snapToGrid = snapToGrid;
	}

	public double getSnapToGridInterval() {
		return snapToGridInterval;
	}

	public void setSnapToGridInterval(double snapToGridInterval) {
		this.snapToGridInterval = snapToGridInterval;
	}
	
	public void addPointDragListener(PointDragListener listener) {
        pointDragListener = PointDragEventMulticaster.add(pointDragListener, listener);
    }
	
    public void removePointDragListener(PointDragListener listener) {
    	pointDragListener = PointDragEventMulticaster.remove(pointDragListener, listener);
    } 
    
    protected void firePointDragStart(double[] location) {
        final PointDragListener l=pointDragListener;
        if (l != null) l.pointDragStart(new PointDragEvent(ps, index, location));
    }
    protected void firePointDragged(double[] location) {
        final PointDragListener l=pointDragListener;
        if (l != null) l.pointDragged(new PointDragEvent(ps, index, location));
    }      
    protected void firePointDragEnd(double[] location) {
        final PointDragListener l=pointDragListener;
        if (l != null) l.pointDragEnd(new PointDragEvent(ps, index, location));
    }
}
