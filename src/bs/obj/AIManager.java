package bs.obj;

public class AIManager {

	public long cdId;
	public long inventoryId;
	public long musicFileId;
	
	public AIManager() {
		this.cdId = 1;
		this.inventoryId = 1;
		this.musicFileId = 1;
	}
	
	public long getNextCDId() {
		long id = cdId;
		cdId++;
		return id;
	}
	
	public long getNextInventoryId() {
		long id = inventoryId;
		inventoryId++;
		return id;
	}
	
	public long getNextMusicFileId() {
		long id = musicFileId;
		musicFileId++;
		return id;
	}
	
}
