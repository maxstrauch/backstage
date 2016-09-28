package bs;

import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;

import org.neodatis.odb.ODB;
import org.neodatis.odb.ODBFactory;
import org.neodatis.odb.OID;
import org.neodatis.odb.Objects;
import org.neodatis.odb.core.query.IQuery;
import org.neodatis.odb.impl.core.query.criteria.CriteriaQuery;

import bs.obj.AIManager;
import bs.obj.CD;
import bs.obj.DBOEntry;
import bs.obj.Inventory;
import bs.obj.Label;
import bs.obj.MusicFile;
import bs.ui.GUI;

import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel;

public class Bs extends AbstractListModel {

	private static final long serialVersionUID = 1L;
	public static final int VIEW_FLAG_CD = 0x02;
	public static final int VIEW_FLAG_INVENTORY = 0x04;
	public static final int VIEW_FLAG_MUSICFILES = 0x06;
	
	private Properties l;
	private ODB odb;
	private int viewFlag = VIEW_FLAG_CD;
	private IQuery search;
	private CD[] cds;
	private Inventory[] inventories;
	private MusicFile[] musicFiles;
	private boolean updateDBOs = true;
	private String orderBy = "name";
	private boolean orderASC = true;

	private GUI g;
	
	public Bs() {
		super();
		tty().println(" *** Welcome to backstage " + Config.APP_VERSION + "! *** ");
		this.init();
	}
	
	public PrintStream tty() {
		return System.out;
	}
	
	public boolean isCollectionOpened() {
		return !(odb == null);
	}
	
	public GUI getUI() {
		return g;
	}
	
	private void init() {
		// load the language file
		try {
			l = new Properties();
			l.load(this.getClass().getResourceAsStream("de.properties"));
		} catch (Exception e) {
			e.printStackTrace(tty());
		}
		
		// ui
		try {
			UIManager.setLookAndFeel(new NimbusLookAndFeel());
		} catch (Exception e) {
			e.printStackTrace(tty());
			try {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			} catch (Exception e2) {
				e2.printStackTrace(tty());
				try {
					UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
				} catch (Exception e3) {
					e3.printStackTrace(tty());
					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					} catch (Exception e4) {
						e4.printStackTrace(tty());
					}
				}
			}
		}
		
		// ui
		g = new GUI(this);
		g.init();
	}
	
	public void exit() {
		// dispose window
		g.getC().dispose();	
		// close db
		if (odb != null) odb.close();
		File[] f = Config.getJARLocation().listFiles();
		for (File file : f)
			if (file.getAbsolutePath().endsWith(".transaction"))
				file.delete();
		System.exit(0);
	}
	
	public String l(String k) {
		String value = l.getProperty(k);
		if (value != null)
			value = value.replaceAll("%%appversion%%", Config.APP_VERSION);
		return value;
	}
	
	// data management //////////////////////////////////////////////
	
//	private File current;
	
	public boolean open(File f) {
		if (odb != null) close();
		
		System.out.println("!! !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! !!");
		System.out.println("!! Going to open a new collection ...  !!");
		System.out.println("!! Please stand by ...                 !!");
		
		try {
			odb = ODBFactory.open(f.getAbsolutePath());
//			current = f;
		} catch (Exception e) {
			e.printStackTrace(tty());
//			current = null;
			return false;
		}
		
		// load the ai manager
		if (odb.count(new CriteriaQuery(AIManager.class)).intValue() < 1) {
			odb.store(new AIManager());
		}
		System.out.println("!! !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! !!");
		return true;
	}
	
	public void close() {
		if (odb == null) return;
		System.out.println("<< Closing current collectin!");
		odb.close();
		odb = null;
		System.gc();
//		current = null;
	}

	public boolean isFilterQueryActive() {
		return !(this.search == null);
	}

	public void rerender() {
		fireContentsChanged(this, 0, getSize());
	}
	
	public void setOrderByField(String oby) {
		if (oby != null && oby.length() > 1) {
			orderBy = oby;
			updateDBOs = true;
		}
	}
	
	public void setOrderByMethod(boolean byAsc) {
		this.orderASC = byAsc;
		updateDBOs = true;
	}
	
	public void flushDatabase() {
		odb.commit();
	}
	
	public Label[] getLabelFormats() {
		Objects<Inventory> lbs = odb.getObjects(Label.class);
		return lbs.toArray(new Label[lbs.size()]);
	}
	
	public void storeLabelFormat(Label l) {
		if (l == null) return;
		
		Objects<Inventory> lbs = odb.getObjects(Label.class);
		Label[] ls = lbs.toArray(new Label[lbs.size()]);
		boolean eq = false;
		for (Label label : ls) if (label.equals(l)) eq = true;
		if (!eq) odb.store(l);
		return;
	}
	
	public boolean setSearch(IQuery s) {
		this.search = s;
		updateDBOs = true;
		return !(s == null);
	}
	
	public void clearSearch() {
		this.search = null;
		updateDBOs = true;
	}
	
	private void refilter() {
		if (this.search != null) {
			if (this.getViewFlag() == VIEW_FLAG_CD) {
				// do a filer
				Objects<CD> dbo = odb.getObjects(this.search);
				cds = dbo.toArray(new CD[dbo.size()]);
			} else if (this.getViewFlag() == VIEW_FLAG_INVENTORY) {
				// do a filer
				Objects<Inventory> dbo = odb.getObjects(this.search);
				inventories = dbo.toArray(new Inventory[dbo.size()]);
			} else {
				// do a filer
				Objects<MusicFile> dbo = odb.getObjects(this.search);
				musicFiles = dbo.toArray(new MusicFile[dbo.size()]);
			}
		} else {
			// normal display 
			if (this.getViewFlag() == VIEW_FLAG_CD) {
				CriteriaQuery cq = new CriteriaQuery(CD.class);
				if (orderASC)
					cq.orderByAsc(this.orderBy);
				else cq.orderByDesc(this.orderBy);
				Objects<CD> dbo = odb.getObjects(cq);
				cds = dbo.toArray(new CD[dbo.size()]);
			} else if (this.getViewFlag() == VIEW_FLAG_INVENTORY) {
				CriteriaQuery cq = new CriteriaQuery(Inventory.class);
				if (orderASC)
					cq.orderByAsc(this.orderBy);
				else cq.orderByDesc(this.orderBy);
				Objects<Inventory> dbo = odb.getObjects(cq);
				inventories = dbo.toArray(new Inventory[dbo.size()]);
			} else {
				CriteriaQuery cq = new CriteriaQuery(MusicFile.class);
				if (orderASC)
					cq.orderByAsc(this.orderBy);
				else cq.orderByDesc(this.orderBy);
				Objects<MusicFile> dbo = odb.getObjects(cq);
				musicFiles = dbo.toArray(new MusicFile[dbo.size()]);
			}
		}
		return;
	}
	
	public void setViewFlag(int f) {
		if (f != VIEW_FLAG_CD && f != VIEW_FLAG_INVENTORY &&
				f != VIEW_FLAG_MUSICFILES && f != -1) return;
		
		viewFlag = f;
		updateDBOs = true;
		search = null;
		return;
	}
	
	public int getViewFlag() {
		return this.viewFlag;
	}
	
	public Object getElementAt(int index) {
		if (odb == null) return null;
		
		if (updateDBOs) {
			this.refilter();
			updateDBOs = false;
		}
			
		if (viewFlag == VIEW_FLAG_CD) { //: render for cds
			if (cds.length <= index) return null;
			return cds[index];
		} else if (viewFlag == VIEW_FLAG_INVENTORY) { //: render for inventory
			if (inventories.length <= index) return null;
			return inventories[index];
		} else { //: render for music files
			if (musicFiles.length <= index) return null;
			return musicFiles[index];
		}
	}
	
	public void removeElement(Object o) {
		if (odb == null) return;
		odb.delete(o);
		updateDBOs = true;
	}
	
	public void updateElement(DBOEntry old, DBOEntry changed) {
		if (odb == null) return;
		OID oid = odb.getObjectId(old);
		odb.getObjectFromId(oid);
		if (changed != null) old.update(changed);
		odb.store(old);
		g.fireUpdate();
	}
	
	public void addElement(DBOEntry o) {
		if (odb == null) return;
		OID id = odb.store(o);
		o.setId(id.getObjectId());
		
		if (odb.count(new CriteriaQuery(AIManager.class)).intValue() < 1) {
			odb.store(new AIManager());
		}
		
		Objects<AIManager> oo = odb.getObjects(AIManager.class);
		if (oo.size() > 0) {
			AIManager man = oo.getFirst();
			if (o instanceof CD) o.setOrderId(man.getNextCDId());
			else if (o instanceof Inventory)
				o.setOrderId(man.getNextInventoryId());
			else o.setOrderId(man.getNextMusicFileId());
			odb.store(man);
		}
		
		odb.store(o);
		updateDBOs = true;
	}

	public int getSize() {
		if (odb == null) return 0;
		if (viewFlag == VIEW_FLAG_CD)
			return (search != null ? (cds != null ? cds.length : 0) :
					odb.count(new CriteriaQuery(CD.class)).intValue());
		else if (viewFlag == VIEW_FLAG_INVENTORY)
			return (search != null ? (inventories != null ? inventories.length : 0) :
				odb.count(new CriteriaQuery(Inventory.class)).intValue());
		else
			return (search != null ? (musicFiles != null ? musicFiles.length : 0) :
				odb.count(new CriteriaQuery(MusicFile.class)).intValue());
	}
	
    public void copy(String src, String dest) {
        try {
            RandomAccessFile datei = new RandomAccessFile(src,"r");
            RandomAccessFile neudatei = new RandomAccessFile(dest, "rw");
            while (neudatei.length() < datei.length()) {
                neudatei.write(datei.read());
            }
            datei.close();
            neudatei.close();
        } catch (IOException e) {
            return;
        }
    }  
	
	// static methods //////////////////////////////////////////////

	public static void err(Component w, String m, Bs b) {
		JOptionPane.showMessageDialog(
				w, 
				m, 
				b.l("bs.dialog.err"),
				JOptionPane.ERROR_MESSAGE, 
				null
			);
		return;
	}
	
	public static boolean ask(Component w, String m, Bs b) {
		int result = JOptionPane.showConfirmDialog(
				w, 
				m, 
				b.l("bs.dialog.ask"), 
				JOptionPane.OK_CANCEL_OPTION, 
				JOptionPane.QUESTION_MESSAGE, 
				null
			);
		return (result == 0 ? true : false);
	}
	
	public static void nfo(Component w, String m, Bs b) {
		JOptionPane.showMessageDialog(
				w,
				m,
				b.l("bs.dialog.info"),
				JOptionPane.INFORMATION_MESSAGE,
				null
			);
		return;
	}
	
	public static String formatDate(Date d) {
		return new SimpleDateFormat("dd.MM.yyyy").format(d);
	}
	
	public static String formatAsTime(int seconds) {
		if (seconds < 1) return "0:00";
		int min = seconds/60;
		int sec = seconds%60;
		return Bs.fill(min, 2, "0") + ":" + Bs.fill(sec, 2, "0");
	}
	
	public static void addSeparator(JPanel panel, String text) {
		JLabel l = new JLabel(text);
		l.setFont(l.getFont().deriveFont(Font.BOLD));
		panel.add(l, "gapbottom 1, span, split 2, aligny center");
		panel.add(new JSeparator(), "gapleft rel, growx");
	}
	
	public static String fill(int num, int maxl, String c) {
		int cl = String.valueOf(num).length(), oc = c.length(), 
				free = Math.round(((float) maxl-cl)/(float) oc);
		if (cl >= maxl) return String.valueOf(num);
		String result = "";
		for (int i = 0; i < free; i++) result += c;
		return result + String.valueOf(num);
	}
	
	// main method //////////////////////////////////////////////	
	
	public static void main(String[] args) {
		// check if all classes are in the classpath
		try {
			Class.forName("net.miginfocom.swing.MigLayout");
			Class.forName("org.jdom.Attribute");
			Class.forName("org.neodatis.odb.Configuration");
			Class.forName("net.sourceforge.barbecue.Barcode");
			Class.forName("javax.xml.XMLConstants");
			Class.forName("org.xhtmlrenderer.Version");
			Class.forName("com.lowagie.text.Anchor");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(
					null, 
					"Es konnten nicht alle externen Bibliotheken geladen \r\n" +
					"werden. Backstage kann nicht gestartet werden!", 
					"Fataler Fehler",
					JOptionPane.ERROR_MESSAGE
				);
			System.exit(0);
		}
		
		try {
			new Bs();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
