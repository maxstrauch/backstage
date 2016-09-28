package bs.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

import bs.Bs;
import bs.obj.CD;
import bs.obj.Inventory;
import bs.obj.MusicFile;

import com.lowagie.text.Font;

public class ItemListCellRender extends JPanel implements ListCellRenderer {

	private static final long serialVersionUID = 1L;

	private JLabel mainIcon;
	private JLabel title;
	private JLabel titleSeparator = new JLabel(", ");
	private JLabel line2;
	private JLabel end;
	private JLabel hired;

	private Bs b;
	
	public ItemListCellRender(Bs b) {
		this.b = b;
		this.setLayout(new MigLayout());
		mainIcon = new JLabel();
		mainIcon.setPreferredSize(new Dimension(16, 16));
		this.add(mainIcon, "align left");
		title = new JLabel();
		title.setFont(title.getFont().deriveFont(Font.BOLD));
		this.add(title, "gap unrel, align left");
		this.add(titleSeparator, "align left");
		line2 = new JLabel();
		line2.setFont(line2.getFont().deriveFont(Font.ITALIC));
		this.add(line2, "align left");
		hired = new JLabel();
		hired.setPreferredSize(new Dimension(16, 16));
		this.add(hired, "push, align right");
		end = new JLabel();
		this.add(end, "gap unrel");
	}
	
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		if (value == null) return this;
		
		if (value instanceof CD) {
			CD c = (CD) value;
			mainIcon.setIcon(UIIco.MEDIA_CD_16);
			if (c.getName() == null)
				titleSeparator.setText(null);
			else title.setText(c.getName());
			line2.setText(Bs.fill((int) c.getOrderId(), 5, "0") + 
					(c.getInterpret() == null ? "" : " (" + c.getInterpret() + ")"));
			end.setText(Bs.formatAsTime(c.getTotalDuration()));
			hired.setIcon((c.isBorrowed() ? UIIco.WEB_16 : null));
		} else if (value instanceof Inventory) {
			Inventory i = (Inventory) value;
			mainIcon.setIcon(UIIco.MEDIA_INV_16);
			if (i.getName() == null)
				titleSeparator.setText(null);
			else title.setText(i.getName());
			line2.setText(Bs.fill((int) i.getOrderId(), 5, "0") + 
					(i.getType() == null ? "" : " (" + i.getType() + ")"));
			end.setText(i.getPurchasedOn());
			hired.setIcon((i.isBorrowed() ? UIIco.WEB_16 : null));
		} else if (value instanceof MusicFile) {
			MusicFile mf = (MusicFile) value;
			mainIcon.setIcon(UIIco.MEDIA_MUS_16);
			if (mf.getName() == null)
				titleSeparator.setText(null);
			else title.setText(mf.getName());
			line2.setText(Bs.fill((int) mf.getOrderId(), 5, "0") +
					(mf.getInterpret() == null ? "" : " (" + mf.getInterpret() + ")"));
			end.setText(Bs.formatAsTime(mf.getDuration()));
			hired.setIcon(null);
		} else {
			mainIcon.setIcon(null);
			titleSeparator.setText(null);
			title.setText(b.l("Info.001"));
			line2.setText("");
			end.setText("");
			hired.setIcon(null);
			return this;
		}
		
		// manage the highlightening
		UIDefaults defaults = UIManager.getDefaults( );
		if (isSelected) {
			setBackground(defaults.getColor("List.selectionBackground"));
			setForeground(defaults.getColor("List.selectionForeground"));
			colorizeForeground(defaults.getColor("List.selectionForeground"), 
					titleSeparator, title, line2, end);
		} else {
			setBackground(defaults.getColor("List.background"));
			setForeground(defaults.getColor("List.foreground"));
			colorizeForeground(defaults.getColor("List.foreground"), 
					titleSeparator, title, line2, end);
		}
		
		// do the nimbus bug fix
		if (UIManager.getLookAndFeel().getName().equalsIgnoreCase("nimbus")) {
			// bug fix for: 'Invalid background of components displayed 
			// in JTable in Nimbus L&F'
			// id #6723524; see bugs.sun.com/bugdatabase/view_bug.do?bug_id=6723524
			setOpaque(true);
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				colorizeForeground(Color.white, 
						titleSeparator, title, line2, end);
			} else {
				if (index % 2 == 1) setBackground(Color.WHITE);
				else setBackground(new Color(0xf2f3f4));//list.getBackground());
			}
		}
		return this;
	}

	private void colorizeForeground(Color c, Component... cs) {
		for (Component comp : cs) comp.setForeground(c);
	}
	
}
