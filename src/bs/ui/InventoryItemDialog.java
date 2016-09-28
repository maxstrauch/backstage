package bs.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;

import net.miginfocom.swing.MigLayout;
import bs.Bs;
import bs.Config;
import bs.Util;
import bs.obj.DBOEntry;
import bs.obj.Inventory;

public class InventoryItemDialog implements KeyListener {
	
	private JDialog d;
	private Bs b;
	private Inventory i;
	
	private JTextField name;
	private JTextField inventoryType;
	private JTextField serialNumber;
	private JTextField location;
	private JFormattedTextField purchasedOn;
	private JRadioButton statusOk;
	private JRadioButton statusDamaged;
	private JRadioButton statusDestroyed;
	private JRadioButton statusLost;
	private JTextArea desc;
	private JButton okayAction;
	private JButton cancelAction;
	
	public InventoryItemDialog(Bs b, JFrame p, Inventory i) {
		this.b = b;
		this.i = i;
		
		this.d = new JDialog(p);
		this.initWindow();
	}
	
	public void init() {
		d.pack();
		d.setSize(new Dimension(
				d.getInsets().left+d.getInsets().right + Config.editDialogSize.width,
				d.getInsets().bottom+d.getInsets().top + Config.editDialogSize.height
		));
		d.setResizable(false);
		d.setModal(true);
		d.setLocationRelativeTo(d.getParent());
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				d.setVisible(true);
			}
		});
	}
	
	public Inventory getResult() {
		Inventory i = new Inventory();
		if (name != null)
			i.setName(name.getText());
		if (inventoryType != null) 
			i.setType(inventoryType.getText());
		if (serialNumber != null)
			i.setSerialNumber(serialNumber.getText());
		if (location != null)
			i.setLocation(location.getText());
		if (purchasedOn != null) {
			try {
				Date d = (Date) new SimpleDateFormat("dd.MM.yyyy")
						.parse(purchasedOn.getText());
				i.setPurchasedOn(d.getTime());
			} catch (Exception e) {
				/* ex */
			}
		}
			
		if (desc != null)
			i.setDescription(desc.getText());
		if (statusOk != null && statusOk.isSelected())
			i.setState(DBOEntry.STATE_OKAY);
		if (statusDamaged != null && statusDamaged.isSelected())
			i.setState(DBOEntry.STATE_DAMAGED);
		if (statusDestroyed != null && statusDestroyed.isSelected())
			i.setState(DBOEntry.STATE_DESTROYED);
		if (statusLost != null && statusLost.isSelected())
			i.setState(DBOEntry.STATE_LOST);
		return i;
	}
	
	public boolean isEditingMode() {
		return !(this.i == null);
	}
	
	public JDialog getC() {
		return this.d;
	}
	
	private void initWindow() {
		d.setTitle(b.l("iid.title." + (isEditingMode() ? "edit" : "new")));
		d.setLayout(new BorderLayout());
		d.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		d.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				notifyClose();
			}
		});

		// ---------------------------------------
		// init components
		name = new JTextField();
		name.addKeyListener(this);
		inventoryType = new JTextField();
		inventoryType.addKeyListener(this);
		serialNumber = new JTextField();
		serialNumber.addKeyListener(this);
		location = new JTextField();
		purchasedOn = new JFormattedTextField();
		purchasedOn.setColumns(8);
		try {
			purchasedOn.setFormatterFactory(new DefaultFormatterFactory(
					new MaskFormatter("##.##.####")));
		} catch (Exception e) {
			purchasedOn.setEnabled(false);
		}
		purchasedOn.setText(new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
		statusOk = new JRadioButton(b.l("iid.lbl.gen.status.ok"));
		statusOk.setSelected(true);
		statusDamaged = new JRadioButton(b.l("iid.lbl.gen.status.dam"));
		statusDestroyed = new JRadioButton(b.l("iid.lbl.gen.status.des"));
		statusLost = new JRadioButton(b.l("iid.lbl.gen.status.los"));
		desc = new JTextArea();
		okayAction = new JButton(b.l("iid.btns.ok"));
		okayAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Inventory re = getResult();
				if (isEditingMode()) {
					b.updateElement(i, re);
				} else {
					b.addElement(re);
				}
				notifyClose();
				b.getUI().fireUpdate();
			}
		});
		okayAction.setEnabled(false);
		cancelAction = new JButton(b.l("iid.btns.canc"));
		cancelAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				notifyClose();
			}
		});
		
		// ---------------------------------------
		// create layout
		// >> body
		JTabbedPane body = new JTabbedPane();
		body.setBorder((Util.isNimbusLaf() ? new EmptyBorder(10, 0, 0, 0) : new EmptyBorder(10, 10, 10, 10)));
		// main panel
		JPanel gen = new JPanel(new MigLayout((Util.isNimbusLaf() ? "insets 10 10 10 10" : "insets 10 10 10 10")));
		gen.setOpaque(false);
		gen.add(new JLabel(b.l("iid.lbl.gen.name") + ":"));
		gen.add(name, "wrap, width 100%");
		gen.add(new JLabel(b.l("iid.lbl.gen.type") + ":"));
		gen.add(inventoryType, "wrap, width 100%");
		gen.add(new JLabel(b.l("iid.lbl.gen.serial") + ":"));
		gen.add(serialNumber, "wrap, width 100%");
		gen.add(new JLabel(b.l("iid.lbl.gen.loc") + ":"));
		gen.add(location, "wrap, width 100%");
		gen.add(new JLabel(b.l("iid.lbl.gen.purch") + ":"));
		gen.add(purchasedOn, "wrap");
		gen.add(new JLabel(b.l("iid.lbl.gen.status") + ":"));
		ButtonGroup group = new ButtonGroup();
		group.add(statusOk);
		group.add(statusDamaged);
		group.add(statusDestroyed);
		group.add(statusLost);
		JPanel tempContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tempContainer.setOpaque(false);
		tempContainer.add(statusOk);
		statusOk.setOpaque(false);
		tempContainer.add(statusDamaged);
		statusDamaged.setOpaque(false);
		tempContainer.add(statusDestroyed);
		statusDestroyed.setOpaque(false);
		tempContainer.add(statusLost);
		statusLost.setOpaque(false);
		gen.add(tempContainer, "wrap");
		Bs.addSeparator(gen, b.l("iid.lbl.gen.desc"));
		gen.add(new JScrollPane(desc), "span, width 100%, height 100%");
		body.addTab(b.l("iid.cpt.tab.gen"), gen);
		d.add(body, BorderLayout.CENTER);
		
		// >> footer
		JPanel footer = new JPanel(new MigLayout("insets 0 10 10 10"));
		footer.add(okayAction, "push, align right, sg");
		footer.add(cancelAction, "align right, sg");
		d.add(footer, BorderLayout.SOUTH);
		
		// ---------------------------------------
		// set default values
		if (i != null) {
			name.setText(i.getName());
			inventoryType.setText(i.getType());
			serialNumber.setText(i.getSerialNumber());
			location.setText(i.getLocation());
			purchasedOn.setText(i.getPurchasedOn());
			desc.setText(i.getDescription());
			if (i.getState() == DBOEntry.STATE_OKAY) 
				statusOk.setSelected(true);
			if (i.getState() == DBOEntry.STATE_DAMAGED) 
				statusDamaged.setSelected(true);
			if (i.getState() == DBOEntry.STATE_DESTROYED) 
				statusDestroyed.setSelected(true);
			if (i.getState() == DBOEntry.STATE_LOST) 
				statusLost.setSelected(true);
			Inventory i = this.getResult();
			if (i != null && i.isFullFilled())
				this.okayAction.setEnabled(true);
			else this.okayAction.setEnabled(false);
		}
		return;
	}

	private void notifyClose() {
		d.dispose();
	}
	
	public void keyReleased(KeyEvent arg0) {
		Inventory i = this.getResult();
		if (i != null && i.isFullFilled())
			this.okayAction.setEnabled(true);
		else this.okayAction.setEnabled(false);
	}
	
	public void keyPressed(KeyEvent arg0) { }

	public void keyTyped(KeyEvent arg0) { }

}
