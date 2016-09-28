package bs.obj;

import java.util.ArrayList;
import java.util.Date;

import bs.Bs;
import bs.Config;
import bs.Ean13Util;

public class Inventory implements DBOEntry {

	// basic	
	private String name;
	private String type;
	private String serialNumber;
	private String location;
	// alias for purchasedOn
	private long creationDate = 0; // needed by the search	
	private String description;
	private int flag = DBOEntry.STATE_OKAY;
	private long orderId;
	
	// loan management
	private ArrayList<LoanAction> loanActions;
	private long loanCnt = 0;
	private boolean isBorrowed = false;
	
	// barcode management
	private long id = 0;
	private String barcode = ""; // needed by the search
	
	public Inventory() {
		loanActions = new ArrayList<LoanAction>();
	}
	
	// get methods ////////////////////////////////////////////////
	
	public String getName() {
		return this.name;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String getSerialNumber() {
		return this.serialNumber;
	}
	
	public String getLocation() {
		return this.location;
	}
	
	public String getPurchasedOn() {
		try {
			Date d = new Date(this.creationDate);
			return Bs.formatDate(d);
		} catch (Exception e) { /* don't watch */ }
		return Config.DATE_UNKNOWN;
	}
	
	public long getPurchasedOnAsLong() {
		return this.creationDate;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public int getState() {
		return this.flag;
	}
	
	public boolean isBorrowed() {
		return this.isBorrowed;
	}
	
	public String getId() {
		if (this.barcode == null) return "0000000000000";
		if (this.barcode.length() != 13 &&
				this.barcode.length() != 12) return "0000000000000";
		return this.barcode;
	}
	
	// set methods ////////////////////////////////////////////////
	
	public void setName(String n) {
		this.name = n;
	}
	
	public void setType(String t) {
		this.type = t;
	}
	
	public void setSerialNumber(String sn) {
		this.serialNumber = sn;
	}
	
	public void setLocation(String l) {
		this.location = l;
	}
	
	public void setPurchasedOn(long pon) {
		this.creationDate = pon;
	}
	
	public void setDescription(String d) {
		this.description = d;
	}
	
	public void setState(int f) {
		this.flag = f;
	}
	
	// loan management ////////////////////////////////////////////
	
	public int getLoanCount() {
		return this.loanActions.size();
	}
	
	public LoanAction getLoanAction(int i) {
		if (i < 0 || i >= this.loanActions.size()) return null;
		return this.loanActions.get(i);
	}
	
	public void addLoanAction(LoanAction t) {
		t.setId(this.loanCnt+"");
		this.loanActions.add(t);
		this.loanCnt++;		
		this.isBorrowed = true;
	}
	
	public void clearOpenLoanAction() {
		LoanAction la = this.loanActions.get(loanActions.size()-1);
		if (la == null) return;
		loanActions.remove(la);
		this.isBorrowed = false;
		return;
	}
	
	public void closeOpenLoanAction(Date d, String t) {
		LoanAction la = this.loanActions.get(loanActions.size()-1);
		if (la == null) return;
		la.setEndDate(d);
		la.setDescription(t);
		this.isBorrowed = false;
		return;
	}
	
	// overridings ////////////////////////////////////////////////
	
	public String toString() {
		return "Inventory" + barcode + "@" + name;
	}
	
	public boolean isFullFilled() {
		return name.length() > 2;
	}

	public void setId(long id) {
		this.id = id;
		this.createBarcode();
	}

	private void createBarcode() {
		int cnt = 0;
		int[] ean = new int[13];
		
		// get the id digits
		String base = Config.BARCODE_PREFIX_INVENTORY;
		int runs = Config.BARCODE_PREFIX_INVENTORY.length();
		for (int i = 0; i < runs; i++) {
			String temp = base.substring(0, 1);
			base = base.substring(1);
			ean[cnt] = Integer.parseInt(temp);
			cnt++;
		}
		
		// write the id
		String id = String.valueOf(this.id);
		int idLength = id.length();
		
		// fill with zeros
		for (int i = 0; i < (ean.length-(1+idLength+runs)); i++) {
			ean[cnt] = 0;
			cnt++;
		}
		
		// insert id
		base = id;
		runs = idLength;
		for (int i = 0; i < runs; i++) {
			String temp = base.substring(0, 1);
			base = base.substring(1);
			ean[cnt] = Integer.parseInt(temp);
			cnt++;
		}
		
		// compute the check digit
		ean[cnt] = Ean13Util.checkDigit(ean);

		// set the barcode
		for (int i : ean)
			this.barcode += String.valueOf(i);
		
		System.out.println("(Inventory) Barcode generated: " + this.barcode);
		return; 
	}

	public void update(DBOEntry e) {
		Inventory copy = (Inventory) e;
		
		// basic data
		this.name = copy.getName();
		this.type = copy.getType();
		this.serialNumber = copy.getSerialNumber();
		this.location = copy.getLocation();
		this.creationDate = copy.getPurchasedOnAsLong();
		this.description = copy.getDescription();
		this.flag = copy.getState();
		return;
	}

	public void setOrderId(long id) {
		this.orderId = id;
	}

	public long getOrderId() {
		return this.orderId;
	}

	public StringBuffer formatToXHTML(Bs b) {
		StringBuffer body = new StringBuffer();
		body.append("<h1 class=\"firstHeading\">" + name + "</h1>");
		body.append("<p>" + Bs.fill((int) orderId, 5, "0") + " (" +
				barcode + ")<br/>");
		body.append("<b>" + b.l("iid.lbl.gen.type") + ":</b> " + 
				(type == null || type.length() < 1 ? "-" : type) + "<br/>");
		body.append("<b>" + b.l("iid.lbl.gen.serial") + ":</b> " + 
				(serialNumber == null || serialNumber.length() < 1 ? "-" : 
					serialNumber) + "<br/>");
		body.append("<b>" + b.l("iid.lbl.gen.loc") + ":</b> " + 
				(location == null || location.length() < 1 ? "-" : location) + "<br/>");
		body.append("<b>" + b.l("iid.lbl.gen.purch") + ":</b> " + 
				getPurchasedOn() + "<br/>");
		String status = b.l("cdid.lbl.gen.status.un");
		if (getState() == DBOEntry.STATE_OKAY)
			status = b.l("cdid.lbl.gen.status.ok");
		if (getState() == DBOEntry.STATE_DAMAGED)	
			status = b.l("cdid.lbl.gen.status.dam");
		if (getState() == DBOEntry.STATE_DESTROYED)
			status = b.l("cdid.lbl.gen.status.des");		
		if (getState() == DBOEntry.STATE_LOST)
			status = b.l("cdid.lbl.gen.status.los");
		body.append("<b>" + b.l("bsui.cpt.tab.loan") + ":</b> " + 
				status + "<br/>");
		body.append("<b>" + b.l("Title.003") + ": </b> " + 
				(description == null || description.length() < 1 ? "-" : 
					"<code>" + description + "</code>"));
		body.append("</p>");
		body.append("<h1 class=\"firstHeading second\">" + b.l("Title.005") + "</h1>");
		if (loanActions.size() < 1) {
			body.append("<p>" + b.l("Info.008") + "</p>");
		} else {
			body.append("<p><table width=\"100%\">");
			body.append("<tr>");
			body.append("<td></td>");
			body.append("<td><b>" + b.l("cdid.lbl.gen.loan.tbl.usrby") + "</b></td>");
			body.append("<td><b>" + b.l("cdid.lbl.gen.loan.tbl.usr") + "</b></td>");
			body.append("<td><b>" + b.l("cdid.lbl.gen.loan.tbl.sdat") + "</b></td>");
			body.append("<td><b>" + b.l("cdid.lbl.gen.loan.tbl.endat") + "</b></td>");
			body.append("<td><b>" + b.l("cdid.lbl.gen.loan.tbl.comm") + "</b></td>");
			body.append("</tr>");
			for (int i = 0; i < loanActions.size(); i++) {
				body.append("<tr>");
				body.append("<td>" + (i+1) + "</td>");
				body.append("<td>" + loanActions.get(i).getBorrowedBy() + "</td>");
				body.append("<td>" + loanActions.get(i).getBorrowedTo() + "</td>");
				body.append("<td>" + Bs.formatDate(loanActions.get(i).getStartDate()) + "</td>");
				body.append("<td>" + (loanActions.get(i).getEndDate() == null ? 
						"-" : Bs.formatDate(loanActions.get(i).getEndDate())) + "</td>");
				body.append("<td>" + (loanActions.get(i).getDescription() == null ?
						"-" : loanActions.get(i).getDescription()) + "</td>");
				body.append("</tr>");
			}
			body.append("</table></p>");
		}
		return body;
	}
	
}
