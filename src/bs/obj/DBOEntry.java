package bs.obj;

import bs.Bs;

public interface DBOEntry {
	
	public static final int STATE_OKAY = 0x01;
	public static final int STATE_DAMAGED = 0x02;
	public static final int STATE_LOST = 0x03;
	public static final int STATE_DESTROYED = 0x04;
	
	public void setId(long id);
	public void update(DBOEntry e);
	
	public void setOrderId(long id);
	public long getOrderId();
	
	public StringBuffer formatToXHTML(Bs b);
}
