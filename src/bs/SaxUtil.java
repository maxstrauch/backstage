package bs;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

public class SaxUtil {

	public static Element[] getElementsByName(Element root, String name) {
		ArrayList<Element> els = new ArrayList<Element>();
		
		getElements(root, name, els);
		
		Element[] e = new Element[els.size()];
		els.toArray(e);
		return e;
	}
	
	private static void getElements(Element root, String n, ArrayList<Element> e) {
		
		if (root.getName().equals(n)) {
			e.add(root);
		} else {
			List<?> children = root.getChildren();
			if (children.size() > 0) {
				for (int i = 0; i < children.size(); i++) {
					getElements((Element) children.get(i), n, e);
				}
			}
		}
	}
	
	public static Element getFirstElementByName(Element r, String n) {
		Element[] es = getElementsByName(r, n);
		if (es.length > 0) return es[0];
		return null;
	}
	
}
