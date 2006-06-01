package hallcgi;
//import java.io.*;
import java.util.*;

// This appears in Core Web Programming from
// Prentice Hall Publishers, and may be freely used
// or adapted. 1997 Marty Hall, hall@apl.jhu.edu.

public class ShowParse extends CgiShow {
	String[] names;
	String[][] values;
	HashMap valmap;
	
	public void parse() {
	    QueryStringParser parser = new QueryStringParser(args[0]);
	    LookupTable table = parser.parse();
	    names = table.getNames();
	    String[] firstvalues = table.getValues();
	    values = new String[firstvalues.length][];
	    String n, v;
	    String[] fullValue;
	    for(int i=0; i<names.length; i++) {
	        if (table.numValues(names[i]) > 1) {
	      	    values[i] = table.getFullValue(names[i]);
	        } else {
	            values[i] = new String[1];
	            values[i][0] = firstvalues[i];
	        }
      	    valmap.put(names[i], values[i]);
	    }
	}
	
	public ShowParse () {
	    this("ShowParse");
	}

	public ShowParse (String name) {
	    super(name);
		valmap = new HashMap();
	    parse();
	}
	
	public String getValue(String key) {		
		return getValues(key)[0];
	}
	
	public String getValue(String key, String defaultVal) {
		if (valmap.containsKey(key))
			return getValue(key);
		else
			return defaultVal;
	}
	
	public int getValue(String key, int defaultVal) {
		if (valmap.containsKey(key)) {
			try {
				return Integer.parseInt(getValue(key));
			} catch (NumberFormatException nfe) {				
			}
		}
		return defaultVal;
	}

	public double getValue(String key, double defaultVal) {
		if (valmap.containsKey(key)) {
			try {
				return Double.parseDouble(getValue(key));
			} catch (NumberFormatException nfe) {				
			}
		}
		return defaultVal;
	}

	public java.util.Iterator getIterator(String key) {
		if (!valmap.containsKey(key)) {
			//System.err.println("No mapping for '" + key + "' in " + valmap.toString());
			return (new ArrayList(0)).iterator();
		}
		return Arrays.asList(getValues(key)).iterator();
	}
	
	public boolean isChecked(String key) {
		return valmap.containsKey(key);
	}
	
	public String[] getValues(String key) {
		return (String[])valmap.get(key);
	}
	
	public String[] getNames() {
		return names;	
	}
	
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Iterator it = Arrays.asList(names).iterator(); it.hasNext(); ) {
			String key = (String)it.next();
			sb.append("\n" + key + ": ");
			for (Iterator jt = getIterator(key); jt.hasNext(); ) {
				sb.append((String)jt.next() + " - ");
			}
		}
		return sb.toString();		
	}
	
  public static void main(String[] args) {
    ShowParse app = new ShowParse("ShowParse");
    app.printFile();
  }

  public ShowParse(String name, String[] queryData,
                   String requestMethod) {
    super(name, queryData, requestMethod);
    parse();
  }
  
  protected void printBody(String[] queryData) {
    System.out.println("Request method:  <CODE>" +
                       type + "</CODE>.<BR>");
    if (names.length > 0)
      System.out.println("Data supplied:\n" +
                         "<CENTER>\n" +
                         "<TABLE BORDER=1>\n" +
                         "  <TR><TH>Name<TH>Value(s)");
    else
      System.out.println("<H2>No data supplied.</H2>");
    
    String name, value;
    String[] fullValue;
    for(int i=0; i<names.length; i++) {
      name = names[i];
      System.out.println("  <TR><TD>" + name);
      if (values[i].length > 1) {
        fullValue = values[i];
        System.out.println
          ("      <TD>Multiple values supplied:\n" +
           "          <UL>");
        for(int j=0; j<values[i].length; j++)
          System.out.println("            <LI>" +
                             fullValue[j]);
        System.out.println("          </UL>");
      } else {
        value = values[i][0];
        if (value.equals(""))
          System.out.println
            ("      <TD><I>No Value Supplied</I>");
        else
          System.out.println
            ("      <TD>" + value);
      }
    }
    System.out.println("</TABLE>\n</CENTER>");
  }

  protected void printStyleRules() {
    super.printStyleRules();
    System.out.println
      ("TH { background: black;\n" +
       "     color: white }\n" +
       "UL { margin-top: -10pt }");
  }
}
