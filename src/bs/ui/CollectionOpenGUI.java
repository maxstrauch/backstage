package bs.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;
import bs.Bs;

public class CollectionOpenGUI {

	private JDialog d;
	private Bs b;
	
	public CollectionOpenGUI(Bs bs, JFrame p) {
		d = new JDialog(p);
		b = bs;
		this.initWindow();
	}

	public void init() {
		d.pack();
		d.setResizable(false);
		d.setModal(true);
		d.setLocationRelativeTo(d.getParent());
		d.setVisible(true);
	}
	
	
	private void initWindow() {
		d.setTitle(b.l("opener.title"));
		d.setLayout(new MigLayout());
		d.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		d.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				notifyClose();
			}
		});

		// ---------------------------------------
		// init components
		
		JProgressBar pg = new JProgressBar();
		pg.setIndeterminate(true);
		d.add(pg, "width 375px, wrap");
		
		// >> footer
		JButton cancelAction = new JButton(b.l("general.cap.cancel"));
		cancelAction.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//notifyClose();
			}
		});
		cancelAction.setOpaque(false);
		d.add(cancelAction, "push, align right, gaptop 4");
		return;
	}
	
	private void notifyClose() {
		d.dispose();
	}

}
