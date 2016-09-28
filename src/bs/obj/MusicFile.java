package bs.obj;

import java.util.Date;

import bs.Bs;
import bs.Config;
import bs.Ean13Util;

public class MusicFile implements DBOEntry {
	
	public static final int GENDER_MALE = 0x02;
	public static final int GENDER_FEMALE = 0x03;
	public static final int GENDER_UNKNOWN = 0x00;
	
	public static final int END_COLD_END = 0x06;
	public static final int END_QUICK_FADE = 0x07;
	public static final int END_FADE = 0x08;
	public static final int END_UNKNOWN = 0x00;
	
	// basic
	private String name = null;
	private String interpret = null;
	private String rampTimes = null;
	private int gender = 0;
	private int duration = 0;
	private int end = 0;
	private String description = null;
	private int flag = DBOEntry.STATE_OKAY;
	private long creationDate = 0;
	private long orderId;
	
	// barcode management
	private long id = 0;
	private String barcode = ""; // needed by the search
	
	
	public MusicFile() {
		// created.
	}
	
	// get methods ////////////////////////////////////////////////
	
	public String getName() {
		return this.name;
	}
	
	public String getInterpret() {
		return this.interpret;
	}
	
	public String getRampTimes() {
		return this.rampTimes;
	}
	
	public int getGender() {
		return this.gender;
	}
	
	public int getDuration() {
		return this.duration;
	}
	
	public int getEndType() {
		return this.end;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public int getState() {
		return this.flag;
	}
	
	public String getId() {
		if (this.barcode == null) return "0000000000000";
		if (this.barcode.length() != 13 &&
				this.barcode.length() != 12) return "0000000000000";
		return this.barcode;
	}
	
	public String getCreatedOn() {
		try {
			Date d = new Date(this.creationDate);
			return Bs.formatDate(d);
		} catch (Exception e) { /* don't watch */ }
		return Config.DATE_UNKNOWN;
	}
	
	// set methods ////////////////////////////////////////////////
	
	public void setState(int f) {
		this.flag = f;
	}
	
	public void setName(String n) {
		this.name = n;
	}
	
	public void setInterpret(String i) {
		this.interpret = i;
	}
	
	public void setRampTimes(String r) {
		this.rampTimes = r;
	}
	
	public void setGender(int g) {
		if (g != GENDER_FEMALE && g != GENDER_MALE && g != GENDER_UNKNOWN) return;
		this.gender = g;
	}
	
	public void setDuration(int d) {
		if (d < 1) {
			this.duration = 0;
			return;
		}
		this.duration = d;
	}
	
	public void setEndType(int e) {
		if (e != END_COLD_END && e != END_FADE && e != END_QUICK_FADE &&
				e != END_UNKNOWN) return;
		this.end = e;
	}
	
	public void setDescription(String d) {
		this.description = d;
	}
	
	public void setCreatedOn(long con) {
		this.creationDate = con;
	}
	
	// overridings ////////////////////////////////////////////////
	
	public boolean isFullFilled() {
		return (name.length() > 2 && interpret.length() > 2);
	}
	
	public String toString() {
		return "MusicFile" + barcode + "@" + name;
	}

	public void setId(long id) {
		this.id = id;
		this.createBarcode();
	}

	private void createBarcode() {
		int cnt = 0;
		int[] ean = new int[13];
		
		// get the id digits
		String base = Config.BARCODE_PREFIX_MUSICFILE;
		int runs = Config.BARCODE_PREFIX_MUSICFILE.length();
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
		
		System.out.println("(MusicFile) Barcode generated: " + this.barcode);
		return; 
	}

	public void update(DBOEntry e) {
		MusicFile copy = (MusicFile) e;
		
		// basic data
		name = copy.getName();
		interpret = copy.getInterpret();
		rampTimes = copy.getRampTimes();
		gender = copy.getGender();
		duration = copy.getDuration();
		end = copy.getEndType();
		description = copy.getDescription();
		flag = copy.getState();
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
		body.append("<b>" + b.l("ex.nfo.headline.cre") + ":</b> " + 
				getCreatedOn() + "<br/>");
		body.append("<b>" + b.l("mid.lbl.opt.ramp") + ":</b> " + 
				(rampTimes == null || rampTimes.length() < 1 ? "-" :
					rampTimes) + "<br/>");
		String gender = b.l("cdtd.lbl.gender.un");
		if (getGender() == Track.GENDER_MALE)
			gender = b.l("cdtd.lbl.gender.man");
		if (getGender() == Track.GENDER_FEMALE)
			gender = b.l("cdtd.lbl.gender.wom");
		body.append("<b>" + b.l("mid.lbl.opt.gender") + ":</b> " + 
				gender + "<br/>");
		body.append("<b>" + b.l("OrderBy.totalDuration") + ":</b> " + 
				Bs.formatAsTime(duration) + "<br/>");
		String end = b.l("cdtd.lbl.end.un");
		if (getEndType() == Track.END_COLD_END)
			end = b.l("cdtd.lbl.end.c");
		if (getEndType() == Track.END_FADE)
			end = b.l("cdtd.lbl.end.f");
		if (getEndType() == Track.END_QUICK_FADE)
			end = b.l("cdtd.lbl.end.q");
		body.append("<b>" + b.l("cdtd.lbl.gen.end") + ":</b> " + 
				end + "<br/>");
		body.append("<b>" + b.l("Title.003") + ": </b> " + 
				(description == null || description.length() < 1 ? "-" : 
					"<code>" + description + "</code>"));
		body.append("</p>");
		return body;
	}
	
}
