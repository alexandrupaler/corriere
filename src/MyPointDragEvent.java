import de.jreality.scene.PointSet;
import de.jreality.scene.tool.ToolContext;
import de.jreality.tools.PointDragEvent;


public class MyPointDragEvent extends PointDragEvent{

	protected ToolContext tc = null;
	
	public MyPointDragEvent(PointSet pointSet, int index, double[] position) {
		super(pointSet, index, position);
		// TODO Auto-generated constructor stub
	}
	
	public MyPointDragEvent(ToolContext tc)
	{
		super(new PointSet(), 0, new double[0]);
		this.tc = tc;
	}
	
}
