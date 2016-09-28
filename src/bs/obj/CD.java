package bs.obj;

import java.util.ArrayList;
import java.util.Date;

import bs.Bs;
import bs.Config;
import bs.Ean13Util;

public class CD implements DBOEntry {
	
	// basic
	private String name = null;
	private String interpret = null;
	private String description = null;
	private int flag = DBOEntry.STATE_OKAY;
	private long creationDate = 0; // needed by the search	
	private int totalDuration = 0;
	private long orderId;
	
	// track management
	private ArrayList<Track> tracks = null;
	private long trackCnt = 0;
	
	// loan management
	private ArrayList<LoanAction> loanActions;
	private long loanCnt = 0;
	private boolean isBorrowed = false;
	
	// barcode management
	private long id = 0;
	private String barcode = ""; // needed by the search
	
	public CD() {
		this(null, null, null, 0);
	}
	
	public CD(String n, String i, String d, long con) {
		super();
		
		this.name = n;
		this.interpret = i;
		this.description = d;
		this.tracks = new ArrayList<Track>();
		this.creationDate = con;
		this.setState(DBOEntry.STATE_OKAY);
		loanActions = new ArrayList<LoanAction>();
	}
	
	// get methods ////////////////////////////////////////////////
	
	public String getName() {
		return this.name;
	}
	
	public String getInterpret() {
		if (this.interpret.length() < 1) return null;
		return this.interpret;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String getCreatedOn() {
		try {
			Date d = new Date(this.creationDate);
			return Bs.formatDate(d);
		} catch (Exception e) { /* don't watch */ }
		return Config.DATE_UNKNOWN;
	}
	
	public int getState() {
		return this.flag;
	}
	
	public boolean isBorrowed() {
		return this.isBorrowed;
	}

	public int getTotalDuration() {
		return this.totalDuration;
	}
	
	private void computeTotalDuration() {
		this.totalDuration = 0;
		for (int i = 0; i < tracks.size(); i++) {
			this.totalDuration += tracks.get(i).getDuration();
		}
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
	
	public void setInterpret(String i) {
		this.interpret = i;
	}
	
	public void setDescription(String d) {
		this.description = d;
	}
	
	public void setCreatedOn(long con) {
		this.creationDate = con;
	}
	
	public void setState(int f) {
		this.flag = f;
	}

	// track management ///////////////////////////////////////////
	
	public int getTrackCount() {
		return this.tracks.size();
	}
	
	public Track getTrack(int i) {
		if (i < 0 || i >= this.tracks.size()) return null;
		return this.tracks.get(i);
	}
	
	public void addTrack(Track t) {
		t.setId(this.trackCnt);
		this.tracks.add(t);
		this.trackCnt++;		
		this.computeTotalDuration();
	}
	
	// loan management ////////////////////////////////////////////
	
	public int getLoanCount() {
		return this.loanActions.size();
	}
	
	public void clearLoan() {
		this.loanActions.clear();
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
		return "CD" + barcode + "@" + name;
	}

	public void setId(long id) {
		this.id = id;
		this.createBarcode();
	}

	private void createBarcode() {
		int cnt = 0;
		int[] ean = new int[13];
		
		// get the id digits
		String base = Config.BARCODE_PREFIX_CD;
		int runs = Config.BARCODE_PREFIX_CD.length();
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
		
		//System.out.println("(CD) Barcode generated: " + this.barcode);
		return; 
	}

	public void update(DBOEntry e) {
		CD copy = (CD) e;
		
		// basic data
		this.name = copy.getName();
		this.interpret = copy.getInterpret();
		this.description = copy.getDescription();
		this.flag = copy.getState();
		
		// manage tracks
		Track[] t = new Track[copy.getTrackCount()];
		for (int i = 0; i < copy.getTrackCount(); i++)
			t[i] = copy.getTrack(i);
		this.tracks.clear();
		this.trackCnt = 0;
		for (Track track : t) this.addTrack(track);
		return;
	}

	public void setOrderId(long id) {
		this.orderId = id;
	}

	public long getOrderId() {
		return this.orderId;
	}
	
	public boolean isFullFilled() {
		return (name != null && name.length() > 2);
	}

	public StringBuffer formatToXHTML(Bs b) {
		StringBuffer body = new StringBuffer();
		body.append("<h1 class=\"firstHeading\">" + name + "</h1>");
		body.append("<p>" + Bs.fill((int) orderId, 5, "0") + " (" +
				barcode + ")<br/>");
		body.append("<b>" + b.l("bsui.search.full.int") + ":</b> " + 
				(interpret == null || interpret.length() < 1 ? "-" : interpret) + "<br/>");
		body.append("<b>" + b.l("cdid.lbl.gen.con") + ":</b> " +
				getCreatedOn() + "<br/>");
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
		body.append("<h1 class=\"firstHeading second\">" + b.l("Title.004") + "</h1>");
		if (tracks.size() < 1) {
			body.append("<p>" + b.l("Info.007") + "</p>");
		} else {
			body.append("<p><table width=\"100%\">");
			body.append("<tr>");
			body.append("<td></td>");
			body.append("<td><b>" + b.l("ex.nfo.headline.nm") + "</b></td>");
			body.append("<td><b>" + b.l("ex.nfo.headline.ipr") + "</b></td>");
			body.append("<td><b>" + b.l("ex.nfo.headline.ramp") + "</b></td>");
			body.append("<td><b>" + b.l("ex.nfo.headline.gen") + "</b></td>");
			body.append("<td><b>" + b.l("ex.nfo.headline.dur") + "</b></td>");
			body.append("<td><b>" + b.l("ex.nfo.headline.end") + "</b></td>");
			body.append("</tr>");
			for (int i = 0; i < tracks.size(); i++) {
				
				String gender = b.l("cdtd.lbl.gender.un");
				if (tracks.get(i).getGender() == Track.GENDER_MALE)
					gender = b.l("cdtd.lbl.gender.man");
				if (tracks.get(i).getGender() == Track.GENDER_FEMALE)
					gender = b.l("cdtd.lbl.gender.wom");
				String end = b.l("cdtd.lbl.end.un");
				if (tracks.get(i).getEndType() == Track.END_COLD_END)
					end = b.l("cdtd.lbl.end.c");
				if (tracks.get(i).getEndType() == Track.END_FADE)
					end = b.l("cdtd.lbl.end.f");
				if (tracks.get(i).getEndType() == Track.END_QUICK_FADE)
					end = b.l("cdtd.lbl.end.q");	
				
				body.append("<tr>");
				body.append("<td>" + (i+1) + "</td>");
				body.append("<td>" + tracks.get(i).getName() + "</td>");
				body.append("<td>" + (tracks.get(i).getInterpret() == null ? "-" :
					tracks.get(i).getInterpret()) + "</td>");
				body.append("<td>" + (tracks.get(i).getRampTimes() == null ? "-" :
					tracks.get(i).getRampTimes()) + "</td>");
				body.append("<td>" + gender + "</td>");
				body.append("<td>" + Bs.formatAsTime(tracks.get(i).getDuration()) + "</td>");
				body.append("<td>" + end + "</td>");
				body.append("</tr>");
			}
			body.append("</table></p>");
		}
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
