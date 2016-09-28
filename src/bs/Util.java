package bs;

import javax.swing.UIManager;

public class Util {

	public static boolean isNimbusLaf() {
		return UIManager.getLookAndFeel().getName().equalsIgnoreCase("nimbus");
	}
	
}
