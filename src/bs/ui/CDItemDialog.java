package bs.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;
import bs.AmazonQuery;
import bs.Bs;
import bs.Config;
import bs.Util;
import bs.obj.CD;
import bs.obj.DBOEntry;
import bs.obj.Track;

public class CDItemDialog implements KeyListener {
	
	private JDialog d;
	private Bs b;
	private CD c;
	private ArrayList<Track> tracks;
	
	private JTextField name;
	private JTextField interpret;
	private JTextArea desc;
	private JRadioButton statusOk;
	private JRadioButton statusDamaged;
	private JRadioButton statusDestroyed;
	private JRadioButton statusLost;
	private JList trks;
	private JButton trkNew;
	private JButton trkEdit;
	private JButton trkRemove;
	private JButton trkUp;
	private JButton trkDown;
	private JButton autoDetect;
	private JButton okayAction;
	private JButton cancelAction;
	
	public CDItemDialog(Bs b, JFrame p, CD c) {
		this.b = b;
		this.c = c;
		
		this.d = new JDialog(p);
		this.tracks = new ArrayList<Track>();
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
	
	public CD getResult() {
		CD c = new CD();
		if (this.c == null) c.setCreatedOn(new Date().getTime());
		
		if (name != null)
			c.setName(name.getText());
		if (interpret != null)
			c.setInterpret(interpret.getText());
		if (desc != null)
			c.setDescription(desc.getText());
		if (statusOk != null && statusOk.isSelected())
			c.setState(DBOEntry.STATE_OKAY);
		if (statusDamaged != null && statusDamaged.isSelected())
			c.setState(DBOEntry.STATE_DAMAGED);
		if (statusDestroyed != null && statusDestroyed.isSelected())
			c.setState(DBOEntry.STATE_DESTROYED);
		if (statusLost != null && statusLost.isSelected()) 
			c.setState(DBOEntry.STATE_LOST);
		
		for (int i = 0; i < tracks.size(); i++)
			c.addTrack(tracks.get(i));
		return c;
	}
	
	public boolean isEditingMode() {
		return !(this.c == null);
	}
	
	public JDialog getC() {
		return this.d;
	}

	private void initWindow() {
		d.setTitle(b.l("cdid.title." + (isEditingMode() ? "edit" : "new")));
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
		statusOk = new JRadioButton(b.l("cdid.lbl.gen.status.ok"));
		statusDamaged = new JRadioButton(b.l("cdid.lbl.gen.status.dam"));
		statusDestroyed = new JRadioButton(b.l("cdid.lbl.gen.status.des"));
		statusLost = new JRadioButton(b.l("cdid.lbl.gen.status.los"));
		statusOk.setSelected(true);
		desc = new JTextArea();
		trks = new JList(new DefaultListModel());
		
		final CDItemDialog cdidObject = this;
		trkNew = new JButton(UIIco.LIST_ADD_16);
		trkNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new TrackDialog(cdidObject, null).setVisible(true);
			}
		});
		trkRemove = new JButton(UIIco.LIST_REMOVE_16);
		trkRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int[] s = trks.getSelectedIndices();
				
				if (s != null && s.length > 0) {
					// get tracks to remove
					Track[] toRemove = new Track[s.length];
					for (int i = 0; i < toRemove.length; i++)
						toRemove[i] = tracks.get(s[i]);
					
					// remove objects
					for (int i = 0; i < toRemove.length; i++)
						tracks.remove(toRemove[i]);
					
					// re-render
					renderTrackList();
				}
			}
		});
		trkEdit = new JButton(UIIco.EDIT_16);
		trkEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int s = trks.getSelectedIndex();
				Track t = (s > -1 ? tracks.get(s) : null);
				
				// show the mask
				if (t != null) {
					new TrackDialog(cdidObject, t).setVisible(true);
				}
			}
		});
		trkUp = new JButton(UIIco.GO_UP_16);
		trkUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				swapRows(true);
			}
		});
		trkDown = new JButton(UIIco.GO_DOWN_16);
		trkDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				swapRows(false);
			}
		});
		
		// >> footer
		autoDetect = new JButton(b.l("cddlg.cap.import"));
		autoDetect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new AmazonQueryDialog(cdidObject).setVisible(true);
			}
		});
		okayAction = new JButton(b.l("cdid.btns.ok"));
		okayAction.setEnabled(false);
		okayAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				CD re = getResult();
				if (isEditingMode()) {
					b.updateElement(c, re);
				} else {
					b.addElement(re);
				}
				notifyClose();
				b.getUI().fireUpdate();
			}
		});
		cancelAction = new JButton(b.l("cdid.btns.canc"));
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
		gen.add(new JLabel(b.l("cdid.lbl.gen.name") + ":"));
		gen.add(name, "width 100%, wrap");
		gen.add(new JLabel(b.l("cdid.lbl.gen.interpret") + ":"));
		gen.add(interpret, "wrap, width 100%");
		gen.add(new JLabel(b.l("cdid.lbl.gen.status") + ":"));
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
		Bs.addSeparator(gen, b.l("cdid.lbl.gen.desc"));
		gen.add(new JScrollPane(desc), "span, width 100%, height 100%");
		body.addTab(b.l("cdid.cpt.tab.gen"), gen);
		// track panel
		JPanel trk = new JPanel(new MigLayout());
		trk.setOpaque(false);
		trkNew.setOpaque(false);
		trkRemove.setOpaque(false);
		trkEdit.setOpaque(false);
		trkUp.setOpaque(false);
		trkDown.setOpaque(false);
		trk.add(trkNew, "sg");
		trk.add(trkRemove, "sg");
		trk.add(trkEdit, "sg, gap unrel");
		trk.add(trkUp, "sg, gap unrel");
		trk.add(trkDown, "sg, wrap");
		trk.add(new JScrollPane(trks), "span, width 100%, height 100%");
		body.addTab(b.l("cdid.cpt.tab.trk"), trk);
		d.add(body, BorderLayout.CENTER);
		
		// >> footer
		JPanel footer = new JPanel(new MigLayout("insets 0 10 10 10"));
		footer.add(autoDetect, "push");
		footer.add(okayAction, "sg");
		footer.add(cancelAction, "sg");
		d.add(footer, BorderLayout.SOUTH);
		
		// ---------------------------------------
		// set default values
		if (c != null) {
			name.setText(c.getName());
			interpret.setText(c.getInterpret());
			desc.setText(c.getDescription());
			
			if (c.getState() == DBOEntry.STATE_OKAY) statusOk.setSelected(true); 
			if (c.getState() == DBOEntry.STATE_LOST) statusLost.setSelected(true); 
			if (c.getState() == DBOEntry.STATE_DESTROYED) statusDestroyed.setSelected(true); 
			if (c.getState() == DBOEntry.STATE_DAMAGED) statusDamaged.setSelected(true); 
			for (int i = 0; i < c.getTrackCount(); i++)
				handleNewTrack(c.getTrack(i));
			triggerChange();
		}
		return;
	}
	
	private void notifyClose() {
		d.dispose();
	}

	private void renderTrackList() {
		DefaultListModel dlm = (DefaultListModel) this.trks.getModel();
		if (dlm == null) return;
		dlm.removeAllElements();

		for (int i = 0; i < tracks.size(); i++)
			dlm.addElement("" + Bs.fill((i+1), 2, "0") + ". " + tracks.get(i).toListString());
		return;
	}
	
	public void handleTrackChanged(Track old, Track t) {
		int o = tracks.indexOf(old);
		if (o < 0) {
			this.handleNewTrack(t);
			return;
		}
		
		tracks.remove(old);
		t.setId(old.getId());
		tracks.add(o, t);
		renderTrackList();
		trks.setSelectedIndex(o);
	}
	
	public void handleNewTrack(Track t) {
		tracks.add(t);
		renderTrackList();
	}
	
	private void swapRows(boolean up) {
		int c = trks.getSelectedIndex();
		int s = trks.getModel().getSize();
		if (c < 0 || s < 1) return;
		
		if (up && (c-1) > -1) {
			Track from = tracks.get(c);
			Track to = tracks.get(c-1);
			
			tracks.set(c-1, from);
			tracks.set(c, to);
			
			renderTrackList();
			trks.setSelectedIndex(c-1);
		} else if (!up && (c+1) < s) {
			Track from = tracks.get(c);
			Track to = tracks.get(c+1);
			
			tracks.set(c+1, from);
			tracks.set(c, to);
			
			renderTrackList();
			trks.setSelectedIndex(c+1);
		}
		return;
	}

	private void triggerChange() {
		if (okayAction == null )
			return;
		CD r = this.getResult();
		if (r != null && r.isFullFilled())
			this.okayAction.setEnabled(true);
		else this.okayAction.setEnabled(false);
	}

	public void keyReleased(KeyEvent e) {
		triggerChange();
	}

	public void keyPressed(KeyEvent e) { }
	
	public void keyTyped(KeyEvent e) { }
	
	public class AmazonQueryDialog extends JDialog {

		private static final long serialVersionUID = 1L;
		private JTextField lookFor;
		private JProgressBar looking;
		private JList queryResults;
		private JLabel info;
		private JButton okayAction;
		private JButton searchAction;
		private JButton cancelAction;
		
		private boolean searching = false;
		
		public AmazonQueryDialog(CDItemDialog p) {
			super(p.getC());
			setResizable(false);
			setModal(true);
			this.initComponents();
			pack();
			setLocationRelativeTo(p.getC());
		}
		
		private JDialog getDialog() {
			return this;
		}
		
		private void initComponents() {
			setTitle(b.l("query.title"));
			setLayout(new BorderLayout());
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					notifyClose(true);
				}
			});
			
			lookFor = new JTextField();
			lookFor.addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent e) {
					super.keyReleased(e);
					if (lookFor.getText().length() > 2)
						searchAction.setEnabled(true);
					else 
						searchAction.setEnabled(false);
				}
			});
			looking = new JProgressBar();
			looking.setEnabled(false);
			queryResults = new JList(new DefaultListModel());
			queryResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			queryResults.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					Object sel = queryResults.getSelectedValue();
					if (sel != null && sel instanceof AmazonQuery) {
						okayAction.setEnabled(true);
					}
				}
			});
			info = new JLabel(" ");
			searchAction = new JButton(b.l("query.cap.search"));
			searchAction.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					lookForCD();
				}
			});
			searchAction.setEnabled(false);
			cancelAction = new JButton(b.l("general.cap.cancel"));
			cancelAction.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					notifyClose(false);
				}
			});
			okayAction = new JButton(b.l("query.cap.import"));
			okayAction.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					AmazonQuery aq = null;
					try {
						aq = (AmazonQuery) queryResults.getSelectedValue();
					} catch (Exception e2) {
						aq = null;
					}

					if (insertIntoFields(aq)) {
						notifyClose(true);
					} else {
						Bs.err(getDialog(), b.l("query.msg.incomplete"), b);
					}
				}
			});
			okayAction.setEnabled(false);

			JPanel body = new JPanel(new MigLayout("insets 10 10 10 10"));
			body.add(new JLabel(b.l("query.cap.query") + ":"));
			body.add(lookFor, "width 100%");
			body.add(searchAction, "wrap");
			body.add(new JScrollPane(queryResults), "span, width 450px, height 150px, wrap");
			body.add(info, "span, width 100%");
			body.add(looking, "span, width 100%");
			add(body, BorderLayout.CENTER);
			
			// >> footer
			JPanel footer = new JPanel(new MigLayout("insets 0 10 10 10"));
			footer.add(okayAction, "push, align right, sg");
			footer.add(cancelAction, "align right, sg");
			add(footer, BorderLayout.SOUTH);
			return;
		}
		
		private void finished() {
			this.lookFor.setEnabled(true);
			this.searchAction.setEnabled(true);
			this.queryResults.setEnabled(true);
			this.looking.setIndeterminate(false);
			this.looking.setEnabled(false);
			this.okayAction.setEnabled(false);
			this.cancelAction.setEnabled(true);
			this.searching = false;
		}
		
		private void lookForCD() {
			this.info.setText(b.l("query.msg.working").replaceAll("%%v", lookFor.getText()));
			this.lookFor.setEnabled(false);
			this.searchAction.setEnabled(false);
			DefaultListModel dlm = (DefaultListModel) queryResults.getModel();
			dlm.clear();
			this.queryResults.setEnabled(false);
			this.looking.setEnabled(true);
			this.looking.setIndeterminate(true);
			this.okayAction.setEnabled(false);
			this.searching = true;
			
			new Thread(new Runnable() {
				public void run() {
					try {
						Properties p = new Properties();
						try {
							p.load(new FileInputStream(Config.cfg));
						} catch (Exception e2) { /* */ }
						if (p.containsKey("proxy.server") && p.containsKey("proxy.port")) {
							AmazonQuery.proxyAddr = p.getProperty("proxy.server");
							AmazonQuery.proxyPort = Integer.parseInt(p.getProperty("proxy.port"));
							b.tty().print("Using proxy server: " + AmazonQuery.proxyAddr + "@" +
									AmazonQuery.proxyPort);
						}
						
						AmazonQuery[] aqs = AmazonQuery.retrieve(lookFor.getText());
						if (aqs == null) {
							info.setText(b.l("query.msg.noresult"));
							finished();
						} else {
							if (!searching) {
								info.setText(b.l("query.msg.canceled"));
								finished();
								return;
							}
							info.setText(b.l("query.msg.results").replaceAll("%%n", String.valueOf(aqs.length)));
							Arrays.sort(aqs, new Comparator<AmazonQuery>() {
								public int compare(AmazonQuery o1,
										AmazonQuery o2) {
									return (o1.getRating() < o2.getRating() ? 1 : -1);
								}
							});
							DefaultListModel dlm = (DefaultListModel) queryResults.getModel();
							for (int i = 0; i < aqs.length; i++)
								dlm.addElement(aqs[i]);
							finished();
						}
					} catch (Exception e) {
						info.setText(b.l("query.msg.exception"));
						finished();
					}
				}
			}).start();
			return;
		}
		
		private void notifyClose(boolean overwrite) {
			if (overwrite) {
				searching = false;
				dispose();
				return;
			}
			
			if (searching) {
				searching = false;
				info.setText(b.l("query.msg.canceling"));
				cancelAction.setEnabled(false);
			} else dispose();
			return;
		}
		
		private boolean insertIntoFields(AmazonQuery q) {
			if (q == null) return false;
			name.setText((q.title == null ? b.l("general.cap.unknown") : q.title));
			interpret.setText((q.interpret == null ? b.l("general.cap.unknown") : q.interpret));
			statusOk.setSelected(true);
			desc.setText((q.description == null ? "" : q.description));
			Track[] trks = q.tracks.toArray(new Track[q.tracks.size()]);
			for (Track t : trks) handleNewTrack(t);
			triggerChange();
			return true;
		}
		
	}
	
	private class TrackDialog extends JDialog implements KeyListener {
		
		private static final long serialVersionUID = 1L;
		private CDItemDialog cdid;
		private Track track;
		
		private TrackDialog(CDItemDialog p, Track t) {
			super(p.getC());
			this.cdid = p;
			this.track = t;
			setResizable(false);
			setModal(true);
			this.initComponents();
			
			pack();
			setSize(new Dimension(
					getInsets().left+getInsets().right + 275,
					getInsets().bottom+getInsets().top + 335
			));
			setLocationRelativeTo(p.getC());
		}
		
		private boolean isEditingMode() {
			return !(this.track == null);
		}
		
		private JTextField trkName;
		private JTextField trkInterpret;
		private JTextField trkRampTime;
		private JComboBox trkGender;
		private JSpinner trkDuration;
		private JComboBox trkEndType;
		private JTextArea trkDesc;
		
		private JButton trkOkayAction;
		private JButton trkCancelAction;
		
		private void initComponents() {
			setTitle(b.l("cdtd.title." + (this.isEditingMode() ? "edit" : "new")));
			setLayout(new BorderLayout());
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					notifyClose();
				}
			});
		
			trkName = new JTextField();
			trkName.addKeyListener(this);
			if (this.isEditingMode() && track != null) 
				trkName.setText(track.getName());
			trkInterpret = new JTextField();
			trkInterpret.addKeyListener(this);
			if (this.isEditingMode() && track != null) 
				trkInterpret.setText(track.getInterpret());
			trkRampTime = new JTextField();
			trkRampTime.addKeyListener(this);
			if (this.isEditingMode() && track != null) 
				trkRampTime.setText(track.getRampTimes());
			trkGender = new JComboBox(new String[]{
					b.l("cdtd.lbl.gender"), b.l("cdtd.lbl.gender.man"), 
					b.l("cdtd.lbl.gender.wom")
			});
			if (this.isEditingMode() && track != null) {
				if (track.getGender() == Track.GENDER_UNKNOWN) {
					trkGender.setSelectedIndex(0);
				} else if (track.getGender() == Track.GENDER_MALE) {
					trkGender.setSelectedIndex(1);
				} else {
					trkGender.setSelectedIndex(2);
				}
			}
			trkDuration = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));
			trkDuration.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					triggerChange();
				}
			});
			if (this.isEditingMode() && track != null) 
				trkDuration.setValue(track.getDuration());
			trkEndType = new JComboBox(new String[]{
					b.l("cdtd.lbl.end"), b.l("cdtd.lbl.end.c"), 
					b.l("cdtd.lbl.end.q"), b.l("cdtd.lbl.end.f")
			});
			if (this.isEditingMode() && track != null) {
				if (track.getEndType() == Track.END_UNKNOWN) {
					trkEndType.setSelectedIndex(0);
				} else if (track.getEndType() == Track.END_COLD_END) {
					trkEndType.setSelectedIndex(1);
				} else if (track.getEndType() == Track.END_QUICK_FADE) {
					trkEndType.setSelectedIndex(2);
				} else {
					trkEndType.setSelectedIndex(3);
				}
			}
			trkDesc = new JTextArea();
			if (this.isEditingMode() && track != null) 
				trkDesc.setText(track.getDescription());
			
			trkOkayAction = new JButton(b.l("cdtd.btns.ok"));
			trkOkayAction.setEnabled(false);
			trkOkayAction.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					notifyComposeTrackAndSave();
				}
			});
			trkCancelAction = new JButton(b.l("cdtd.btns.canc"));
			trkCancelAction.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					notifyClose();
				}
			});
		
			// >> body
			JPanel body = new JPanel(new MigLayout());
			Bs.addSeparator(body, b.l("cdtd.lbl.gen"));
			
			body.add(new JLabel(b.l("cdtd.lbl.gen.name") + ":"));
			body.add(trkName, "wrap, width 100%");
			body.add(new JLabel(b.l("cdtd.lbl.gen.interpret") + ":"));
			body.add(trkInterpret, "wrap, width 100%");
			body.add(new JLabel(b.l("cdtd.lbl.gen.ramp") + ":"));
			body.add(trkRampTime, "wrap, width 100%");
			body.add(new JLabel(b.l("cdtd.lbl.gen.gender") + ":"));
			body.add(trkGender, "wrap, width 100%");
			body.add(new JLabel(b.l("cdtd.lbl.gen.dur") + ":"));
			body.add(trkDuration, "wrap, width 100%");
			body.add(new JLabel(b.l("cdtd.lbl.gen.end") + ":"));
			body.add(trkEndType, "wrap, width 100%");
		
			Bs.addSeparator(body, b.l("cdtd.lbl.gen.desc"));
			body.add(new JScrollPane(trkDesc), "span, width 100%, height 100%");
			add(body, BorderLayout.CENTER);
			
			// >> footer
			JPanel footer = new JPanel(new MigLayout("", "push[][]", ""));
			footer.add(trkOkayAction, "sg");
			footer.add(trkCancelAction, "sg");
			add(footer, BorderLayout.SOUTH);
			
			if (track != null) this.triggerChange();
			return;
		}
		
		private Track composeTrack() {
			if (trkName == null || trkInterpret == null || trkRampTime == null ||
					trkGender == null || trkDuration == null || trkEndType == null ||
					trkDesc == null) {
				return null;
			}
			
			// new track
			Track t = new Track();
			t.setName(trkName.getText());
			t.setInterpret(trkInterpret.getText());
			t.setRampTimes(trkRampTime.getText());
			
			try {
				String g = (String) trkGender.getSelectedItem();
				if (g != null) {
					t.setGender((g.equals(b.l("cdtd.lbl.gender.man")) ? Track.GENDER_MALE : 
						(g.equals(b.l("cdtd.lbl.gender.wom")) ? Track.GENDER_FEMALE : Track.GENDER_UNKNOWN)
					));
				}
			} catch (Exception e) { }
			
			t.setDuration((Integer) trkDuration.getValue());
			
			try {
				String e = (String) trkEndType.getSelectedItem();
				if (e != null) {
					t.setEndType((e.equals(b.l("cdtd.lbl.end.c")) ? Track.END_COLD_END : 
						((e.equals(b.l("cdtd.lbl.end.q")) ? Track.END_QUICK_FADE : 
							(e.equals(b.l("cdtd.lbl.end.f")) ? Track.END_FADE : Track.END_UNKNOWN))
						)
					));
				}
			} catch (Exception e) { }
			
			t.setDescription(trkDesc.getText());
			return t;
		}
		
		private void notifyComposeTrackAndSave() {
			if (!this.isEditingMode()) {
				Track t = this.composeTrack();
				
				// track assembled
				this.cdid.handleNewTrack(t);
				this.notifyClose();
			} else {
				Track changedTrack = this.composeTrack();
				
				// track assembled
				this.cdid.handleTrackChanged(track, changedTrack);
				this.notifyClose();
			}
		}
		
		private void notifyClose() {
			dispose();
		}
		
		private void triggerChange() {
			if (trkOkayAction == null || trkOkayAction == null) {
				return;
			}
			
			Track t = composeTrack();
			if (t != null && t.isFullFilled())
				this.trkOkayAction.setEnabled(true);
			else this.trkOkayAction.setEnabled(false);
		}

		public void keyReleased(KeyEvent e) {
			triggerChange();
		}

		public void keyPressed(KeyEvent e) { }
		
		public void keyTyped(KeyEvent e) { }
		
	}

}
