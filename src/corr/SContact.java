package corr;
import java.util.ArrayList;
import java.util.List;


public class SContact extends SNode {

	protected int mode = 0;
	public SNode parent = null;
	private SNode oldparent = null;

	public SContact chain = null;
	public SContact chainStart = null;
	public boolean visited = false;
	
	public SContact(Coordinate c, SNode parent)
	{
		super(c);
		this.parent = parent;
	}
	
	public boolean runChain(SContact prev, int prevmode)
	{
		if(chainStart.visited)
		{
			return false;
		}
		
		int newmode = mode;
		if(this.mode != 0)
		{
			setContactMode(mode);
			newmode = (mode + 2)%2 + 1;
		}
		
		//if(parent.)
		newmode = (prevmode + 2)%2 + 1;
		//what was previous?
		if(prevmode == 2)
		{
			
		}
		
		
		if(((SSheet)parent).enabled)
		{
			newmode = (mode + 2)%2 + 1;
		}
		else
		{
			newmode = mode; //newmode merge mai departe si atunci nu il modific
		}
		
		//go to next one in the chain
		if(chain == null)
		{
			return true;//finished runnning the chain
		}
		return chain.runChain(this, newmode);
	}
	
	protected void openAllContacts()
	{
		parent.removeNeighbor(ngh.get(LEFT));
		parent.removeNeighbor(ngh.get(RIGHT));
		ngh.get(LEFT).removeNeighbor(parent);
		ngh.get(RIGHT).removeNeighbor(parent);
		
		ngh.get(LEFT).removeNeighbor(ngh.get(RIGHT));
		ngh.get(RIGHT).removeNeighbor(ngh.get(LEFT));
	}
	
	public void newParent(SNode parent)
	{
		this.oldparent = this.parent;
		this.parent = parent;
	}
	
	public void revertParent()
	{
		this.parent = this.oldparent;
		this.oldparent = null;
	}
	
	public void setContactMode(int mode)
	{
//		System.out.println("\n---->" + toString() + " before setcontactmode with nrngh " + ngh.size() + " mode " + mode);
//		for(SNode sn : ngh)
//		{
//			System.out.println(sn + "-->" + sn.ngh);
//		}
		
		if(this.parent.getClass() == SSheet.class)
		{
			contactSheet(mode);
		}
		if(this.parent.getClass() == SInput.class)
		{
			contactInput(mode);
		}
//		
//		System.out.println("after setcontactmode");
//		for(SNode sn : ngh)
//		{
//			System.out.println(sn + "-->" + sn.ngh);
//		}
	}
	
	
	//0-ul de la contactSheet e oarecum diferit de cel de la input
	//la input inseamna ca vecinii nu sunt legati la sheet, deci ei trec del a unul la celalalt
	//acelasi lucru este oarecum valabil si la sheeturi
	//0 inseamna ca sunt uniti unul cu celalalt, vecinii
	//3 inseamna ca nu sunt nici legati, dar nici la sheet
	//apare nevoia acestui 3, deoarece 0 presupune ca un tub se creeaza si atunci doua suprafete sunt unite
	//hi totusi sa nu pun asa ceva, si sa pun helpnode-uri la sheet-uri
	protected void contactSheet(int mode)
	{
		this.mode = mode;
		
		// 0 tuburile sunt unite
		// 1 un tub este unit cu sheetul
		// 2 celalalt tub este unit
		switch(mode)
		{
		case 0:
			openAllContacts();
			ngh.get(LEFT).addNeighbor(ngh.get(RIGHT));
			ngh.get(RIGHT).addNeighbor(ngh.get(LEFT));
			break;
		case 1:
			openAllContacts();
			ngh.get(LEFT).addNeighbor(parent);
			parent.addNeighbor(ngh.get(LEFT));
			break;
		case 2:
			openAllContacts();
			ngh.get(RIGHT).addNeighbor(parent);
			parent.addNeighbor(ngh.get(RIGHT));
			break;
		case 3:
			System.out.println("not possible!");
			break;
		case -3:
			//07.01.2014 - now this is allowed in order to find the vars
			openAllContacts();
			break;
		}
	}
	
	protected void contactInput(int mode)
	{
		this.mode = mode;

		switch(this.mode)
		{
		case 0: //both ends closed - skip parent
			openAllContacts();
			ngh.get(LEFT).addNeighbor(ngh.get(RIGHT));
			ngh.get(RIGHT).addNeighbor(ngh.get(LEFT));
			break;
		case 3: //both ends connected to input
			openAllContacts();
			ngh.get(LEFT).addNeighbor(parent);
			ngh.get(RIGHT).addNeighbor(parent);
			break;
		case -3:
			openAllContacts();
			break;
		}
	}
}
