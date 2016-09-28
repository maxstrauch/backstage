package bs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bs.obj.Track;
import java.util.Arrays;

// 8712155090776
// 4014513000019
// 094635323726
// B000FQIT16
public class AmazonQuery {

	public String title = null;
	public String url = null;
	public String interpret = null;
	public String description = null;
	public ArrayList<Track> tracks;

	private int rating;

	public AmazonQuery() {
		tracks = new ArrayList<Track>();
		rating = -1;
	}

	public void doRating() {
		rating = 0;
		if (title != null)
			rating++;
		if (url != null)
			rating++;
		if (interpret != null)
			rating++;
		if (description != null)
			rating++;
		if (tracks.size() > 0)
			rating++;
		return;
	}

	public int getRating() {
		return this.rating;
	}

	public String toString() {
		String toString = (rating < 0 ? "" : "[" + rating + "/5] ") + title;
		if (interpret != null && interpret.length() > 1)
			toString += " (" + interpret + ")";
		toString += " - "
				+ (tracks.size() == 1 ? "1 Track" : tracks.size() + " Tracks");
		return toString;
	}

	public static String proxyAddr = null;
	public static int proxyPort = -1;

	public static final String URL = "https://www.amazon.de/s/ref=nb_sb_noss?field-keywords=";

	private static final Pattern productString = Pattern
			.compile("<a.*?s-access-detail-page.*?href=\"(.*?)\">");
	private static final Pattern productLink = Pattern
			.compile("href=\"(.*?)\"");
	private static final Pattern productTitle = Pattern
			.compile("<span[^>]+productTitle[^>]+>(.*?)<[/]{0,1}span[^>]+>");
	private static final Pattern productOther = Pattern
			.compile("</h2>[^<]+<div[^>]+content[^>]+><ul><li>(.*?)<a[^>]+");
	private static final Pattern trackList = Pattern
	.compile("<dic[^>]+a-row[^>]+>(.*?)</div>");
	private static final Pattern trackListAlt = Pattern
	.compile("<tr[^>]+(listRowEven|listRowOdd)[^>]+>(.*?)</td>");

	
	public static AmazonQuery[] retrieve(String term) {
		ArrayList<AmazonQuery> products = new ArrayList<AmazonQuery>();

		// create the url
		String urlFrom = null;
		try {
			urlFrom = URL + URLEncoder.encode(term, "UTF-8");
		} catch (Exception e) {
			return null;
		}

		// download the content of the product site and
		String s = AmazonQuery.load(urlFrom);
                
		if (s == null) return null;
		// replace all line breaks
		//s = s.replaceAll("\r", "").replaceAll("\n", "");
                
		// look for product results and
		Matcher m = productString.matcher(s);
		while (m.find()) {
                    
			// loop through them so that they can be scanned
			String productHtml = m.group();
			String productUrl = null;
			AmazonQuery aq = new AmazonQuery();

			// get the link to the product's site
//			Matcher link = productLink.matcher(productHtml);
//			if (link.find()) {
//				productUrl = link.group().replace("href=\"", "");
//				productUrl = productUrl.substring(0, productUrl.length() - 1);
//			}
                        productUrl = m.group(1);

			// get the html of the product's site to extract
			// the desired information
			String p = AmazonQuery.load(productUrl);
                        
			if (p != null) {
				// replace all line breaks
				p = p.replaceAll("\r", "").replaceAll("\n", "");

				aq.url = productUrl;

				// -------------------------------------------------

				Matcher titleMatcher = productTitle.matcher(p);
				if (titleMatcher.find()) {
					aq.title = titleMatcher.group();
					aq.title = aq.title.replaceAll("<[^>]+>", "");
					aq.title = aq.title.replaceAll("[ ]{2,}", " ");
				}
                                if (aq.title != null) {
                                    aq.title = aq.title.trim();
                                }
                                
				Matcher otherMatcher = productOther.matcher(p);
				if (otherMatcher.find()) {
					String temp = otherMatcher.group();
					aq.description = "";

					Matcher ms = Pattern.compile("<li>(.*?)</li>").matcher(temp);
					while (ms.find()) {
						String t = ms.group();
						t = t.replaceAll("</b>", ":").replaceAll("<[^>]+>", "")
								.replace("::", ":").replaceAll("  ", " ");

						aq.description += t + ", ";

					}

					if (aq.description != null && aq.description.length() > 0) {
						if (aq.description.endsWith(", ")) {
							aq.description = aq.description.substring(0,
									aq.description.length() - ", ".length());
						}
					}
				}

				Matcher trackListMatcher = trackList.matcher(p);
				while (trackListMatcher.find()) {
					Track t = new Track();
                                        
                                        System.out.println(">>" + trackListMatcher.group());

					String temp = trackListMatcher.group();

					Matcher tempMatcher = Pattern.compile(
							"<td[^>]+titleCol[^>]+>(.*?)</td>").matcher(temp);
					if (tempMatcher.find()) {
						t.setName(tempMatcher.group());
						t.setName(t.getName().replaceAll("(.*?)<a[^>]+>", ""));
						t.setName(t.getName().replaceAll("<[^>]+>", ""));

						if (tempMatcher.find()) {
							t.setInterpret(tempMatcher.group());
							t.setInterpret(t.getInterpret().replaceAll(
									"(.*?)<a[^>]+>", ""));
							t.setInterpret(t.getInterpret().replaceAll("<[^>]+>",
									""));

						}
					}

					tempMatcher = Pattern.compile("[0-9]{1,2}:[0-9]{1,2}").matcher(
							temp);
					if (tempMatcher.find()) {
						String sTime = tempMatcher.group();
						int min = Integer.parseInt(sTime.substring(0,
								sTime.indexOf(':')));
						int sec = Integer.parseInt(sTime.substring(sTime
								.indexOf(':') + 1));

						t.setDuration(sec + min * 60);
					}

					aq.tracks.add(t);
				}

				// interpret
				ArrayList<String> interpret = new ArrayList<String>();

				for (int i = 0; i < aq.tracks.size(); i++) {
					Track t = aq.tracks.get(i);
					if (t != null && t.getInterpret() != null) {
						String ipret = t.getInterpret();
						boolean exsists = false;

						for (int j = 0; j < interpret.size(); j++) {
							if (interpret.get(j).equals(ipret))
								exsists = true;
						}

						if (!exsists) {

							interpret.add(ipret);

						}

					}
				}

				
				if (aq.tracks.size() < 1) {
					
					Matcher trackListMatcherAlt = trackListAlt.matcher(p);
					while (trackListMatcherAlt.find()) {
						String temp = trackListMatcherAlt.group();
						temp = temp.replaceAll("<[^>]+>", "");
						temp = temp.substring(temp.indexOf(" ")+1);
						
						Track t = new Track();
						t.setName(temp);
						aq.tracks.add(t);
					}
					
					
					
					
					
					
					
					
				}
				
				
				
				
				
				
				
				
				
				
				
				String ipret = "";
				for (int i = 0; i < interpret.size(); i++) {
					ipret += interpret.get(i);
					if (i < interpret.size() - 1)
						ipret += ", ";
				}

				aq.interpret = ipret;

				aq.doRating();
				products.add(aq);
			}
		}
		return products.toArray(new AmazonQuery[products.size()]);
	}

	public static String load(String urlString) {
		InputStream is = null;
		String container = "";

		if (proxyAddr != null && proxyPort > 0) {
			System.setProperty("proxySet", "true");
			System.setProperty("proxyHost", proxyAddr);
			System.setProperty("proxyPort", String.valueOf(proxyPort));
		}

		try {
			URL url = new URL(urlString);
			is = url.openStream();
			container += new Scanner(is).useDelimiter("\\Z").next();
			return container;
		} catch (Exception e) {
			return null;
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}
	}

}
