package ckling.text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tartarus.snowball.SnowballStemmer;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.xerces.dom.CoreDocumentImpl;
import org.cyberneko.html.parsers.DOMFragmentParser;

//import ckling.db.Database;


public class Text {

	public String text;
	public boolean stopwords = true;
	public boolean stem = true;
	public String lang;

	private static Pattern[] p = new Pattern[2];

	private Pattern stopword = null;
	private SnowballStemmer stemmer = null;

//	private static Database db = new Database();

	public static void main(String[] args) {
		System.out.println("test");
	}
	
	public Text () {
	}
	public Text(String input) {
		setText(input);
	}

	public void loadFile (String fileLocation) {

		try {

			String output = "";

			BufferedReader reader;
			reader = new BufferedReader (new FileReader(fileLocation));

			String line;
			boolean first = true;
			while ((line = reader.readLine()) != null) {

				if (first) first=false;
				else output+="\n";

				output += line;

			}

			setText(output);
			
			reader.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean loadUrl(String s, boolean save) throws Exception
	{

//		if (save) {
//			db.executeQuery("SELECT * FROM site WHERE url = '" + s +"'");	
//
//			if (db.rs.next()) {
//				text = db.rs.getString("html");
//				return false;
//			}
//		}

		StringBuffer buf;

		URL url = new URL(s);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setAllowUserInteraction(false);
		conn.setDoOutput(true);
		conn.addRequestProperty("User-Agent",
		"University of Koblenz-Landau Crawler");
		conn.connect();
		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
		{
			System.out.println(conn.getResponseMessage());
			text = null;
			return true;
		}

		int responseCode = conn.getResponseCode();

		int retryCounter = 0;
		while (responseCode != 200) {

			if (responseCode == 420) {

				//get, how many seconds we should wait
				int wait;
				if (conn.getHeaderField("Retry-After") != null) { 
					wait = Integer.valueOf(conn.getHeaderField("Retry-After"));
					if (wait >= 60*60) wait = 2*60*60;
				}
				else {
					wait = 5*60;
				}
				//GregorianCalendar now=new GregorianCalendar();	
				//System.out.println("Limit: " + count + ", time: " + format.format(now.getTime()) + " retry after: " + wait);
				Thread.sleep(1000*wait + 1000);
				//no success due to the rate limit


			}

			else if (responseCode == 502 || responseCode == 503 || responseCode == 500) {


				//System.out.println("Fail at " + count + ", time: " + format.format(now.getTime()));
				retryCounter++;
				//retry 1 times
				if (retryCounter >= 1) text = null;

			}
			if (responseCode == 403) {

				System.out.println(conn.getResponseCode());

			}


			//if result not modified or some other errors
			else {
				text = null;
				return true;
			}


		}


		BufferedReader br = new BufferedReader(new InputStreamReader(conn
				.getInputStream()));
		buf = new StringBuffer();

		char[] c = new char[50000];
		int numChars = br.read(c);
		while (numChars > 0)
		{
			buf.append(c, 0, numChars);
			numChars = br.read(c);
		}
		br.close();
		br = null;

		conn.disconnect();
		conn = null;

//		if (save) {
//			db.addValue("url", s);
//			db.addValue("html", buf.toString());
//			db.updateInto("INSERT", "site");
//		}

		text = buf.toString();
		return true;
	}


	public String getText() {	
		return text;		
	}


	public void setText(String input) {
		text = input;
	}

	public void setStopwords(boolean input) {
		stopwords = input;		
	}
	public void setStem(boolean input) {
		stem = input;		
	}

	public void write (String dest) {

		try {
			File outputFile = new File(dest); ; 
			FileWriter fw;
			fw = new FileWriter(outputFile);


			BufferedWriter bw = new BufferedWriter(fw); 

			bw.write(text);

			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	public void setLang(String input) {

		//filter out these
		p[0] = Pattern.compile("^([ \\t\\n\\x0B\\f\\r\"]+|(http|https|ftp)\\://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(:[a-zA-Z0-9]*)?/?([a-zA-Z0-9\\-\\._\\?\\,\\'/\\\\\\+&amp;%\\$#\\=~])*|[?!\\.,]+)");
		//keep normal words, smilies, numbers
		p[1] = Pattern.compile("^((:D|:-D|:P|:-P|:O|:-O|B\\)|B-\\)|:S|:-S|:X|:-X|XD|xD|X-D|X-\\)|8\\)|8-\\)|X\\(|X-\\(|:d|:-d|n8|w8|n1)|[0-9]([\\.:]?[0-9]+)?|[#@]?[_a-zA-ZÀ-ÖØ-öø-ž0-9']+|[‘‚¨\\\\’ ‘•0-9~®‹›*—´«»`@ł€¶ŧ←↓→øþæſðđŋħł»«¢„“”µ°!\"§$%&/()=?'_:;>¹²³¼½¬{\\[\\]}–…·|<,.\\-#+'\\^]+)");
		
		//p[0] = Pattern.compile("^[ \\t\\n\\x0B\\f\\r]+");
		//p[1] = Pattern.compile("^(http|https|ftp)\\://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(:[a-zA-Z0-9]*)?/?([a-zA-Z0-9\\-\\._\\?\\,\\'/\\\\\\+&amp;%\\$#\\=~])*");
		//p[2] = Pattern.compile("^[?!.]+");
		//p[3] = Pattern.compile("^(:D|:-D|:P|:-P|:O|:-O|B\\)|B-\\)|:S|:-S|:X|:-X|XD|xD|X-D|X-\\)|8\\)|8-\\)|X\\(|X-\\(|:d|:-d|n8|w8|n1)");
		//p[4] = Pattern.compile("^[0-9]([\\.:]?[0-9]+)?");
		//p[5] = Pattern.compile("^[#@]?[_a-zA-ZÀ-ÖØ-öø-ž0-9']+");
		//p[6] = Pattern.compile("^[‘‚¨\\\\’ ‘•0-9~®‹›*—´«»`@ł€¶ŧ←↓→øþæſðđŋħł»«¢„“”µ°!\"§$%&/()=?'_:;>¹²³¼½¬{\\[\\]}–…·|<,.\\-#+'\\^]+");

		lang = input;		
		if (lang.equals("de")) {
			stopword = Pattern.compile("^(aber|alle|allem|allen|aller|alles|als|also|am|an|ander|andere|anderem|anderen|anderer|anderes|anderm|andern|anderr|anders|auch|auf|aus|bei|bin|bis|bist|da|damit|dann|der|den|des|dem|die|das|dass|daß|derselbe|derselben|denselben|desselben|demselben|dieselbe|dieselben|dasselbe|dazu|dein|deine|deinem|deinen|deiner|deines|denn|derer|dessen|dich|dir|du|dies|diese|diesem|diesen|dieser|dieses|doch|dort|durch|ein|eine|einem|einen|einer|eines|einig|einige|einigem|einigen|einiger|einiges|einmal|er|ihn|ihm|es|etwas|euer|eure|eurem|euren|eurer|eures|für|gegen|gewesen|hab|habe|haben|hat|hatte|hatten|hier|hin|hinter|ich|mich|mir|ihr|ihre|ihrem|ihren|ihrer|ihres|euch|im|in|indem|ins|ist|jede|jedem|jeden|jeder|jedes|jene|jenem|jenen|jener|jenes|jetzt|kann|kein|keine|keinem|keinen|keiner|keines|können|könnte|machen|man|manche|manchem|manchen|mancher|manches|mein|meine|meinem|meinen|meiner|meines|mit|muss|musste|nach|nicht|nichts|noch|nun|nur|ob|oder|ohne|sehr|sein|seine|seinem|seinen|seiner|seines|selbst|sich|sie|ihnen|sind|so|solche|solchem|solchen|solcher|solches|soll|sollte|sondern|sonst|über|um|und|uns|unse|unsem|unsen|unser|unses|unter|viel|vom|von|vor|während|war|waren|warst|was|weg|weil|weiter|welche|welchem|welchen|welcher|welches|wenn|werde|werden|wie|wieder|will|wir|wird|wirst|wo|wollen|wollte|würde|würden|zu|zum|zur|zwar|zwischen)$");
			stemmer = new org.tartarus.snowball.ext.germanStemmer();
		}
		else if (lang.equals("en")) {
			stopword = Pattern.compile("^(i|me|my|myself|we|us|our|ours|ourselves|you|your|yours|yourself|yourselves|he|him|his|himself|she|her|hers|herself|it|its|itself|they|them|their|theirs|themselves|what|which|who|whom|this|that|these|those|am|is|are|was|were|be|been|being|have|has|had|having|do|does|did|doing|would|could|should|ought|might|however|will|would|shall|should|can|could|may|might|must|ought|i'm|you're|he's|she's|it's|we're|they're|i've|you've|we've|they've|i'd|you'd|he'd|she'd|we'd|they'd|i'll|you'll|he'll|she'll|we'll|they'll|isn't|aren't|wasn't|weren't|hasn't|haven't|hadn't|doesn't|don't|didn't|won't|wouldn't|shan't|shouldn't|can't|cannot|couldn't|mustn't|let's|that's|who's|what's|here's|there's|when's|where's|why's|how's|daren't|needn't|oughtn't|mightn't|a|an|the|and|but|if|or|because|as|until|while|of|at|by|for|with|about|against|between|into|through|during|before|after|above|below|to|from|up|down|in|out|on|off|over|under|again|further|then|once|here|there|when|where|why|how|all|any|both|each|few|more|most|other|some|such|no|nor|not|only|own|same|so|than|too|very)$");
			stemmer = new org.tartarus.snowball.ext.englishStemmer();
		}
		else if (lang.equals("du")) {
			stopword = Pattern.compile("");
			stemmer = new org.tartarus.snowball.ext.dutchStemmer();
		}
		else {
			stopword = null;
			//english als standard
			stemmer = new org.tartarus.snowball.ext.englishStemmer();
		}
	}

	public Iterator<String> getTerms() {

		if (lang == null) setLang("en");

		ArrayList<String> words = new ArrayList<String>();

		/*
		if (terms.containsKey("test"))
			System.out.println(terms.get("test"));
		else {
			terms.put("test", 1);
		}

		if (terms.containsKey("test"))
			System.out.println(terms.get("test"));
		else {
			terms.put("test", 1);
		}

		if (1==1)	
		return;
		 */

		String content = text;
		String word = "";
		Matcher matcher;
		MatchResult matchResult = null;
		int i = 0;


		//System.out.println(db.rs.getString("content"));


		while (content.length() > 0) {

			for (i = 0; i < p.length;i++) {

				matcher = p[i].matcher(content);

				if (matcher.find()) {

					matchResult = matcher.toMatchResult();
					word = matchResult.group();
					content = content.substring(matchResult.end());
					word = word.toLowerCase();

					break;
				}

			}
			
			if (i < p.length) {

				if (i >= 1) {
					
					if (	! stopwords || 
							! stopword.matcher(word).matches()) {

						if (stem) {
							stemmer.setCurrent(word);
							stemmer.stem();
							word = stemmer.getCurrent();
						}

						if (word.length() > 32) { word = word.substring(0,32); }

						words.add(word);

					}
				}

			}
			else {
				content = content.substring(1);
			}

		}

		//System.out.println();

		Iterator<String> output = words.iterator();

		return output;
	}
	
	public DocumentFragment getXML () throws SAXException, IOException {
	
		DOMFragmentParser parser = new DOMFragmentParser();
		CoreDocumentImpl codeDoc = new CoreDocumentImpl();
		DocumentFragment doc = codeDoc.createDocumentFragment();

		InputSource source = new InputSource(new StringReader(text));
		parser.parse(source, doc);

		return doc;
		
	}
	
	

}
