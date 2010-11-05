package magellan.plugin.lighthouseicons;

import java.awt.*;
import java.awt.event.*;

/**
 * Taken from website: http://www.rgagnon.com/javadetails/java-0242.html
 * Used as MsgBox
 * 
 * @author (unknown)
 *
 */


public class MsgBox extends Dialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	 public boolean id = false;
	 Button ok,can;

	 public MsgBox(Frame frame, String msg, String title,boolean okcan){
	  super(frame, title, true);
	  setLayout(new BorderLayout());
	  add("Center",new Label(msg));
	  addOKCancelPanel(okcan);
	  createFrame();
	  pack();
	  setVisible(true);
	  }

	 void addOKCancelPanel( boolean okcan ) {
	  Panel p = new Panel();
	  p.setLayout(new FlowLayout());
	  createOKButton( p );
	  if (okcan == true)
	     createCancelButton( p );
	  add("South",p);
	  }

	 void createOKButton(Panel p) {
	  p.add(ok = new Button("OK"));
	  ok.addActionListener(this); 
	  }

	 void createCancelButton(Panel p) {
	  p.add(can = new Button("Abbruch"));
	  can.addActionListener(this);
	  }

	 void createFrame() {
	  Dimension d = getToolkit().getScreenSize();
	  setLocation(d.width/3,d.height/3);
	  }

	 public void actionPerformed(ActionEvent ae){
	  if(ae.getSource() == ok) {
	    id = true;
	    setVisible(false);
	    }
	  else if(ae.getSource() == can) {
	    setVisible(false);
	    }
	  }
	}
