package corr;
import java.awt.BorderLayout;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class NewJApplet extends javax.swing.JFrame {
	private JTextField jTextField1;
	private JLabel jLabel2;
	public JPanel jPanel1;
	private JButton jButton1;
	private JLabel jLabel1;
	private JTextField jTextField2;
	
	GearsApplet app = null;

	/**
	* Auto-generated main method to display this 
	* JApplet inside a new JFrame.
	*/
		
	public NewJApplet() {
		super();
		initGUI();
	}
	
	private void initGUI() {
		try {
			getContentPane().setLayout(null);
			setSize(new Dimension(400, 300));
			{
				jTextField1 = new JTextField();
				getContentPane().add(jTextField1);
				jTextField1.setBounds(116, 12, 165, 22);
				jTextField1.setText("9");
			}
			{
				jTextField2 = new JTextField();
				getContentPane().add(jTextField2);
				jTextField2.setBounds(116, 46, 165, 22);
				jTextField2.setText("16,20,30");
			}
			{
				jLabel1 = new JLabel();
				getContentPane().add(jLabel1);
				jLabel1.setBounds(36, 15, 68, 15);
				jLabel1.setText("Tubes");
			}
			{
				jLabel2 = new JLabel();
				getContentPane().add(jLabel2);
				jLabel2.setBounds(36, 49, 68, 15);
				jLabel2.setText("Sheets");
			}
			{
				jPanel1 = new JPanel();
				getContentPane().add(jPanel1);
				jPanel1.setBounds(19, 74, 400, 300);
				jPanel1.setBackground(new java.awt.Color(141,139,77));
				jPanel1.setLayout(null);
			}
			{
				jButton1 = new JButton();
				getContentPane().add(jButton1);
				jButton1.setText("Search");
				jButton1.setBounds(293, 24, 88, 22);
				jButton1.addMouseListener(new MouseAdapter() {
					public void mouseReleased(MouseEvent evt) {
						jButton1MouseReleased(evt);
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void jButton1MouseReleased(MouseEvent evt) {
		//Correlations.search("", jTextField1.getText(), jTextField2.getText());
	}

}
