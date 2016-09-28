package bs.obj;


public class Track {
	
	public static final int GENDER_MALE = 0x02;
	public static final int GENDER_FEMALE = 0x03;
	public static final int GENDER_UNKNOWN = 0x00;
	
	public static final int END_COLD_END = 0x06;
	public static final int END_QUICK_FADE = 0x07;
	public static final int END_FADE = 0x08;
	public static final int END_UNKNOWN = 0x00;
	

	private long id = 0;
	private String name = null;
	private String interpret = null;
	private String rampTimes = null;
	private int gender = 0;
	private int duration = 0;
	private int end = 0;
	private String description = null;
	
	public Track() {
		this(null, null, null, 0, 0, 0, null);
	}
	
	public Track(String n, String i, String r, int g, int l, int e, String d) {
		this.name = n;
		this.interpret = i;
		this.rampTimes = r;
		this.setGender(g);
		this.setDuration(l);
		this.setEndType(e);
		this.description = d;
	}
	
	public long getId() {
		return this.id;
	}
	
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

	public void setId(long id) {
		this.id = id;
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
	
	public String toString() {
		return "Track@" + name + " (i=" + interpret + ", r=" + rampTimes + 
				", g=" + gender + ", e=" + end + ") " + duration + ": " +
				description;
	}
	
	public boolean isFullFilled() {
		return (name.length() > 2 && interpret.length() > 2 && 
				rampTimes.length() > 2 && duration > 0);
	}

	public String toListString() {
		return name;
	}
	
}
