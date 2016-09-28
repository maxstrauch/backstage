package bs;

import java.awt.Dimension;
import java.io.File;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

public class Config {
	
	public static final String editorPaneStyle = 
		"<style type=\"text/css\">" +
		"html, body {font-family: sans-serif;}" +
		"h5 {color: #002640;font-size: 1.25em;margin: 0;padding: 0;margin-top: 10px;}" +
		"b {color: #002640;}" +
		"p {margin: 0px;padding: 0px; margin-top: 5px;}" +
		"</style>";

	public static final StringBuffer STYLESHEET = new StringBuffer(
			"<style type=\"text/css\">" +
			"@page {size: 210mm 297mm; margin: 20mm" +
					"-fs-flow-top: \"header\";-fs-flow-bottom: \"footer\";" +
						"-fs-flow-left: \"left\";-fs-flow-right: \"right\";" +
						"padding: 1em;}" +
			".copyr {color: #DDDDDD;font-size:0.6em;margin-top:0.4em;width:100%;" +
					"border-top: 1px solid #DDDDDD;}" +
			"#pagenumber:before {content: counter(page);}" +
			"#pagecount:before {content: counter(pages);}" +
			"* {margin: 0; padding: 0;}" +
			"body {margin: 7px;font-family:sans-serif;}" +
			".firstHeading {font-size:1.6em;line-height:1.2em;margin-bottom:0.1em;" +
					"margin-top:0;padding-bottom:0;padding-top:0;border-bottom:1px solid #AAAAAA;" +
					"font-weight:normal;color:black;margin:0;width:auto;}" +
			".disabled {color: #888888;}" +
			"p {line-height:1.5em;margin:0.4em 0 0.5em;font-size:0.8em;color:black;}" +
			".section {font-size:0.95em;font-weight:bold;}" +
			"code {border: dotted 1px #AAAAAA;width:100%;}" +
			".second {font-size:1.4em;margin-top: 10px;}" +
			".normal {line-height:1.5em;margin:0.4em 0 0.5em;font-size:100%;color:black;}" +
			"table {margin:0.4em 0 0.5em;width: 100%;}" +
			"td {background-color: #EEEEEE;padding: 2px;}" +
			"</style>"
		);
		
	public static final String DB_EXTENSION = ".bsdb";
	
	public static final String LABEL_FORMATS = "label.cfg";
	
	public static final String DATE_UNKNOWN = "??.??.????";
	
	public static final String BARCODE_PREFIX_CD = "999";
	public static final String BARCODE_PREFIX_INVENTORY = "998";
	public static final String BARCODE_PREFIX_MUSICFILE = "997";
	
	public static final String APP_VERSION = "0.3 (15/11/10)";
	public static final String APP_IDENT = "backstage " + APP_VERSION;
	public static final String APP_IDENT_SUB = "(c) 2010 by Maximilian Strauch";
	
	public static final String FILE = "backstage";
	public static final String FILE_EXTENSION = ".db";
	
	public static File getJARLocation() {
		Class<?> mainClass = Config.class;
		ProtectionDomain pDomain = mainClass.getProtectionDomain();
		CodeSource cSource = pDomain.getCodeSource();
		URL loc = cSource.getLocation();
		try {
			File f = new File(loc.toURI());
			if (f.isFile()) f = f.getParentFile();
			return f;
		} catch (Exception e) {
			return new File(System.getProperty("user.home"));
		}
	}
	
	public static final File cfg = new File(getJARLocation() + 
			System.getProperty("file.separator") + "proxy.cfg");
	
	public static final Dimension editDialogSize = new Dimension(
			475, 350);
	
	public static final Dimension aboutDialogSize = new Dimension(
			400, 300);
	
}
