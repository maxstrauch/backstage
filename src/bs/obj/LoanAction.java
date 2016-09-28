package bs.obj;

import java.util.Date;

public class LoanAction {

	private String id;
	private String borrowedTo;
	private Date startDate;
	private Date endDate;
	private String desc;
	private String borrowedBy;
	
	public LoanAction(String by, String to, Date s, String d) {
		this.borrowedBy = by;
		this.borrowedTo = to;
		this.startDate = s;
		this.endDate = null;
		this.desc = d;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getBorrowedBy() {
		return this.borrowedBy;
	}
	
	public String getBorrowedTo() {
		return this.borrowedTo;
	}
	
	public Date getStartDate() {
		return this.startDate;
	}
	
	public Date getEndDate() {
		return this.endDate;
	}
	
	public String getDescription() {
		return this.desc;
	}
	
	public void setDescription(String d) {
		this.desc = d;
	}
	
	public boolean isReturned() {
		return !(this.endDate == null);
	}
	
	public void setEndDate(Date d) {
		this.endDate = d;
	}
	
	public void setId(String i) {
		this.id = i;
	}
	
}
