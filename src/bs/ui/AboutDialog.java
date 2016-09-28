package bs.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;
import bs.Bs;
import bs.Config;
import bs.Util;

public class AboutDialog {

	private JDialog d;
	private Bs b;
	private JScrollPane sp;
	
	public AboutDialog(final Bs bs, JFrame p) {
		d = new JDialog(p);
		b = bs;
		this.initWindow();
	}

	public void init() {
		d.pack();
		d.setSize(new Dimension(
				d.getInsets().left+d.getInsets().right + 290,
				d.getInsets().bottom+d.getInsets().top + 440
		));
		d.setResizable(false);
		d.setModal(true);
		d.setLocationRelativeTo(d.getParent());
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				sp.getViewport().setViewPosition(new Point(0, 0));
				d.setVisible(true);
			}
		});
	}
	
	
	private void initWindow() {
		d.setTitle(b.l("about.title"));
		d.setLayout(new BorderLayout());
		d.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		d.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				notifyClose();
			}
		});

		// ---------------------------------------
		// init components
		
		// >> head
		
		// >> body
		JPanel body = new JPanel((Util.isNimbusLaf() ?
				new MigLayout("insets 0 0 0 0") : new MigLayout())) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void paintComponent(Graphics g) {
				g.drawImage(UIIco.bsAboutBackground.getImage(), 0, 0, null);
			}
		};
		body.setBorder(new EmptyBorder(137, 7, 7, 7));
		
		// info area
		JEditorPane thirdPartyView = new JEditorPane(
				"text/html", "<html><head>" + Config.editorPaneStyle + "</head><body>" + b.l("about.html.about") 
				+ "</body></html>");
		thirdPartyView.setEditable(false);
		thirdPartyView.setFocusable(false);
		sp = new JScrollPane(thirdPartyView);
		body.add(sp, "height 100%, width 100%, wrap");
		
		
		// >> footer
		JButton okayAction = new JButton(b.l("general.cap.close"));
		okayAction.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				notifyClose();
			}
		});
		okayAction.setOpaque(false);	
//		JButton btn = new JButton("Clear");
//		btn.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				if (Bs.ask(b.getUI().getC(), "Wirklich sicher?", b)) {
//					b.clearHistory();
//				}
//			}
//		});
//		body.add(btn, "align right, span");
		body.add(okayAction, "align right, span");
		// ---------------------------------------
		// create layout

		// >> body
		d.add(body, BorderLayout.CENTER);
		return;
	}
	
	private void notifyClose() {
		d.dispose();
	}

}
