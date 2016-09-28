package bs.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.miginfocom.swing.MigLayout;
import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeFactory;

import org.neodatis.odb.core.query.criteria.And;
import org.neodatis.odb.core.query.criteria.Or;
import org.neodatis.odb.core.query.criteria.Where;
import org.neodatis.odb.impl.core.query.criteria.CriteriaQuery;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
import org.xml.sax.InputSource;

import bs.Bs;
import bs.Config;
import bs.Ean13Util;
import bs.obj.CD;
import bs.obj.DBOEntry;
import bs.obj.Inventory;
import bs.obj.Label;
import bs.obj.LoanAction;
import bs.obj.MusicFile;

import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class GUI {
	
	private final static String cardWelcome = "welcome";
	private final static String cardBackstage = "backstage";

	private Pattern DIGITS5= Pattern.compile("[0-9]{5}");
	private Pattern DIGITS13 = Pattern.compile("[0-9]{13}");
	
	private JFrame f;
	private Bs b;
	private Object displayedObject = null;
	
	private JComboBox orderByElement;
	private JToggleButton orderByASC;
	private JToggleButton orderByDESC;
	private JSplitPane mainSplitter;
	private JList itemList;
	private XHTMLPanel mainInfoArea;
	
	private JRadioButtonMenuItem viewCDRadioButton;
	private JRadioButtonMenuItem viewInventoryRadioButton;
	private JRadioButtonMenuItem viewMusicFileRadioButton;
	private JToggleButton viewCDToggleButton;
	private JToggleButton viewInventoryToggleButton;
	private JToggleButton viewMusicFileToggleButton;
	
	private ViewCDsAction viewCDsAction;
	private ViewInventoryAction viewInventoryAction;
	private ViewMusicFilesAction viewMusicFilesAction;
	
	private ClearBorrowItemAction clearBorrowItemAction;
	private NewFileAction newFileAction;
	private OpenFileAction openFileAction;
	private SaveFileAction saveFileAction;
	private NewItemAction newItemAction;
	private EditItemAction editItemAction;
	private RemoveItemAction removeItemAction;
	private SearchItemAction searchItemAction;
	private RemoveSearchAction removeSearchAction;
	private ExportAction exportAction;
	private AboutAction aboutAction;
	private ExitAction exitAction;
	private BorrowItemAction borrowItemAction;
	private ReturnItemAction returnItemAction;
	private JPanel cards;
	
	public GUI(final Bs b) {
		this.f = new JFrame(b.l("Title.001"));
		this.b = b;
	}
	
	public void init() {
		this.createComponents();
		this.composeFrame();
		this.setProperties();
		
		f.pack();
		f.setSize(
				f.getInsets().left+f.getInsets().right + 800,
				f.getInsets().bottom+f.getInsets().top + 600
		);
		f.setLocationRelativeTo(null);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				f.setVisible(true);
			}
		});
	}
	
	private void setProperties() {
		f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exitAction.actionPerformed(new ActionEvent(
						e.getSource(), e.getID(), ""));
			}
		});
		f.setIconImage(UIIco.bsFrameIcon.getImage());
		
		removeSearchAction.setEnabled(false);
		
		b.setViewFlag(-1);
		viewCDsAction.actionPerformed(new ActionEvent(viewCDsAction, 1, ""));
		saveFileAction.setEnabled(false);

		
		setNoFileOpenedView();
	}
	
	private void createComponents() {
		// create the view manager buttons
		this.viewCDsAction = new ViewCDsAction();
		this.viewInventoryAction = new ViewInventoryAction();
		this.viewMusicFilesAction = new ViewMusicFilesAction();
		this.viewCDRadioButton = new JRadioButtonMenuItem(viewCDsAction);
		this.viewInventoryRadioButton = new JRadioButtonMenuItem(viewInventoryAction);
		this.viewMusicFileRadioButton = new JRadioButtonMenuItem(viewMusicFilesAction);
		ButtonGroup bg1 = new ButtonGroup();
		bg1.add(viewCDRadioButton);
		bg1.add(viewInventoryRadioButton);
		bg1.add(viewMusicFileRadioButton);
		this.viewCDToggleButton = new JToggleButton(viewCDsAction);
		this.viewInventoryToggleButton = new JToggleButton(viewInventoryAction);
		this.viewMusicFileToggleButton = new JToggleButton(viewMusicFilesAction);
		ButtonGroup bg2 = new ButtonGroup();
		bg2.add(viewCDToggleButton);
		bg2.add(viewInventoryToggleButton);
		bg2.add(viewMusicFileToggleButton);
		
		// other actions
		this.newFileAction = new NewFileAction();
		this.openFileAction = new OpenFileAction();
		this.saveFileAction = new SaveFileAction();
		this.newItemAction = new NewItemAction();
		this.editItemAction = new EditItemAction();
		this.removeItemAction = new RemoveItemAction();
		this.searchItemAction = new SearchItemAction();
		this.removeSearchAction = new RemoveSearchAction();
		this.exportAction = new ExportAction();
		this.aboutAction = new AboutAction();
		this.exitAction = new ExitAction();
		this.borrowItemAction = new BorrowItemAction();
		this.returnItemAction = new ReturnItemAction();
		this.clearBorrowItemAction = new ClearBorrowItemAction();
		
		// create the item list
		this.itemList = new JList(b);
		itemList.setCellRenderer(new ItemListCellRender(b));
		itemList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				Object[] o = itemList.getSelectedValues();
				if (o == null) return;
				// nothing selected
				if (o.length < 1) {
					// disable info area
					displayNoViewMessage(0);
					// disable edit button
					editItemAction.setEnabled(false);
					// disable delete button
					removeItemAction.setEnabled(false);
					// disable hiring action
					borrowItemAction.setEnabled(false);
					returnItemAction.setEnabled(false);
					clearBorrowItemAction.setEnabled(false);
				}
				// one item selected:
				if (o.length == 1) {
					if (o[0] == null)
						return;
					if (o[0].equals(displayedObject))
						return;
					displayedObject = o[0];
					
					// enable info area
					displayInInfoArea(o[0]);
					// enable edit button
					editItemAction.setEnabled(true);
					// enable delete button
					removeItemAction.setEnabled(true);
					// check it
					if (o[0] instanceof CD) {
						borrowItemAction.setEnabled(!((CD) o[0]).isBorrowed());
						returnItemAction.setEnabled(((CD) o[0]).isBorrowed());
						clearBorrowItemAction.setEnabled(((CD) o[0]).isBorrowed());
					} else if (o[0] instanceof Inventory) {
						borrowItemAction.setEnabled(!((Inventory) o[0]).isBorrowed());
						returnItemAction.setEnabled(((Inventory) o[0]).isBorrowed());
						clearBorrowItemAction.setEnabled(((Inventory) o[0]).isBorrowed());
					} else {
						// disable hiring action
						borrowItemAction.setEnabled(false);
						returnItemAction.setEnabled(false);
						clearBorrowItemAction.setEnabled(false);
					}
				}
				// multiple items selected:
				if (o.length > 1) {
					// disable info area
					displayNoViewMessage(o.length);
					// disable edit button
					editItemAction.setEnabled(false);
					// enable delete button
					removeItemAction.setEnabled(true);
					// disable hiring action
					borrowItemAction.setEnabled(false);
					returnItemAction.setEnabled(false);
					clearBorrowItemAction.setEnabled(false);
				}
				return;
			}
		});
		
		// create the main info area
		this.mainInfoArea = new XHTMLPanel();
		
		// order by box + buttons
		orderByElement = new JComboBox();
		orderByElement.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (orderByElement.getSelectedItem() == null)
					return;
				OrderByObject obyo = (OrderByObject) orderByElement
						.getSelectedItem();
				b.setOrderByField(obyo.variableName);
				b.rerender();
			}
		});
		orderByASC = new JToggleButton(UIIco.SORT_ASC_16);
		orderByASC.setSelected(true);
		orderByASC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b.setOrderByMethod(orderByASC.isSelected());
				fireUpdate();
			}
		});
		orderByDESC = new JToggleButton(UIIco.SORT_DESC_16);
		orderByDESC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b.setOrderByMethod(orderByASC.isSelected());
				fireUpdate();
			}
		});
		ButtonGroup bg3 = new ButtonGroup();
		bg3.add(orderByASC);
		bg3.add(orderByDESC);
		return;
	}
	
	private void composeFrame() {
		f.setLayout(new BorderLayout());
		
		// (1) create the menu bar
		JMenuBar mb = new JMenuBar();
		JMenu fileMenu = new JMenu(b.l("Menu.001"));
		fileMenu.add(newFileAction);
		fileMenu.addSeparator();
		fileMenu.add(openFileAction);
		fileMenu.add(saveFileAction);
		fileMenu.addSeparator();
		fileMenu.add(exportAction);
		fileMenu.add(new Options());
		fileMenu.addSeparator();
		fileMenu.add(exitAction);
		mb.add(fileMenu);
		
		JMenu editMenu = new JMenu(b.l("Menu.011"));
		editMenu.add(newItemAction);
		editMenu.add(removeItemAction);
		editMenu.addSeparator();
		editMenu.add(editItemAction);
		editMenu.addSeparator();
		editMenu.add(searchItemAction);
		editMenu.add(removeSearchAction);
		editMenu.addSeparator();
		editMenu.add(borrowItemAction);
		editMenu.add(returnItemAction);
		editMenu.add(clearBorrowItemAction);
		mb.add(editMenu);
		
		JMenu viewMenu = new JMenu(b.l("Menu.005"));
		viewMenu.add(viewCDRadioButton);
		viewMenu.add(viewInventoryRadioButton);
		viewMenu.add(viewMusicFileRadioButton);
		mb.add(viewMenu);
		
		JMenu aboutMenu = new JMenu("?");
		aboutMenu.add(aboutAction);
		mb.add(aboutMenu);
		f.setJMenuBar(mb);
		
		// (2) create the tool bar
		JToolBar tb = new JToolBar(b.l("Title.002"));
		tb.setFloatable(true);
		
		tb.add(openFileAction).setFocusable(false);
		tb.add(saveFileAction).setFocusable(false);
		tb.addSeparator();
		viewCDToggleButton.setText(null);
		viewInventoryToggleButton.setText(null);
		viewMusicFileToggleButton.setText(null);
		tb.add(viewCDToggleButton).setFocusable(false);
		tb.add(viewInventoryToggleButton).setFocusable(false);
		tb.add(viewMusicFileToggleButton).setFocusable(false);
		tb.addSeparator();
		tb.add(newItemAction).setFocusable(false);
		tb.add(editItemAction).setFocusable(false);
		tb.addSeparator();
		tb.add(searchItemAction).setFocusable(false);
		tb.add(removeSearchAction).setFocusable(false);
		f.add(tb, BorderLayout.NORTH);
		
		// (3) create the content pane
		JPanel content = new JPanel(new MigLayout("insets 0 0 0 0"));
		content.add(new JLabel(b.l("Info.002")), "push, align right");
		content.add(this.orderByElement);
		content.add(this.orderByASC);
		content.add(this.orderByDESC, "wrap");
		
		// (3.1) create the main list
		JScrollPane mainList = new JScrollPane(this.itemList);
		content.add(mainList, "span, width 100%, height 100%");
		// (3.2) create the main info area
		FSScrollPane sp = new FSScrollPane(mainInfoArea);
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);		
		JPanel c02 = new JPanel(new MigLayout("insets 0 0 0 0"));
		c02.add(sp, "width 100%, height 100%");
		// create the splitter
		UIManager.put("SplitPaneDivider.border", BorderFactory.createLineBorder(
				content.getBackground(), 1));
		mainSplitter = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT,
				content,
				c02
		);
		mainSplitter.setBorder(null);
		mainSplitter.setOneTouchExpandable(false);
		content.setMinimumSize(new Dimension(0, 200));
		c02.setMinimumSize(new Dimension(0, 100));
		JPanel container01 = new JPanel(new MigLayout());
		container01.add(mainSplitter, "width 100%, height 100%");
		
		// create the card layout
		JPanel card1 = container01;
		JPanel card2 = new JPanel(new MigLayout("insets 0 0 0 0")) {
			private static final long serialVersionUID = 1L;

			protected void paintComponent(Graphics g) {
				//super.paintComponent(g);
				g.setColor(new Color(0x002640));
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		card2.add(new JLabel(UIIco.bsStartUpBackground), "pos (visual.x2-pref) (visual.y2-pref)");

		// Create the panel that contains the "cards"
		cards = new JPanel(new CardLayout());
		cards.add(card1, cardBackstage);
		cards.add(card2, cardWelcome);
		f.add(cards, BorderLayout.CENTER);
		return;
	}
	
	
	
	
	
	
	
	
	
	
	// ---------------------------------------------------------------------
	
	private void setNoFileOpenedView() {
		((CardLayout) cards.getLayout()).show(cards, cardWelcome);
		orderByElement.setEnabled(false);
		orderByASC.setEnabled(false);
		orderByDESC.setEnabled(false);
		itemList.setEnabled(false);
		mainInfoArea.setEnabled(false);
		viewCDRadioButton.setEnabled(false);
		viewInventoryRadioButton.setEnabled(false);
		viewMusicFileRadioButton.setEnabled(false);
		viewCDToggleButton.setEnabled(false);
		viewInventoryToggleButton.setEnabled(false);
		viewMusicFileToggleButton.setEnabled(false);
		viewCDsAction.setEnabled(false);
		viewInventoryAction.setEnabled(false);
		viewMusicFilesAction.setEnabled(false);
		clearBorrowItemAction.setEnabled(false);
		saveFileAction.setEnabled(false);
		newItemAction.setEnabled(false);
		editItemAction.setEnabled(false);
		removeItemAction.setEnabled(false);
		searchItemAction.setEnabled(false);
		removeSearchAction.setEnabled(false);
		exportAction.setEnabled(false);
		aboutAction.setEnabled(true);
		exitAction.setEnabled(true);
		borrowItemAction.setEnabled(false);
		returnItemAction.setEnabled(false);
		return;
	}
	
	private void setFileOpenedView() {
		((CardLayout) cards.getLayout()).show(cards, cardBackstage);
		orderByElement.setEnabled(true);
		orderByASC.setEnabled(true);
		orderByDESC.setEnabled(true);
		itemList.setEnabled(true);
		mainInfoArea.setEnabled(true);
		viewCDRadioButton.setEnabled(true);
		viewInventoryRadioButton.setEnabled(true);
		viewMusicFileRadioButton.setEnabled(true);
		viewCDToggleButton.setEnabled(true);
		viewInventoryToggleButton.setEnabled(true);
		viewMusicFileToggleButton.setEnabled(true);
		viewCDsAction.setEnabled(true);
		viewInventoryAction.setEnabled(true);
		viewMusicFilesAction.setEnabled(true);
		clearBorrowItemAction.setEnabled(true);
		saveFileAction.setEnabled(true);
		newItemAction.setEnabled(true);
		editItemAction.setEnabled(true);
		removeItemAction.setEnabled(true);
		searchItemAction.setEnabled(true);
		removeSearchAction.setEnabled(false);
		exportAction.setEnabled(true);
		aboutAction.setEnabled(true);
		exitAction.setEnabled(true);
		borrowItemAction.setEnabled(true);
		returnItemAction.setEnabled(true);
		
		b.setViewFlag(-1);
		viewCDsAction.actionPerformed(new ActionEvent(viewCDsAction, 1, ""));
		saveFileAction.setEnabled(false);
		return;
	}
	
	public JFrame getC() {
		return f;
	}

	private void updateOrderByBox(int view) {
		OrderByObject[] orderBy = null;
		if (view == Bs.VIEW_FLAG_CD || view == Bs.VIEW_FLAG_MUSICFILES) 
			orderBy = new OrderByObject[]{
				new OrderByObject(b.l("OrderBy.name"), "name"),
				new OrderByObject(b.l("OrderBy.interpret"), "interpret"),
				new OrderByObject(b.l("OrderBy.id"), "orderId"),
				new OrderByObject(b.l("OrderBy.totalDuration"), "totalDuration")
			};
		if (view == Bs.VIEW_FLAG_INVENTORY) 
			orderBy = new OrderByObject[]{
				new OrderByObject(b.l("OrderBy.name"), "name"),
				new OrderByObject(b.l("OrderBy.type"), "type"),
				new OrderByObject(b.l("OrderBy.id"), "id"),
				new OrderByObject(b.l("OrderBy.purchasedOn"), "creationDate")
			};
		if (orderBy == null) return;
		orderByElement.removeAllItems();
		for (OrderByObject orderByObject : orderBy) {
			orderByElement.addItem(orderByObject);
		}
		return;
	}

	private void displayNoViewMessage(int items) {
		StringBuffer sb = new StringBuffer();
		sb.append("<html>");
		sb.append("<head><title></title>");
		sb.append(Config.STYLESHEET);
		sb.append("</head><body><div class=\"main\">");
		sb.append("<h1 class=\"firstHeading disabled\">" + b.l("Info.003") + 
				"</h1><p class=\"disabled\">" + b.l("Info.004") + "</p>");
		sb.append("</div></body>");
		sb.append("</html>");
		this.display(sb.toString());
		return;
	}
	
	private void displayInInfoArea(Object o) {
		try {
			DBOEntry dbo = (DBOEntry) o;
			StringBuffer sb = new StringBuffer();
			sb.append("<html><head><title></title>");
			sb.append(Config.STYLESHEET);
			sb.append("</head><body><div class=\"main\">");
			StringBuffer sb1 = dbo.formatToXHTML(b);
			if (sb1 != null) sb.append(sb1);
			else {
				sb.append("<h1 class=\"firstHeading disabled\">" + b.l("Info.005") + 
						"</h1><p class=\"disabled\">" + b.l("Info.006") + "</p>");
			}
			sb.append("</div></body></html>");
			this.display(sb.toString());
		} catch (Exception e) {
			e.printStackTrace(b.tty());
		}
		return;
	}
	
	private void display(String xhtml) {
		try {
			mainInfoArea.setDocumentFromString(
				xhtml, "app://default", new XhtmlNamespaceHandler()
			);
		} catch (Exception e) {
			e.printStackTrace(b.tty());
		}
		return;
	}
	
	public static void setSelectedMultiComponent(boolean e, AbstractButton... c) {
		for (AbstractButton comp : c)
			comp.setSelected(e);
	}
	
	public void fireUpdate() {
		int sel = itemList.getSelectedIndex();
		itemList.clearSelection();
		b.rerender();
		if (sel > -1 && sel < itemList.getModel().getSize()) {
			displayedObject = null;
			itemList.setSelectedIndex(sel);
		} else {
			itemList.setSelectedIndex(0);
		}
		return;
	}
	
	// ---------------------------------------------------------------------
	
	private class ViewCDsAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		{
			putValue(Action.NAME, b.l("Menu.006"));
			putValue(Action.LARGE_ICON_KEY, UIIco.MEDIA_CD_24);
			putValue(Action.SMALL_ICON, UIIco.MEDIA_CD_16);
		}
		public void actionPerformed(ActionEvent e) {
			if (b.getViewFlag() == Bs.VIEW_FLAG_CD) return;
			// ask if a filter should be discared
			if (b.isFilterQueryActive()) {
				if (!Bs.ask(f, b.l("Ask.001"), b)) {
					if (b.getViewFlag() == Bs.VIEW_FLAG_CD) {
						viewCDRadioButton.setSelected(true);
						viewCDToggleButton.setSelected(true);
						setSelectedMultiComponent(false, viewInventoryRadioButton, 
								viewInventoryToggleButton, viewMusicFileRadioButton,
								viewMusicFileToggleButton);
					} else if (b.getViewFlag() == Bs.VIEW_FLAG_INVENTORY) {
						viewInventoryRadioButton.setSelected(true);
						viewInventoryToggleButton.setSelected(true);
						setSelectedMultiComponent(false, viewCDRadioButton, 
								viewCDToggleButton, viewMusicFileRadioButton,
								viewMusicFileToggleButton);
					} else {
						viewMusicFileRadioButton.setSelected(true);
						viewMusicFileToggleButton.setSelected(true);
						setSelectedMultiComponent(false, viewCDRadioButton, 
								viewCDToggleButton, viewInventoryRadioButton,
								viewInventoryToggleButton);
					}
					return;
				}
			}
			
			// inform bs and rerender
			b.setViewFlag(Bs.VIEW_FLAG_CD);
			// update order field
			updateOrderByBox(Bs.VIEW_FLAG_CD);
			// rerender
			b.rerender();

			// do the ui
			itemList.clearSelection();
			if (itemList.getModel().getSize() > 0)
				itemList.setSelectedIndex(0);
			viewCDRadioButton.setSelected(true);
			viewCDToggleButton.setSelected(true);
			setSelectedMultiComponent(false, viewInventoryRadioButton, 
					viewInventoryToggleButton, viewMusicFileRadioButton,
					viewMusicFileToggleButton);
			return;
		}
	}
	
	private class ViewInventoryAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		{
			putValue(Action.NAME, b.l("Menu.007"));
			putValue(Action.LARGE_ICON_KEY, UIIco.MEDIA_INV_24);
			putValue(Action.SMALL_ICON, UIIco.MEDIA_INV_16);
		}
		public void actionPerformed(ActionEvent e) {
			if (b.getViewFlag() == Bs.VIEW_FLAG_INVENTORY) return;
			// ask if a filter should be discared
			if (b.isFilterQueryActive()) {
				if (!Bs.ask(f, b.l("Ask.001"), b)) {
					if (b.getViewFlag() == Bs.VIEW_FLAG_CD) {
						viewCDRadioButton.setSelected(true);
						viewCDToggleButton.setSelected(true);
						setSelectedMultiComponent(false, viewInventoryRadioButton, 
								viewInventoryToggleButton, viewMusicFileRadioButton,
								viewMusicFileToggleButton);
					} else if (b.getViewFlag() == Bs.VIEW_FLAG_INVENTORY) {
						viewInventoryRadioButton.setSelected(true);
						viewInventoryToggleButton.setSelected(true);
						setSelectedMultiComponent(false, viewCDRadioButton, 
								viewCDToggleButton, viewMusicFileRadioButton,
								viewMusicFileToggleButton);
					} else {
						viewMusicFileRadioButton.setSelected(true);
						viewMusicFileToggleButton.setSelected(true);
						setSelectedMultiComponent(false, viewCDRadioButton, 
								viewCDToggleButton, viewInventoryRadioButton,
								viewInventoryToggleButton);
					}
					return;
				}
			}
			
			// inform bs and rerender
			b.setViewFlag(Bs.VIEW_FLAG_INVENTORY);
			// update order field
			updateOrderByBox(Bs.VIEW_FLAG_INVENTORY);
			// rerender
			b.rerender();

			// do the ui
			itemList.clearSelection();
			if (itemList.getModel().getSize() > 0)
				itemList.setSelectedIndex(0);
			viewInventoryRadioButton.setSelected(true);
			viewInventoryToggleButton.setSelected(true);
			setSelectedMultiComponent(false, viewCDRadioButton, 
					viewCDToggleButton, viewMusicFileRadioButton,
					viewMusicFileToggleButton);
			return;
		}
	}
	
	
	private class ViewMusicFilesAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		{
			putValue(Action.NAME, b.l("Menu.008"));
			putValue(Action.LARGE_ICON_KEY, UIIco.MEDIA_MUS_24);
			putValue(Action.SMALL_ICON, UIIco.MEDIA_MUS_16);
		}
		public void actionPerformed(ActionEvent e) {
			if (b.getViewFlag() == Bs.VIEW_FLAG_MUSICFILES) return;
			// ask if a filter should be discared
			if (b.isFilterQueryActive()) {
				if (!Bs.ask(f, b.l("Ask.001"), b)) {
					if (b.getViewFlag() == Bs.VIEW_FLAG_CD) {
						viewCDRadioButton.setSelected(true);
						viewCDToggleButton.setSelected(true);
						setSelectedMultiComponent(false, viewInventoryRadioButton, 
								viewInventoryToggleButton, viewMusicFileRadioButton,
								viewMusicFileToggleButton);
					} else if (b.getViewFlag() == Bs.VIEW_FLAG_INVENTORY) {
						viewInventoryRadioButton.setSelected(true);
						viewInventoryToggleButton.setSelected(true);
						setSelectedMultiComponent(false, viewCDRadioButton, 
								viewCDToggleButton, viewMusicFileRadioButton,
								viewMusicFileToggleButton);
					} else {
						viewMusicFileRadioButton.setSelected(true);
						viewMusicFileToggleButton.setSelected(true);
						setSelectedMultiComponent(false, viewCDRadioButton, 
								viewCDToggleButton, viewInventoryRadioButton,
								viewInventoryToggleButton);
					}
					return;
				}
			}
			
			// inform bs and rerender
			b.setViewFlag(Bs.VIEW_FLAG_MUSICFILES);
			// update order field
			updateOrderByBox(Bs.VIEW_FLAG_MUSICFILES);
			// rerender
			b.rerender();

			// do the ui
			itemList.clearSelection();
			if (itemList.getModel().getSize() > 0)
				itemList.setSelectedIndex(0);
			viewMusicFileRadioButton.setSelected(true);
			viewMusicFileToggleButton.setSelected(true);
			setSelectedMultiComponent(false, viewCDRadioButton, 
					viewCDToggleButton, viewInventoryRadioButton,
					viewInventoryToggleButton);
			return;
		}
	}
	
	private class BorrowItemAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		{
			putValue(Action.NAME, b.l("Menu.018"));
			putValue(Action.SMALL_ICON, UIIco.UNDO_16);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_L, InputEvent.CTRL_MASK));
		}
		public void actionPerformed(ActionEvent e) {
			showBorrowDialog(false);
		}
	}
	
	private class ReturnItemAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		{
			putValue(Action.NAME, b.l("Menu.019"));
			putValue(Action.SMALL_ICON, UIIco.REDO_16);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_R, InputEvent.CTRL_MASK));
		}
		public void actionPerformed(ActionEvent e) {
			showBorrowDialog(true);
		}
	}
	
	private class ClearBorrowItemAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		{
			putValue(Action.NAME, b.l("Menu.020"));
			putValue(Action.SMALL_ICON, UIIco.CLEAR_16);
		}
		public void actionPerformed(ActionEvent e) {
			Object o = itemList.getSelectedValue();
			if (o == null) return;
			
			// accept back
			if (o instanceof CD) {
				CD c = (CD) o;
				c.clearOpenLoanAction();
				b.updateElement(c, null);
			} else if (o instanceof Inventory) {
				Inventory i = (Inventory) o;
				i.clearOpenLoanAction();
				b.updateElement(i, null);
			}
			saveFileAction.setEnabled(true);
		}
	}
	
	private void showBorrowDialog(boolean r) {
		Object o = this.itemList.getSelectedValue();
		if (o == null) return;
		
		// create components
		JTextField borrowedBy = new JTextField();
		borrowedBy.requestFocus();
		JTextField borrowedTo = new JTextField();
		JCheckBox back = new JCheckBox(b.l("Info.009"));
		JTextArea desc = new JTextArea();
		if (o instanceof CD) {
			CD c = (CD) o;
			LoanAction la = c.getLoanAction(c.getLoanCount()-1);
			if (la != null) desc.setText(la.getDescription());
		} else if (o instanceof Inventory) {
			Inventory i = (Inventory) o;
			LoanAction la = i.getLoanAction(i.getLoanCount()-1);
			if (la != null) desc.setText(la.getDescription());
		}
		
		if (r) {
			borrowedBy.setEnabled(false);
			borrowedTo.setEnabled(false);
			back.setEnabled(true);
			desc.setEnabled(true);
		} else {
			borrowedBy.setEnabled(true);
			borrowedTo.setEnabled(true);
			back.setEnabled(false);
			desc.setEnabled(true);
		}
		
		// create the panel
		JPanel panel = new JPanel(new MigLayout("insets 0 0 0 0"));
		panel.setOpaque(false);
		panel.add(new JLabel(b.l("bsui.borrow.by") + ":"));
		panel.add(borrowedBy, "width 100%, wrap");
		panel.add(new JLabel(b.l("bsui.borrow.to") + ":"));
		panel.add(borrowedTo, "width 100%, wrap");
		panel.add(back, "span, width 100%, wrap");
		Bs.addSeparator(panel, b.l("bsui.borrow.desc"));
		panel.add(new JScrollPane(desc), "span, width 100%, height 50px");
		panel.setPreferredSize(new Dimension(400, 
				panel.getPreferredSize().height));
		// show the dialog
		int re = JOptionPane.showOptionDialog(
				f,
				panel, 
				b.l("bsui.borrow") + " - " + (
						o instanceof CD ? ((CD) o).getName() :
							(o instanceof Inventory ? ((Inventory) o).getName() 
									: "?")
				), 
				JOptionPane.OK_CANCEL_OPTION, 
				JOptionPane.QUESTION_MESSAGE, 
				null, 
				null, 
				""
			);
		if (re != 0) return;
		if (r) {
			if (!back.isSelected()) {
				Bs.err(f, b.l("Info.011"), b);
				return;
			}
		} else {
			if (borrowedBy.getText().length() < 2 ||
					borrowedTo.getText().length() < 2) {
				Bs.err(f, b.l("Info.010"), b);
				return;
			}
		}
		
		if (r) {
			// accept back
			if (o instanceof CD) {
				CD c = (CD) o;
				c.closeOpenLoanAction(new Date(), desc.getText());
				b.updateElement(c, null);
			} else if (o instanceof Inventory) {
				Inventory i = (Inventory) o;
				i.closeOpenLoanAction(new Date(), desc.getText());
				b.updateElement(i, null);
			}
		} else {
			// create a new loan action
			LoanAction la = new LoanAction(borrowedBy.getText(), 
					borrowedTo.getText(), new Date(), desc.getText());
			
			// update the element
			if (o instanceof CD) {
				CD c = (CD) o;
				c.addLoanAction(la);
				b.updateElement(c, null);
			} else if (o instanceof Inventory) {
				Inventory i = (Inventory) o;
				i.addLoanAction(la);
				b.updateElement(i, null);
			}
		}
		saveFileAction.setEnabled(true);
		return;
	}
	
	private class SearchItemAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		{
			putValue(Action.NAME, b.l("Menu.013"));
			putValue(Action.LARGE_ICON_KEY, UIIco.EDIT_FIND_24);
			putValue(Action.SMALL_ICON, UIIco.EDIT_FIND_16);
		}
		public void actionPerformed(ActionEvent e) {
			if (displaySearchDialog()) {
				removeSearchAction.setEnabled(true);
				fireUpdate();
			}
		}
	}
	
	private class RemoveSearchAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		{
			putValue(Action.NAME, b.l("Menu.014"));
			putValue(Action.LARGE_ICON_KEY, UIIco.PROCESS_STOP_24);
			putValue(Action.SMALL_ICON, UIIco.PROCESS_STOP_16);
		}
		public void actionPerformed(ActionEvent e) {
			removeSearchAction.setEnabled(false);
			b.clearSearch();
			fireUpdate();
		}
	}
	
	private boolean displaySearchDialog() {
		JPanel panel = new JPanel(new MigLayout("insets 0 0 0 0"));

		// ----------------------------
		// init components
		JTextArea search = new JTextArea();
		JCheckBox inName = new JCheckBox(b.l("bsui.search.full.nam"));
		JCheckBox inInterpret = new JCheckBox(b.l("bsui.search.full.int"));
		JCheckBox inDesc = new JCheckBox(b.l("bsui.search.full.des"));
		JCheckBox inType = new JCheckBox(b.l("bsui.search.full.typ"));
		JCheckBox inSN = new JCheckBox(b.l("bsui.search.full.snu"));
		JCheckBox inLocation = new JCheckBox(b.l("bsui.search.full.loc"));
		JRadioButton and = new JRadioButton(b.l("bsui.search.full.c.and"));
		JRadioButton or = new JRadioButton(b.l("bsui.search.full.c.or"));
		or.setSelected(true);
		ButtonGroup bgConnection = new ButtonGroup();
		bgConnection.add(and);
		bgConnection.add(or);
		JCheckBox loan = new JCheckBox(b.l("bsui.search.full.opt.loan"));
		final JRadioButton statusOk = new JRadioButton(b.l("cdid.lbl.gen.status.ok"));
		statusOk.setSelected(true);
		final JRadioButton statusDamaged = new JRadioButton(b.l("cdid.lbl.gen.status.dam"));
		final JRadioButton statusDestroyed = new JRadioButton(b.l("cdid.lbl.gen.status.des"));
		final JRadioButton statusLost = new JRadioButton(b.l("cdid.lbl.gen.status.los"));
		statusOk.setEnabled(false);
		statusDamaged.setEnabled(false);
		statusDestroyed.setEnabled(false);
		statusLost.setEnabled(false);
		ButtonGroup bgStatus = new ButtonGroup();
		bgStatus.add(statusOk);
		bgStatus.add(statusDamaged);
		bgStatus.add(statusDestroyed);
		bgStatus.add(statusLost);
		final JCheckBox state = new JCheckBox(b.l("bsui.search.full.opt.state"));
		state.setSelected(false);
		state.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (state.isSelected()) {
					statusOk.setEnabled(true);
					statusDamaged.setEnabled(true);
					statusDestroyed.setEnabled(true);
					statusLost.setEnabled(true);
				} else {
					statusOk.setEnabled(false);
					statusDamaged.setEnabled(false);
					statusDestroyed.setEnabled(false);
					statusLost.setEnabled(false);
				}
			}
		});
		final JFormattedTextField from = new JFormattedTextField();
		from.setEnabled(false);
		final JFormattedTextField to = new JFormattedTextField();
		to.setEnabled(false);
		from.setColumns(8);
		to.setColumns(8);
		final JCheckBox time = new JCheckBox(b.l("bsui.search.full.opt.time"));
		time.setSelected(false);
		time.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (time.isSelected()) {
					from.setEnabled(true);
					to.setEnabled(true);
				} else {
					from.setEnabled(false);
					to.setEnabled(false);
				}
			}
		});
		try {
			from.setFormatterFactory(new DefaultFormatterFactory(
					new MaskFormatter("##.##.####")));
			from.setText(new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
			to.setFormatterFactory(new DefaultFormatterFactory(
					new MaskFormatter("##.##.####")));
			to.setText(new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
		} catch (Exception e1) {
			time.setSelected(false);
			time.setEnabled(false);
		}

		// ----------------------------
		// compose components
		panel.add(search, "width 100% !, wrap");

		Bs.addSeparator(panel, b.l("bsui.search.full.c"));
		panel.add(or, "span, split 2");
		panel.add(and, "gap unrel, wrap");

		Bs.addSeparator(panel, b.l("bsui.search.full.in"));
		if (b.getViewFlag() == Bs.VIEW_FLAG_CD
				|| b.getViewFlag() == Bs.VIEW_FLAG_MUSICFILES) {
			panel.add(inName, "span, split 3");
			panel.add(inInterpret, "gap unrel");
			panel.add(inDesc, "gap unrel");
		} else {
			panel.add(inName, "span, split 5");
			panel.add(inType, "gap unrel");
			panel.add(inSN, "gap unrel");
			panel.add(inLocation, "gap unrel");
			panel.add(inDesc, "gap unrel");
		}

		Bs.addSeparator(panel, b.l("bsui.search.full.opt"));
		panel.add(loan, "wrap");
		panel.add(state, "wrap");
		if (b.getViewFlag() == Bs.VIEW_FLAG_CD
				|| b.getViewFlag() == Bs.VIEW_FLAG_INVENTORY) {
			panel.add(statusOk, "gap unrel, span, split 4");
			panel.add(statusDamaged, "gap unrel");
			panel.add(statusDestroyed, "gap unrel");
			panel.add(statusLost, "gap unrel, wrap");
		} else {
			panel.add(statusOk, "gap unrel, span, split 2");
			panel.add(statusDamaged, "gap unrel, wrap");
		}
		panel.add(time, "wrap");
		panel.add(new JLabel(b.l("bsui.search.full.opt.time.from")),
				"gap unrel, span, split 4");
		panel.add(from, "gap unrel");
		panel.add(new JLabel(b.l("bsui.search.full.opt.time.to")), "gap unrel");
		panel.add(to, "gap unrel");

		// ----------------------------
		// show dialog
		int re = JOptionPane.showConfirmDialog(f, panel,
				b.l("bsui.search.full"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null);
		if (re == 0) {
			if (b.getViewFlag() == Bs.VIEW_FLAG_CD) {
				CriteriaQuery cq = null;
				if (search.getText().length() < 1 && !loan.isSelected()
						&& !state.isSelected() && !time.isSelected()) {
					Bs.err(f, b.l("Info.013"), b);
					return false;
				}
				if (!inName.isSelected() && !inInterpret.isSelected() &&
						!inDesc.isSelected() && search.getText().length() != 5
						 && search.getText().length() != 13) {
					Bs.err(f, b.l("Info.012"), b);
					return false;
				}
				if (or.isSelected()) {
					Or q = Where.or();
					// search in the categories
					if (search.getText().length() > 0) {
						String f = search.getText();
						try {
							if (DIGITS5.matcher(f).matches()) {
								q.add(Where.equal("orderId", Long.parseLong(f)));
							} else if (DIGITS13.matcher(f).matches()) {
								q.add(Where.equal("barcode", f));
							}
						} catch (Exception e) {
							e.printStackTrace(b.tty());
						}
						
						if (inName.isSelected()) {
							q.add(Where.like("name", f));
						}
						if (inInterpret.isSelected()) {
							q.add(Where.like("interpret", f));
						}
						if (inDesc.isSelected()) {
							q.add(Where.like("description", f));
						}
					}
					// do the rest stuff
					if (loan.isSelected()) {
						q.add(Where.equal("isBorrowed", true));
					}
					if (state.isSelected()) {
						int s = (statusOk.isSelected() ? DBOEntry.STATE_OKAY
								: (statusLost.isSelected() ? DBOEntry.STATE_LOST
										: (statusDestroyed.isSelected() ? DBOEntry.STATE_DESTROYED
												: (statusDamaged.isSelected() ? DBOEntry.STATE_DAMAGED
														: -1))));
						if (s > -1) {
							q.add(Where.equal("flag", s));
						}
					}
					if (time.isSelected()) {
						try {
							long startI = new SimpleDateFormat("dd.MM.yyyy")
									.parse(from.getText()).getTime();
							long endI = new SimpleDateFormat("dd.MM.yyyy")
									.parse(to.getText()).getTime()
									+ ((24 * 60 * 60 * 1000) - 1000);
							q.add(Where.and()
									.add(Where.ge("creationDate", startI))
									.add(Where.le("creationDate", endI)));
						} catch (Exception e2) {
							e2.printStackTrace(b.tty());
						}
					}
					cq = new CriteriaQuery(CD.class, q);
				} else {
					And q = Where.and();
					// search in the categories
					if (search.getText().length() > 0) {
						String f = search.getText();
						try {
							if (DIGITS5.matcher(f).matches()) {
								q.add(Where.equal("orderId", Long.parseLong(f)));
							} else if (DIGITS13.matcher(f).matches()) {
								q.add(Where.equal("barcode", f));
							}
						} catch (Exception e) {
							e.printStackTrace(b.tty());
						}
						
						if (inName.isSelected()) {
							q.add(Where.like("name", f));
						}
						if (inInterpret.isSelected()) {
							q.add(Where.like("interpret", f));
						}
						if (inDesc.isSelected()) {
							q.add(Where.like("description", f));
						}
					}
					// do the rest stuff
					if (loan.isSelected()) {
						q.add(Where.equal("isBorrowed", true));
					}
					if (state.isSelected()) {
						int s = (statusOk.isSelected() ? DBOEntry.STATE_OKAY
								: (statusLost.isSelected() ? DBOEntry.STATE_LOST
										: (statusDestroyed.isSelected() ? DBOEntry.STATE_DESTROYED
												: (statusDamaged.isSelected() ? DBOEntry.STATE_DAMAGED
														: -1))));
						if (s > -1) {
							q.add(Where.equal("flag", s));
						}
					}
					if (time.isSelected()) {
						try {
							long startI = new SimpleDateFormat("dd.MM.yyyy")
									.parse(from.getText()).getTime();
							long endI = new SimpleDateFormat("dd.MM.yyyy")
									.parse(to.getText()).getTime()
									+ ((24 * 60 * 60 * 1000) - 1000);
							q.add(Where.and()
									.add(Where.ge("creationDate", startI))
									.add(Where.le("creationDate", endI)));
						} catch (Exception e2) {
							e2.printStackTrace(b.tty());
						}
					}
					cq = new CriteriaQuery(CD.class, q);
				}
				// finish ...
				if (cq != null) {
					return b.setSearch(cq);
				}
			} else if (b.getViewFlag() == Bs.VIEW_FLAG_INVENTORY) {
				CriteriaQuery cq = null;
				if (search.getText().length() < 1 && !loan.isSelected()
						&& !state.isSelected() && !time.isSelected()) {
					Bs.err(f, b.l("Info.013"), b);
					return false;
				}
				if (!inName.isSelected() && !inType.isSelected() &&
						!inSN.isSelected() && !inLocation.isSelected() &&
						!inDesc.isSelected() && search.getText().length() != 5
						 && search.getText().length() != 13) {
					Bs.err(f, b.l("Info.012"), b);
					return false;
				}
				if (or.isSelected()) {
					Or q = Where.or();
					// search in the categories
					if (search.getText().length() > 0) {
						String f = search.getText();
						try {
							if (DIGITS5.matcher(f).matches()) {
								q.add(Where.equal("orderId", Long.parseLong(f)));
							} else if (DIGITS13.matcher(f).matches()) {
								q.add(Where.equal("barcode", f));
							}
						} catch (Exception e) {
							e.printStackTrace(b.tty());
						}

						if (inName.isSelected()) {
							q.add(Where.like("name", f));
						}
						if (inType.isSelected()) {
							q.add(Where.like("type", f));
						}
						if (inSN.isSelected()) {
							q.add(Where.like("serialNumber", f));
						}
						if (inLocation.isSelected()) {
							q.add(Where.like("location", f));
						}
						if (inDesc.isSelected()) {
							q.add(Where.like("description", f));
						}
					}
					// do the rest stuff
					if (loan.isSelected()) {
						q.add(Where.equal("isBorrowed", true));
					}
					if (state.isSelected()) {
						int s = (statusOk.isSelected() ? DBOEntry.STATE_OKAY
								: (statusLost.isSelected() ? DBOEntry.STATE_LOST
										: (statusDestroyed.isSelected() ? DBOEntry.STATE_DESTROYED
												: (statusDamaged.isSelected() ? DBOEntry.STATE_DAMAGED
														: -1))));
						if (s > -1) {
							q.add(Where.equal("flag", s));
						}
					}
					if (time.isSelected()) {
						try {
							long startI = new SimpleDateFormat("dd.MM.yyyy")
									.parse(from.getText()).getTime();
							long endI = new SimpleDateFormat("dd.MM.yyyy")
									.parse(to.getText()).getTime()
									+ ((24 * 60 * 60 * 1000) - 1000);
							q.add(Where.and()
									.add(Where.ge("creationDate", startI))
									.add(Where.le("creationDate", endI)));
						} catch (Exception e2) {
							e2.printStackTrace(b.tty());
						}
					}
					cq = new CriteriaQuery(Inventory.class, q);
				} else {
					And q = Where.and();
					// search in the categories
					if (search.getText().length() > 0) {
						String f = search.getText();
						try {
							if (DIGITS5.matcher(f).matches()) {
								q.add(Where.equal("orderId", Long.parseLong(f)));
							} else if (DIGITS13.matcher(f).matches()) {
								q.add(Where.equal("barcode", f));
							}
						} catch (Exception e) {
							e.printStackTrace(b.tty());
						}

						if (inName.isSelected()) {
							q.add(Where.like("name", f));
						}
						if (inType.isSelected()) {
							q.add(Where.like("type", f));
						}
						if (inSN.isSelected()) {
							q.add(Where.like("serialNumber", f));
						}
						if (inLocation.isSelected()) {
							q.add(Where.like("location", f));
						}
						if (inDesc.isSelected()) {
							q.add(Where.like("description", f));
						}
					}
					// do the rest stuff
					if (loan.isSelected()) {
						q.add(Where.equal("isBorrowed", true));
					}
					if (state.isSelected()) {
						int s = (statusOk.isSelected() ? DBOEntry.STATE_OKAY
								: (statusLost.isSelected() ? DBOEntry.STATE_LOST
										: (statusDestroyed.isSelected() ? DBOEntry.STATE_DESTROYED
												: (statusDamaged.isSelected() ? DBOEntry.STATE_DAMAGED
														: -1))));
						if (s > -1) {
							q.add(Where.equal("flag", s));
						}
					}
					if (time.isSelected()) {
						try {
							long startI = new SimpleDateFormat("dd.MM.yyyy")
									.parse(from.getText()).getTime();
							long endI = new SimpleDateFormat("dd.MM.yyyy")
									.parse(to.getText()).getTime()
									+ ((24 * 60 * 60 * 1000) - 1000);
							q.add(Where.and()
									.add(Where.ge("creationDate", startI))
									.add(Where.le("creationDate", endI)));
						} catch (Exception e2) {
							e2.printStackTrace(b.tty());
						}
					}
					cq = new CriteriaQuery(Inventory.class, q);
				}
				// finish ...
				if (cq != null) {
					return b.setSearch(cq);
				}
			} else if (b.getViewFlag() == Bs.VIEW_FLAG_MUSICFILES) {
				CriteriaQuery cq = null;
				if (search.getText().length() < 1 && !loan.isSelected()
						&& !state.isSelected() && !time.isSelected()) {
					Bs.err(f, b.l("Info.013"), b);
					return false;
				}
				if (!inName.isSelected() && !inInterpret.isSelected() &&
						!inDesc.isSelected() && search.getText().length() != 5
						 && search.getText().length() != 13) {
					Bs.err(f, b.l("Info.012"), b);
					return false;
				}
				if (or.isSelected()) {
					Or q = Where.or();
					// search in the categories
					if (search.getText().length() > 0) {
						String f = search.getText();
						try {
							if (DIGITS5.matcher(f).matches()) {
								q.add(Where.equal("orderId", Long.parseLong(f)));
							} else if (DIGITS13.matcher(f).matches()) {
								q.add(Where.equal("barcode", f));
							}
						} catch (Exception e) {
							e.printStackTrace(b.tty());
						}

						if (inName.isSelected()) {
							q.add(Where.like("name", f));
						}
						if (inInterpret.isSelected()) {
							q.add(Where.like("interpret", f));
						}
						if (inDesc.isSelected()) {
							q.add(Where.like("description", f));
						}
					}
					// do the rest stuff
					if (loan.isSelected()) {
						q.add(Where.equal("isBorrowed", true));
					}
					if (state.isSelected()) {
						int s = (statusOk.isSelected() ? DBOEntry.STATE_OKAY
								: (statusLost.isSelected() ? DBOEntry.STATE_LOST
										: (statusDestroyed.isSelected() ? DBOEntry.STATE_DESTROYED
												: (statusDamaged.isSelected() ? DBOEntry.STATE_DAMAGED
														: -1))));
						if (s > -1) {
							q.add(Where.equal("flag", s));
						}
					}
					if (time.isSelected()) {
						try {
							long startI = new SimpleDateFormat("dd.MM.yyyy")
									.parse(from.getText()).getTime();
							long endI = new SimpleDateFormat("dd.MM.yyyy")
									.parse(to.getText()).getTime()
									+ ((24 * 60 * 60 * 1000) - 1000);
							q.add(Where.and()
									.add(Where.ge("creationDate", startI))
									.add(Where.le("creationDate", endI)));
						} catch (Exception e2) {
							e2.printStackTrace(b.tty());
						}
					}
					cq = new CriteriaQuery(MusicFile.class, q);
				} else {
					And q = Where.and();
					// search in the categories
					if (search.getText().length() > 0) {
						String f = search.getText();
						try {
							if (DIGITS5.matcher(f).matches()) {
								q.add(Where.equal("orderId", Long.parseLong(f)));
							} else if (DIGITS13.matcher(f).matches()) {
								q.add(Where.equal("barcode", f));
							}
						} catch (Exception e) {
							e.printStackTrace(b.tty());
						}

						if (inName.isSelected()) {
							q.add(Where.like("name", f));
						}
						if (inInterpret.isSelected()) {
							q.add(Where.like("interpret", f));
						}
						if (inDesc.isSelected()) {
							q.add(Where.like("description", f));
						}
					}
					// do the rest stuff
					if (loan.isSelected()) {
						q.add(Where.equal("isBorrowed", true));
					}
					if (state.isSelected()) {
						int s = (statusOk.isSelected() ? DBOEntry.STATE_OKAY
								: (statusLost.isSelected() ? DBOEntry.STATE_LOST
										: (statusDestroyed.isSelected() ? DBOEntry.STATE_DESTROYED
												: (statusDamaged.isSelected() ? DBOEntry.STATE_DAMAGED
														: -1))));
						if (s > -1) {
							q.add(Where.equal("flag", s));
						}
					}
					if (time.isSelected()) {
						try {
							long startI = new SimpleDateFormat("dd.MM.yyyy")
									.parse(from.getText()).getTime();
							long endI = new SimpleDateFormat("dd.MM.yyyy")
									.parse(to.getText()).getTime()
									+ ((24 * 60 * 60 * 1000) - 1000);
							q.add(Where.and()
									.add(Where.ge("creationDate", startI))
									.add(Where.le("creationDate", endI)));
						} catch (Exception e2) {
							e2.printStackTrace(b.tty());
						}
					}
					cq = new CriteriaQuery(MusicFile.class, q);
				}
				// finish ...
				if (cq != null) {
					return b.setSearch(cq);
				}
			}
			return false;
		}
		return false;
	}
	
	private class NewItemAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		{
			putValue(Action.NAME, b.l("Menu.010"));
			putValue(Action.LARGE_ICON_KEY, UIIco.LIST_ADD_24);
			putValue(Action.SMALL_ICON, UIIco.LIST_ADD_16);
		}
		public void actionPerformed(ActionEvent e) {
			if (b.getViewFlag() == Bs.VIEW_FLAG_CD) {
				CDItemDialog cdid = new CDItemDialog(b, f, null);
				cdid.init();
				saveFileAction.setEnabled(true);
			} else if (b.getViewFlag() == Bs.VIEW_FLAG_INVENTORY) {
				InventoryItemDialog iid = new InventoryItemDialog(
						b, f, null);
				iid.init();
				saveFileAction.setEnabled(true);
			} else {
				MusicItemDialog mid = new MusicItemDialog(b, f, null);
				mid.init();
				saveFileAction.setEnabled(true);
			}
		}
	}
	
	private class EditItemAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		{
			putValue(Action.NAME, b.l("Menu.009"));
			putValue(Action.LARGE_ICON_KEY, UIIco.EDIT_24);
			putValue(Action.SMALL_ICON, UIIco.EDIT_16);
		}
		public void actionPerformed(ActionEvent e) {
			Object o = itemList.getSelectedValue();
			if (o == null) return;
			
			if (o instanceof CD) {
				CDItemDialog cdid = new CDItemDialog(b, f, (CD) o);
				cdid.init();
				saveFileAction.setEnabled(true);
			} else if (o instanceof Inventory) {
				InventoryItemDialog iid = new InventoryItemDialog(
						b, f, (Inventory) o);
				iid.init();
				saveFileAction.setEnabled(true);
			} else {
				MusicItemDialog mid = new MusicItemDialog(
						b, f, (MusicFile) o);
				mid.init();
				saveFileAction.setEnabled(true);
			}
		}
	}
	
	private class RemoveItemAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		{
			putValue(Action.NAME, b.l("Menu.012"));
			putValue(Action.SMALL_ICON, UIIco.LIST_REMOVE_16);
		}
		public void actionPerformed(ActionEvent e) {
			Object[] o = itemList.getSelectedValues();
			if (o != null && o.length > 0) {
				if (Bs.ask(f, b.l("bsui.q.remove"), b)) {
					for (Object object : o) {
						b.removeElement(object);
					}
					fireUpdate();
					saveFileAction.setEnabled(true);
				}
			}
		}
	}
	
	private class ExportAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		{
			putValue(Action.NAME, b.l("Menu.015"));
			putValue(Action.SMALL_ICON, UIIco.DOC_PRINT_16);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_E, InputEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			// create elements
			final JToggleButton datasheet = new JToggleButton();
			final JToggleButton list = new JToggleButton();
			final JToggleButton label = new JToggleButton();
			final JToggleButton allSelect = new JToggleButton();
			final JToggleButton allShown = new JToggleButton();
			
			// set values
			if (itemList.getModel().getSize() > 0) {
				allShown.setEnabled(true);
				if (itemList.getSelectedIndices().length > 0)
					allSelect.setEnabled(true);
				else allSelect.setEnabled(false);
			} else {
				allShown.setEnabled(false);
				allSelect.setEnabled(false);
			}
				
			// set texts
			datasheet.setText("<html><head></head><body><p>" +
					b.l("Info.014") + "</p></body></html>");
			datasheet.setHorizontalTextPosition(JToggleButton.LEFT);
			list.setText("<html><head></head><body><p>" +
					b.l("Info.015") + "</p></body></html>");
			list.setHorizontalTextPosition(JToggleButton.LEFT);
			label.setText("<html><head></head><body><p>" +
					b.l("Info.016") + "</p></body></html>");
			label.setHorizontalTextPosition(JToggleButton.LEFT);
			ButtonGroup bg = new ButtonGroup();
			bg.add(datasheet);
			bg.add(list);
			bg.add(label);
			allSelect.setText("<html><head></head><body><p>" +
					b.l("Title.011") + "</p></body></html>");
			allShown.setText("<html><head></head><body><p>" +
					b.l("Title.010") + "</p></body></html>");
			ButtonGroup bg2 = new ButtonGroup();
			bg2.add(allSelect);
			bg2.add(allShown);
			
			// compose panel
			JPanel ppanel = new JPanel(new MigLayout("insets 0 0 0 0"));
	        Bs.addSeparator(ppanel, b.l("Title.006"));
	        ppanel.add(datasheet, "height 25%, width 100%, wrap");
	        ppanel.add(list, "height 25%, width 100%, wrap");
	        ppanel.add(label, "height 25%, width 100%, wrap");
	        Bs.addSeparator(ppanel, b.l("Title.008"));
	        JPanel c01 = new JPanel(new MigLayout("insets 0 0 0 0"));
	        c01.add(allSelect, "width 50%");
	        c01.add(allShown, "width 50%");
	        ppanel.add(c01, "span, width 100%, height 25%");
			ppanel.setPreferredSize(new Dimension(500, 350));
			
			// display export dialog
			int result = JOptionPane.showConfirmDialog(
					f, ppanel, b.l("bsui.export"), 
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.INFORMATION_MESSAGE, 
					UIIco.DOC_PRINT_48
			);
			// cancel action
			if (result != 0)  return;
			// wrong input
			if ((!datasheet.isSelected() && !list.isSelected() 
					&& !label.isSelected()) || (!allSelect.isSelected() &&
							!allShown.isSelected())) {
				Bs.err(f, b.l("Info.017"), b);
				return;
			}
			
			// if label is selected show label params
			Label labelToUse = null;
			final JSpinner pageBorder = new JSpinner(new SpinnerNumberModel(1, 1, 300, 1));
			final JSpinner topBorder = new JSpinner(new SpinnerNumberModel(5, 1, 300, 1));
			final JSpinner height = new JSpinner(new SpinnerNumberModel(40, 1, 300, 1));
			final JSpinner width = new JSpinner(new SpinnerNumberModel(70, 1, 300, 1));
			final JSpinner perRow = new JSpinner(new SpinnerNumberModel(3, 1, 20, 1));
			final JSpinner perColumn = new JSpinner(new SpinnerNumberModel(7, 1, 20, 1));
			final JSpinner start = new JSpinner(new SpinnerNumberModel(1, 1, 300, 1));
			if (label.isSelected()) {
				Label[] lbs = b.getLabelFormats();
				perRow.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						int c = (Integer) perColumn.getValue();
						int r = (Integer) perRow.getValue();
						int old = (Integer) start.getValue();
						old = (old > r*c ? 1 : old);
						start.setModel(new SpinnerNumberModel(old, 1, r*c, 1));
					}
				});
				final JComboBox cb = new JComboBox(lbs);
				cb.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						Object o = cb.getSelectedItem();
						if (o == null || !(o instanceof Label)) return;
						Label l = (Label) o;
						height.setModel(
								new SpinnerNumberModel(l.height, 1, 300, 1));
						width.setModel(
								new SpinnerNumberModel(l.width, 1, 300, 1));
						pageBorder.setModel(
								new SpinnerNumberModel(l.pageBorder, 1, 300, 1));
						topBorder.setModel(
								new SpinnerNumberModel(l.topBorder, 1, 300, 1));
						perRow.setModel(
								new SpinnerNumberModel(l.labelsPerRow, 1, 20, 1));
						perColumn.setModel(
								new SpinnerNumberModel(l.labelsPerColumn, 1, 20, 1));
						start.setModel(
								new SpinnerNumberModel(l.startLabel, 1, 40, 1));
					}
				});
				
				// compose panel
				JPanel panel = new JPanel(new MigLayout("insets 0 0 0 0"));
				panel.add(new JLabel(UIIco.LABEL), "align center, wrap");
				Bs.addSeparator(panel, b.l("bsui.export.esize"));
				panel.add(cb, "width 100%, wrap");
				JPanel panel1 = new JPanel(new MigLayout("insets 0 0 0 0"));
				panel1.add(new JLabel(b.l("bsui.export.l.pb") + ": "));
				panel1.add(pageBorder, "sg");
				panel1.add(new JLabel(b.l("bsui.export.l.tb") + ": "), "gap unrel");
				panel1.add(topBorder, "sg, wrap");
				panel1.add(new JLabel(b.l("bsui.export.l.h") + ": "));
				panel1.add(height, "sg");
				panel1.add(new JLabel(b.l("bsui.export.l.w") + ": "), "gap unrel");
				panel1.add(width, "sg, wrap");
				panel1.add(new JLabel(b.l("bsui.export.l.epc") + ": "));
				panel1.add(perColumn, "sg");
				panel1.add(new JLabel(b.l("bsui.export.l.epr") + ": "), "gap unrel");
				panel1.add(perRow, "sg");
				panel1.add(new JLabel(b.l("bsui.export.l.s") + ": "), "gap unrel");
				panel1.add(start, "sg");
				panel.add(panel1);
				panel.setPreferredSize(new Dimension(500, 350));
				int r = JOptionPane.showConfirmDialog(
						f, panel, b.l("bsui.export.l"), 
						JOptionPane.OK_CANCEL_OPTION, 
						JOptionPane.QUESTION_MESSAGE, 
						UIIco.DOC_PRINT_48
					);
				
				// export was cancelled
				if (r != 0) return;
				
				labelToUse = new Label();
				labelToUse.pageBorder = (Integer) pageBorder.getValue();
				labelToUse.topBorder = (Integer) topBorder.getValue();
				labelToUse.height = (Integer) height.getValue();
				labelToUse.width = (Integer) width.getValue();
				labelToUse.labelsPerColumn = (Integer) perColumn.getValue();
				labelToUse.labelsPerRow = (Integer) perRow.getValue();
				labelToUse.startLabel = (Integer) start.getValue();
				
				// save it
				b.storeLabelFormat(labelToUse);
				b.flushDatabase();
			}
			if (label.isSelected() && labelToUse == null) {
				Bs.err(f, b.l("Info.018"), b);
				return;
			}
			
			// find a save location
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setDialogTitle(b.l("bsui.export.l"));
			fc.setFileFilter(new FileFilter() {
				public String getDescription() {
					return b.l("bsui.export.pdf");
				}
				public boolean accept(File f) {
					return (f.isDirectory() || f.getAbsolutePath().endsWith(".pdf"));
				}
			});
			int re = fc.showSaveDialog(f);
			// check if canceld
			if (re != JFileChooser.APPROVE_OPTION) return;
			File file2Store = new File((
					fc.getSelectedFile().getAbsolutePath().endsWith(".pdf") ? 
							fc.getSelectedFile().getAbsolutePath() : 
								fc.getSelectedFile().getAbsolutePath() + ".pdf"
			));
			// if file exsist
			if (file2Store.exists()) {
				if (!Bs.ask(f, b.l("Info.019"), b)) return;
			}
			
			// DO EXPORT +++ DO EXPORT +++ DO EXPORT +++ DO EXPORT
			// DO EXPORT +++ DO EXPORT +++ DO EXPORT +++ DO EXPORT
			Object[] o = null;
			if (allSelect.isSelected()) {
				o = itemList.getSelectedValues();
			} else {
				o = new Object[itemList.getModel().getSize()];
				for (int i = 0; i < o.length; i++)
					o[i] = itemList.getModel().getElementAt(i);
			}
			
			// last check
			if (o == null) {
				Bs.err(f, b.l("Info.020"), b);
				return;
			}
			
			if (datasheet.isSelected()) {
				// EXPORT: DATASHEET
				StringBuffer allHtml = new StringBuffer();
				allHtml.append("<html><head><title></title>");
				allHtml.append(Config.STYLESHEET);
				allHtml.append("</head><body>");
				for (int i = 0; i < o.length; i++) {
					DBOEntry dbo = (DBOEntry) o[i];
					if (dbo != null) {
						allHtml.append(dbo.formatToXHTML(b));
						String copy = b.l("Info.021").replaceAll("%%1", "<span id=\"pagenumber\"/>")
									.replaceAll("%%2", "<span id=\"pagecount\"/>")
									.replaceAll("%%4", Bs.formatDate(new Date()))
									.replaceAll("%%3", Config.APP_IDENT);
						if (i < o.length-1) {
							allHtml.append("<div class=\"copyr\" style=\"page-break-after: always;\">" +
									copy + "</div>");
						} else {
							allHtml.append("<div class=\"copyr\">" +
									copy + "</div>");
						}
					} else {
						try {
							throw new NullPointerException("DBOEntry object is " +
									"null. skipped.");
						} catch (Exception e2) {
							e2.printStackTrace(b.tty());
						}
					}
				}
				allHtml.append("</body></html>");
				
				try {
					DocumentBuilder builder = DocumentBuilderFactory.newInstance()
							.newDocumentBuilder();
					Document doc = builder.parse(new InputSource(new StringReader(
							allHtml.toString())));
					ITextRenderer renderer = new ITextRenderer();
					renderer.setDocument(doc, null);
					renderer.layout();
					OutputStream os = new FileOutputStream(file2Store);
					renderer.createPDF(os);
					os.close();
				} catch (Exception e2) {
					e2.printStackTrace(b.tty());
					Bs.err(f, b.l("Info.022"), b);
				}
				return;
			} else if (list.isSelected()) {
				// LIST IS SELECTED
				StringBuffer allHtml = new StringBuffer();
				allHtml.append("<html><head><title></title>");
				allHtml.append(Config.STYLESHEET);
				allHtml.append("</head><body><p><table width=\"100%\">");
				if (o[0] instanceof CD) {
					allHtml.append("<tr>");
					allHtml.append("<td><b>" + b.l("ex.nfo.headline.id") + "</b></td>");
					allHtml.append("<td><b>" + b.l("ex.nfo.headline.nm") + "</b></td>");
					allHtml.append("<td><b>" + b.l("ex.nfo.headline.ipr") + "</b></td>");
					allHtml.append("<td><b>" + b.l("ex.nfo.headline.wdt") + "</b></td>");
					allHtml.append("<td><b>" + b.l("ex.nfo.headline.bor") + "</b></td>");
					allHtml.append("<td><b>" + b.l("ex.nfo.headline.stat") + "</b></td>");
					allHtml.append("</tr>");
					for (int j = 0; j < o.length; j++) {
						CD cd = (CD) o[j];
						allHtml.append("<tr>");
						allHtml.append("<td><b>" + Bs.fill((int) cd.getOrderId(), 5, "0") + "</b></td>");
						allHtml.append("<td>" + cd.getName() + "</td>");
						allHtml.append("<td>" + (cd.getInterpret() == null ? 
								"-" : cd.getInterpret()) + "</td>");
						allHtml.append("<td>" + cd.getTrackCount() + "/" +
								Bs.formatAsTime(cd.getTotalDuration()) + "</td>");
						allHtml.append("<td>" + (cd.isBorrowed() ? b.l("ex.nfo.headline.bor.y") :
							b.l("ex.nfo.headline.bor.n")) + "</td>");
						String status = b.l("cdid.lbl.gen.status.un");
						if (cd.getState() == DBOEntry.STATE_OKAY)
							status = b.l("cdid.lbl.gen.status.ok");
						if (cd.getState() == DBOEntry.STATE_DAMAGED)	
							status = b.l("cdid.lbl.gen.status.dam");
						if (cd.getState() == DBOEntry.STATE_DESTROYED)
							status = b.l("cdid.lbl.gen.status.des");		
						if (cd.getState() == DBOEntry.STATE_LOST)
							status = b.l("cdid.lbl.gen.status.los");
						allHtml.append("<td>" + status + "</td>");
						allHtml.append("</tr>");
					}
				} else if (b.getViewFlag() == Bs.VIEW_FLAG_INVENTORY) {
					allHtml.append("<tr>");
					allHtml.append("<td><b>" + b.l("ex.nfo.headline.id") + "</b></td>");
					allHtml.append("<td><b>" + b.l("ex.nfo.headline.nm") + "</b></td>");
					allHtml.append("<td><b>" + b.l("ex.nfo.headline.ity") + "</b></td>");
					allHtml.append("<td><b>" + b.l("ex.nfo.headline.ipu") + "</b></td>");
					allHtml.append("<td><b>" + b.l("ex.nfo.headline.ilo") + "</b></td>");
					allHtml.append("<td><b>" + b.l("ex.nfo.headline.bor") + "</b></td>");
					allHtml.append("<td><b>" + b.l("ex.nfo.headline.stat") + "</b></td>");
					allHtml.append("</tr>");
					for (int j = 0; j < o.length; j++) {
						Inventory i = (Inventory) o[j];
						allHtml.append("<tr>");
						allHtml.append("<td><b>" + Bs.fill((int) i.getOrderId(), 5, "0") + "</b></td>");
						allHtml.append("<td>" + i.getName() + "</td>");
						allHtml.append("<td>" + (i.getType() == null ? 
								"" : i.getType()) + "</td>");
						allHtml.append("<td>" + i.getPurchasedOn() + "</td>");
						allHtml.append("<td>" + (i.getLocation() == null ? "-" :
							i.getLocation()) + "</td>");
						allHtml.append("<td>" + (i.isBorrowed() ? b.l("ex.nfo.headline.bor.y") :
							b.l("ex.nfo.headline.bor.n")) + "</td>");
						String status = b.l("cdid.lbl.gen.status.un");
						if (i.getState() == DBOEntry.STATE_OKAY)
							status = b.l("cdid.lbl.gen.status.ok");
						if (i.getState() == DBOEntry.STATE_DAMAGED)	
							status = b.l("cdid.lbl.gen.status.dam");
						if (i.getState() == DBOEntry.STATE_DESTROYED)
							status = b.l("cdid.lbl.gen.status.des");		
						if (i.getState() == DBOEntry.STATE_LOST)
							status = b.l("cdid.lbl.gen.status.los");
						allHtml.append("<td>" + status + "</td>");
						allHtml.append("</tr>");
					}
				} else {
					allHtml.append("<tr>");
					allHtml.append("<td><b>" + b.l("ex.nfo.headline.id") + "</b></td>");
					allHtml.append("<td><b>" + b.l("ex.nfo.headline.nm") + "</b></td>");
					allHtml.append("<td><b>" + b.l("ex.nfo.headline.ity") + "</b></td>");
					allHtml.append("<td><b>" + b.l("ex.nfo.headline.dur") + "</b></td>");
					allHtml.append("</tr>");
					for (int j = 0; j < o.length; j++) {
						MusicFile mf = (MusicFile) o[j];
						allHtml.append("<tr>");
						allHtml.append("<td><b>" + Bs.fill((int) mf.getOrderId(), 5, "0") + "</b></td>");
						allHtml.append("<td>" + mf.getName() + "</td>");
						allHtml.append("<td>" + (mf.getInterpret() == null ? 
								"" : mf.getInterpret()) + "</td>");
						allHtml.append("<td>" + Bs.formatAsTime(mf.getDuration()) + "</td>");
						allHtml.append("</tr>");
					}
				}
				allHtml.append("</table></p>");
				String copy = b.l("Info.021").replaceAll("%%1", "<span id=\"pagenumber\"/>")
					.replaceAll("%%2", "<span id=\"pagecount\"/>")
					.replaceAll("%%4", Bs.formatDate(new Date()))
					.replaceAll("%%3", Config.APP_IDENT);
				allHtml.append("<div class=\"copyr\">" + copy + "</div>");
				allHtml.append("</body></html>");
				
				try {
					DocumentBuilder builder = DocumentBuilderFactory.newInstance()
							.newDocumentBuilder();
					Document doc = builder.parse(new InputSource(new StringReader(
							allHtml.toString())));
					ITextRenderer renderer = new ITextRenderer();
					renderer.setDocument(doc, null);
					renderer.layout();
					OutputStream os = new FileOutputStream(file2Store);
					renderer.createPDF(os);
					os.close();
				} catch (Exception e2) {
					e2.printStackTrace(b.tty());
					Bs.err(f, b.l("Info.022"), b);
				}
				return;
			} else {
				// LABEL EXPORT
				try {
					// create labels
					int seitenraender = (Integer) pageBorder.getValue(), 
						oberrerand = (Integer) topBorder.getValue(), 
						hoehe = (Integer) height.getValue(), 
						etiProZeile = (Integer) perRow.getValue(),
						etiProSpalte = (Integer) perColumn.getValue(), 
						startelement = ((Integer) start.getValue()) - 1, 
						breite = (Integer) width.getValue();
					
					Font small = new Font(Font.COURIER, 
							10, Font.NORMAL, new Color(120, 120, 120));
					Font normal = new Font(Font.COURIER, 
							10, Font.NORMAL, new Color(0, 0, 0));
					
					// create the document
					com.lowagie.text.Document document = new com.lowagie.text.Document(
							PageSize.A4, 
							Ean13Util.mm2px(seitenraender), 
							Ean13Util.mm2px(seitenraender), 
							Ean13Util.mm2px(oberrerand), 
							Ean13Util.mm2px(oberrerand)
						);
					PdfWriter.getInstance(document,
							new FileOutputStream(file2Store));
					document.open();
					
					int ocnt = 0;
					int run = 0;
					while (ocnt < o.length) {
						if (run > 0) {
							startelement = 0;
						}
						run++;
						
						document.newPage();
						PdfPTable info = new PdfPTable(etiProZeile);
						info.setLockedWidth(true);
						info.setTotalWidth(PageSize.A4.getWidth()-(2*Ean13Util.mm2px(seitenraender)));
						
						for (int i = 0; i < startelement; i++) {
							PdfPCell c = new PdfPCell();
							c.setFixedHeight(Ean13Util.mm2px(hoehe));
							c.setBorder(PdfPCell.NO_BORDER);
							info.addCell(c);
						}
						
						for (int i = startelement; i < (etiProZeile*etiProSpalte); i++) {
							PdfPCell c = new PdfPCell();
							c.setBorder(PdfPCell.NO_BORDER);
							c.setFixedHeight(Ean13Util.mm2px(hoehe));
							if (ocnt < o.length) {
								Paragraph p = new Paragraph();
								String code = "";
								
								if (o[ocnt] instanceof CD) {
									CD cd = (CD) o[ocnt];
									code = cd.getId();
									p.add(new Paragraph(cd.getName(), normal));
									p.add(new Paragraph(cd.getInterpret(), small));
								} else if (o[ocnt] instanceof Inventory) {
									Inventory in = (Inventory) o[ocnt];
									code = in.getId();
									p.add(new Paragraph(in.getName(), normal));
									p.add(new Paragraph(in.getType(), small));
								} else if (o[ocnt] instanceof MusicFile) {
									MusicFile mf = (MusicFile) o[ocnt];
									code = mf.getId();
									p.add(new Paragraph(mf.getName(), normal));
									p.add(new Paragraph(mf.getInterpret(), small));
								}
								
								c.setPadding(Ean13Util.mm2px(2));
								// barcode
								Barcode barcode = BarcodeFactory.createEAN13(
										code.substring(0, 12));
								barcode.setBarWidth(2);
								BufferedImage image = new BufferedImage(
										barcode.getWidth(), barcode.getHeight(), 
										BufferedImage.TYPE_BYTE_GRAY);
								Graphics2D g = (Graphics2D) image.getGraphics();
								g.setColor(Color.WHITE);
								g.fillRect(0, 0, image.getWidth(), image.getHeight());
								barcode.draw(g, 0, 0);
								g.dispose();
								Image img1 = Image.getInstance(image, Color.black);
								
								PdfPTable grid = new PdfPTable(1);
								grid.setLockedWidth(true);
								grid.setTotalWidth(Ean13Util.mm2px(breite-4));
								PdfPCell cell = new PdfPCell(p);
								cell.setPaddingLeft(Ean13Util.mm2px(2));
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setFixedHeight(Ean13Util.mm2px((hoehe-4)/2));
								grid.addCell(cell);
								PdfPCell bcode = new PdfPCell(img1, true);
								bcode.setBorder(PdfPCell.NO_BORDER);
								grid.addCell(bcode);
								c.addElement(grid);
								ocnt++;
							}
							info.addCell(c);
						}
						document.add(info);
					}
					document.close();
				} catch (Exception e2) {
					e2.printStackTrace(b.tty());
					Bs.err(f, b.l("Info.022"), b);
				}
				return;
			}
		}
	}
	
	private class AboutAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		{
			putValue(Action.NAME, b.l("Menu.016"));
			putValue(Action.SMALL_ICON, UIIco.INFO_16);
		}
		public void actionPerformed(ActionEvent e) {
			AboutDialog ad = new AboutDialog(b, f);
			ad.init();
		}
	}

	private class ExitAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		{
			putValue(Action.NAME, b.l("Menu.017"));
			putValue(Action.SMALL_ICON, UIIco.APP_EXIT_16);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		}
		public void actionPerformed(ActionEvent e) {
			if (Bs.ask(f, b.l("bsui.q.quit.q"), b)) b.exit();
		}
	}
	
	private class NewFileAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		{
			putValue(Action.NAME, b.l("Menu.003"));
			putValue(Action.SMALL_ICON, UIIco.DOC_NEW_16);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_N, InputEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			if (b.isCollectionOpened()) {
				if (!Bs.ask(f, b.l("Ask.002"), b)) {
					return;
				}
				b.close();
				setNoFileOpenedView();
				fireUpdate();
			}
			
			JFileChooser fc = new JFileChooser(Config.getJARLocation());
			fc.setAcceptAllFileFilterUsed(false);
			fc.addChoosableFileFilter(new FileFilter() {
				public String getDescription() {
					return b.l("Info.023");
				}
				public boolean accept(File f) {
					return f.isDirectory() || f.getAbsolutePath().
								endsWith(Config.DB_EXTENSION);
				}
			});
			fc.setDialogTitle(b.l("Title.013"));
			int r = fc.showOpenDialog(f);
			if (r != JFileChooser.APPROVE_OPTION) return;
			File toOpen = fc.getSelectedFile();
			if (!toOpen.getAbsolutePath().endsWith(Config.DB_EXTENSION))
				toOpen = new File(toOpen.getAbsolutePath() + Config.DB_EXTENSION);
			if (toOpen.exists()) {
				if (!Bs.ask(f, b.l("Info.026"), b))
					return;
			}
			
			if (b.open(toOpen)) {
				setFileOpenedView();
				fireUpdate();
			} else Bs.err(f, b.l("Info.025"), b);
			return;
		}
	}
	
	private class OpenFileAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		{
			putValue(Action.NAME, b.l("Menu.002"));
			putValue(Action.LARGE_ICON_KEY, UIIco.DOC_OPEN_24);
			putValue(Action.SMALL_ICON, UIIco.DOC_OPEN_16);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_O, InputEvent.CTRL_MASK));
		}
		public void actionPerformed(ActionEvent e) {
			if (b.isCollectionOpened()) {
				if (!Bs.ask(f, b.l("Ask.002"), b)) {
					return;
				}
				b.close();
				setNoFileOpenedView();
				fireUpdate();
			}
			
			JFileChooser fc = new JFileChooser(Config.getJARLocation());
			fc.setAcceptAllFileFilterUsed(false);
			fc.addChoosableFileFilter(new FileFilter() {
				public String getDescription() {
					return b.l("Info.023");
				}
				public boolean accept(File f) {
					return f.isDirectory() || f.getAbsolutePath().
								endsWith(Config.DB_EXTENSION);
				}
			});
			fc.setDialogTitle(b.l("Title.012"));
			int r = fc.showOpenDialog(f);
			if (r != JFileChooser.APPROVE_OPTION) return;
			File toOpen = fc.getSelectedFile();
			if (!toOpen.canRead() || !toOpen.canWrite()) {
				Bs.err(f, b.l("Info.024"), b);
				return;
			}

			
			if (b.open(toOpen)) {
				setFileOpenedView();
				fireUpdate();
			} else Bs.err(f, b.l("Info.025"), b);
			return;
		}
	}
	
	private class SaveFileAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		{
			putValue(Action.NAME, b.l("Menu.004"));
			putValue(Action.LARGE_ICON_KEY, UIIco.DOC_SAV_24);
			putValue(Action.SMALL_ICON, UIIco.DOC_SAV_16);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_S, InputEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			b.flushDatabase();
			saveFileAction.setEnabled(false);
		}
	}

	private class OrderByObject {
		
		public String title;
		public String variableName;
		
		private OrderByObject(String title, String name) {
			this.title = title;
			this.variableName = name;
		}
		
		public String toString() {
			return this.title;
		}
		
	}

	private class Options extends AbstractAction {
		private static final long serialVersionUID = 1L;
		{
			putValue(Action.NAME, b.l("Menu.021"));
		}
		public void actionPerformed(ActionEvent e) {
			
			Properties p = new Properties();
			try {
				p.load(new FileInputStream(Config.cfg));
			} catch (Exception e2) {
				//e2.printStackTrace(b.tty());
			}
			
			JTextField proxy = new JTextField(15);
			if (p.containsKey("proxy.server")) proxy.setText(p.getProperty("proxy.server"));
			JTextField port = new JTextField(5);
			if (p.containsKey("proxy.port")) port.setText(p.getProperty("proxy.port"));
			
			JPanel oo = new JPanel(new MigLayout());
			oo.add(new JLabel("HTTP: "));
			oo.add(proxy);
			oo.add(new JLabel("Port: "));
			oo.add(port);
			
			JOptionPane.showMessageDialog(b.getUI().getC(), oo, 
					"Proxyservereinstellungen", JOptionPane.QUESTION_MESSAGE);
			
			if (proxy.getText().length() < 1 || port.getText().length() < 1) {
				return;
			} else {
				p.put("proxy.server", proxy.getText());
				p.put("proxy.port", port.getText());
				try {
					p.store(new FileOutputStream(Config.cfg), new Date().toString());
				} catch (Exception e2) {
					//e2.printStackTrace(b.tty());
				}
			}
		}
	}
	
}
