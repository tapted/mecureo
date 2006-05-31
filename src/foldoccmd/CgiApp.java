/*
 * Created on 10-Oct-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package foldoccmd;

import java.io.PrintStream;

/**
 * @author tapted
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class CgiApp {
	public static final String GHTML_TAIL = "</pre></body></html>";
	public static final String GHTML_TITLE = "Java CGI App";
	public String html_title() {return GHTML_TITLE; }
	
	protected PrintStream of; 
	
	public String html_head() {return
		 "<html><head><title>\n  " + html_title() +
		 "\n</title></head><body>\n <h1>\n" + html_title() +
		 "\n </h1><pre>\n";
	}
		
	public void print(Object o) {
		print(o.toString());
	}
	
	public void println(Object o) {
		println(o.toString());
	}
	
	public void print(String s) {
		if (of != null) {
			of.print(s);
			of.flush();
		}
	}

	public void println(String s) {
		if (of != null) {
			of.println(s);
			of.flush();
		}
	}
	
	public String html_tail() {return GHTML_TAIL;}	
	public String err_usage() {return "";}

	protected hallcgi.ShowParse cgi;
	public abstract void doMain() throws Exception;
	
	public CgiApp() {
		this(new hallcgi.ShowParse());
	}

	public CgiApp(hallcgi.ShowParse cgi) {
		this(cgi, System.out);
	}

	public CgiApp(hallcgi.ShowParse cgi, PrintStream of) {
		this.cgi = cgi;
		this.of = of;
	}
	
	public void cgiMain() {
		if (cgi.isChecked("cgiapp_quiet"))
			of = System.err;
		if (cgi.isChecked("cgiapp_silent"))
			of = null;
		
        try {
        	println(html_head());
        	doMain();
        } catch (Exception ioe) {
            println(ioe);
            ioe.printStackTrace(of);
        } finally {
            println(html_tail());
        }
		
	}
}
