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
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;
import bs.Bs;
import bs.Config;
import bs.Util;
import bs.obj.DBOEntry;
import bs.obj.MusicFile;
import bs.obj.Track;

public class MusicItemDialog implements KeyListener {
	
	private JDialog d;
	private Bs b;
	private MusicFile mf;
	
	private JTextField name;
	private JTextField interpret;
	private JTextArea desc;
	private JRadioButton statusOk;
	private JRadioButton statusDamaged;
	private JTextField rampTimes;
	private JComboBox gender;
	private JSpinner duration;
	private JComboBox endTyp;
	private JButton okayAction;
	private JButton cancelAction;
	
	public MusicItemDialog(Bs b, JFrame p, MusicFile mf) {
		this.b = b;
		this.mf = mf;
		
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
	
	public MusicFile getResult() {
		MusicFile mf = new MusicFile();//(this.mf == null ? new MusicFile() : (MusicFile) this.mf.clone());
		if (this.mf == null) mf.setCreatedOn(new Date().getTime());
		
		if (name != null) 
			mf.setName(name.getText());
		if (interpret != null)
			mf.setInterpret(interpret.getText());
		if (statusDamaged != null && statusDamaged.isSelected())
			mf.setState(DBOEntry.STATE_DAMAGED);
		if (statusOk != null && statusOk.isSelected())
			mf.setState(DBOEntry.STATE_OKAY);
		if (desc != null)
			mf.setDescription(desc.getText());
		if (rampTimes != null)
			mf.setRampTimes(rampTimes.getText());
		if (gender != null) {
			String g = (String) gender.getSelectedItem();
			if (g != null) {
				mf.setGender((g.equals(b.l("cdtd.lbl.gender.man")) ? Track.GENDER_MALE : 
					(g.equals(b.l("cdtd.lbl.gender.wom")) ? Track.GENDER_FEMALE : Track.GENDER_UNKNOWN)
				));
			}
		}
		if (duration != null) 
			mf.setDuration((Integer) duration.getValue());
		if (endTyp != null) {
			String e = (String) endTyp.getSelectedItem();
			if (e != null) {
				mf.setEndType((e.equals(b.l("cdtd.lbl.end.c")) ? Track.END_COLD_END : 
					((e.equals(b.l("cdtd.lbl.end.q")) ? Track.END_QUICK_FADE : 
						(e.equals(b.l("cdtd.lbl.end.f")) ? Track.END_FADE : Track.END_UNKNOWN))
					)
				));
			}
		}
		return mf;
	}
	
	public boolean isEditingMode() {
		return !(this.mf == null);
	}
	
	public JDialog getC() {
		return this.d;
	}
	
	private void initWindow() {
		d.setTitle(b.l("mid.title." + (isEditingMode() ? "edit" : "new")));
		d.setLayout(new BorderLayout());
		d.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		d.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				notifyClose();
			}
		});
		
		// ---------------------------------------
		// init components
		// >> body
		name = new JTextField();
		name.addKeyListener(this);
		interpret = new JTextField();
		interpret.addKeyListener(this);
		desc = new JTextArea();
		statusOk = new JRadioButton(b.l("mid.lbl.gen.status.ok"));
		statusDamaged = new JRadioButton(b.l("mid.lbl.gen.status.dam"));
		statusOk.setSelected(true);
		rampTimes = new JTextField();
		gender = new JComboBox(new String[]{
				b.l("cdtd.lbl.gender"), b.l("cdtd.lbl.gender.man"), 
				b.l("cdtd.lbl.gender.wom")
		});
		duration = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));
		endTyp = new JComboBox(new String[]{
				b.l("cdtd.lbl.end"), b.l("cdtd.lbl.end.c"), 
				b.l("cdtd.lbl.end.q"), b.l("cdtd.lbl.end.f")
		});
		// >> footer
		okayAction = new JButton(b.l("mid.btns.ok"));
		okayAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MusicFile re = getResult();
				if (isEditingMode()) {
					b.updateElement(mf, re);
				} else {
					b.addElement(re);
				}
				notifyClose();
				b.getUI().fireUpdate();
			}
		});
		cancelAction = new JButton(b.l("mid.btns.canc"));
		cancelAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				notifyClose();
			}
		});
		
		// ---------------------------------------
		// create layout
		JTabbedPane body = new JTabbedPane();
		body.setBorder((Util.isNimbusLaf() ? new EmptyBorder(10, 0, 0, 0) : new EmptyBorder(10, 10, 10, 10)));
		// main panel
		JPanel gen = new JPanel(new MigLayout((Util.isNimbusLaf() ? "insets 10 10 10 10" : "insets 10 10 10 10")));
		gen.setOpaque(false);
		gen.add(new JLabel(b.l("mid.lbl.gen.name") + ":"));
		gen.add(name, "wrap, width 100%");
		gen.add(new JLabel(b.l("mid.lbl.gen.interpret") + ":"));
		gen.add(interpret, "wrap, width 100%");
		gen.add(new JLabel(b.l("mid.lbl.gen.status") + ":"));
		ButtonGroup group = new ButtonGroup();
		group.add(statusOk);
		group.add(statusDamaged);
		JPanel tempContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tempContainer.setOpaque(false);
		tempContainer.add(statusOk);
		statusOk.setOpaque(false);
		tempContainer.add(statusDamaged);
		statusDamaged.setOpaque(false);
		gen.add(tempContainer, "wrap, width 100%");
		Bs.addSeparator(gen, b.l("cdid.lbl.gen.desc"));
		gen.add(new JScrollPane(desc), "span, width 100%, height 100%");
		body.addTab(b.l("mid.cpt.tab.gen"), gen);
		JPanel opt = new JPanel(new MigLayout());
		opt.setOpaque(false);
		opt.add(new JLabel(b.l("mid.lbl.opt.ramp") + ":"));
		opt.add(rampTimes, "wrap, width 100%");
		opt.add(new JLabel(b.l("mid.lbl.opt.gender") + ":"));
		opt.add(gender, "wrap, width 100%");
		opt.add(new JLabel(b.l("mid.lbl.opt.dur") + ":"));
		opt.add(duration, "wrap, width 100%");
		opt.add(new JLabel(b.l("mid.lbl.opt.end") + ":"));
		opt.add(endTyp, "wrap, width 100%");
		body.addTab(b.l("mid.cpt.tab.opt"), opt);
		d.add(body, BorderLayout.CENTER);
		
		// >> footer
		JPanel footer = new JPanel(new MigLayout("insets 0 10 10 10"));
		footer.add(okayAction, "push, align right, sg");
		okayAction.setEnabled(false);
		footer.add(cancelAction, "align right, sg");
		d.add(footer, BorderLayout.SOUTH);
		
		// ---------------------------------------
		// set default values
		if (mf != null) {
			name.setText(mf.getName());
			interpret.setText(mf.getInterpret());
			desc.setText(mf.getDescription());
			if (mf.getState() == DBOEntry.STATE_OKAY)
				statusOk.setSelected(true);
			if (mf.getState() == DBOEntry.STATE_DAMAGED)
				statusDamaged.setSelected(true);
			rampTimes.setText(mf.getRampTimes());
			if (mf.getGender() == MusicFile.GENDER_UNKNOWN) {
				gender.setSelectedIndex(0);
			} else if (mf.getGender() == MusicFile.GENDER_MALE) {
				gender.setSelectedIndex(1);
			} else {
				gender.setSelectedIndex(2);
			}
			duration.setValue(mf.getDuration());
			if (mf.getEndType() == Track.END_UNKNOWN) {
				endTyp.setSelectedIndex(0);
			} else if (mf.getEndType() == Track.END_COLD_END) {
				endTyp.setSelectedIndex(1);
			} else if (mf.getEndType() == Track.END_QUICK_FADE) {
				endTyp.setSelectedIndex(2);
			} else {
				endTyp.setSelectedIndex(3);
			}
			// check ...
			MusicFile mf = this.getResult();
			if (mf != null && mf.isFullFilled()) {
				okayAction.setEnabled(true);
			} else {
				okayAction.setEnabled(false);
			}
		}
		return;
	}
	
	private void notifyClose() {
		d.dispose();
	}
	
	public void keyReleased(KeyEvent arg0) {
		MusicFile mf = this.getResult();
		if (mf != null && mf.isFullFilled()) {
			okayAction.setEnabled(true);
		} else {
			okayAction.setEnabled(false);
		}
	}
	
	public void keyPressed(KeyEvent arg0) { }

	public void keyTyped(KeyEvent arg0) { }

}
