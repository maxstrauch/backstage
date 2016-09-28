package bs.obj;


public class Label {

	public int pageBorder;
	public int topBorder;
	public int width;
	public int height;
	public int labelsPerColumn;
	public int labelsPerRow;
	public int startLabel;
	
	public String toString() {
		return width + " mm x " +
			height + " mm (top: " + topBorder + " mm, page: " + pageBorder + 
			" mm, lpc: " + labelsPerColumn + " mm, lpr: " + labelsPerRow + " mm, sl: " +
			startLabel + " mm)";
	}
	
	public boolean eq(Label l) {
		return l.pageBorder == pageBorder && l.topBorder == topBorder &&
			l.width == width && l.height == height && l.labelsPerColumn ==
			labelsPerColumn && l.labelsPerRow == labelsPerRow && 
			l.startLabel == startLabel;
	}
	
}