package foldocui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

//my imports
import java.io.*;
import java.util.*;
import foldocparser.*;
import foldocparser.tools.*;
import hashgraph.*;

public class Interface extends JFrame {
    /**
	 * versioning for serailsation
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
    private JMenuBar jMenuBar1 = new JMenuBar();
    private JMenu jMenuFile = new JMenu();
    private JMenuItem jMenuFileExit = new JMenuItem();
    private JMenu jMenuHelp = new JMenu();
    private JMenuItem jMenuHelpAbout = new JMenuItem();
    private JToolBar jToolBar = new JToolBar();
    private JButton jbtnOpen = new JButton();
    private JButton jbtnSave = new JButton();
    private ImageIcon image1;
    private ImageIcon image2;
    private ImageIcon image3;
    private JLabel statusBar = new JLabel();
    private BorderLayout borderLayout1 = new BorderLayout();
    private TitledBorder titledBorder1;
    private JSplitPane jspOutput = new JSplitPane();
    private JButton jbtnSubgraph = new JButton();
    private JSplitPane jspCategories = new JSplitPane();
    private JButton jbtnMerge = new JButton();
    private JScrollPane jscrGraphs = new JScrollPane();
    private JScrollPane jscrCategories = new JScrollPane();
    private JList jlstGraphs = new JList();
    private JList jlstCategories = new JList();
    private JButton jbtnList = new JButton();
    private JTextField jtfQuery = new JTextField();
    private JButton jbtnRDF = new JButton();
    private JButton jbtnOWL = new JButton();
    private JButton jbtnDOT = new JButton();
	private JButton jbtnXML = new JButton();
	private JButton jbtnPeers = new JButton();
    private JMenuItem jMenuFileOpen = new JMenuItem();
    private JMenuItem jMenuFileSave = new JMenuItem();
    private JMenu jMenuOutput = new JMenu();
    private JMenuItem jMenuRDF = new JMenuItem();
	private JMenuItem jMenuOWL = new JMenuItem();
    private JMenuItem jMenuDOT = new JMenuItem();
	private JMenuItem jMenuXML = new JMenuItem();    
    private JMenuItem jMenuClustDOT = new JMenuItem();
    private JButton jbtnGenerate = new JButton();
    private JMenu jMenuInput = new JMenu();
    private JMenuItem jMenuInputGenerate = new JMenuItem();
    private JMenuItem jMenuInputKeyword = new JMenuItem();
    private JMenuItem jMenuInputCopy = new JMenuItem();
    private JSplitPane jspKeywords = new JSplitPane();
    private JScrollPane jscrKeywords = new JScrollPane();
    private JTextArea jtaOutput = new JTextArea();
    private JScrollPane jscrOutput = new JScrollPane();
    private JList jlstKeywords = new JList();

    /**Construct the frame*/
    public Interface() {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            jbInit();
            myInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    /**Component initialization*/
    private void jbInit() throws Exception  {
        image1 = new ImageIcon(foldocui.Interface.class.getResource("openFile.gif"));
        image2 = new ImageIcon(foldocui.Interface.class.getResource("closeFile.gif"));
        image3 = new ImageIcon(foldocui.Interface.class.getResource("help.gif"));
        //setIconImage(Toolkit.getDefaultToolkit().createImage(Interface.class.getResource("[Your Icon]")));
        contentPane = (JPanel) this.getContentPane();
        titledBorder1 = new TitledBorder("");
        contentPane.setLayout(borderLayout1);
        this.setSize(new Dimension(627, 446));
        this.setTitle("FOLDOC Ontology Querier");
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.setText(" ");
        jMenuFile.setText("File");
        jMenuFileExit.setText("Exit");
        jMenuFileExit.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                jMenuFileExit_actionPerformed(e);
            }
        });
        jMenuHelp.setText("Help");
        jMenuHelpAbout.setText("About");
        jMenuHelpAbout.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                jMenuHelpAbout_actionPerformed(e);
            }
        });
        jbtnOpen.setIcon(image1);
        jbtnOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jbtnOpen_actionPerformed(e);
            }
        });
        jbtnOpen.setMinimumSize(new Dimension(30, 27));
        jbtnOpen.setPreferredSize(new Dimension(30, 27));
        jbtnOpen.setToolTipText("Open graph");
        jbtnSave.setIcon(image2);
        jbtnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jbtnSave_actionPerformed(e);
            }
        });
        jbtnSave.setMinimumSize(new Dimension(30, 27));
        jbtnSave.setPreferredSize(new Dimension(30, 27));
        jbtnSave.setToolTipText("Save selected graph");
        jbtnSubgraph.setToolTipText("Generate a subgraph from a query using selected graph");
        jbtnSubgraph.setText("Subgraph");
        jbtnSubgraph.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jbtnSubgraph_actionPerformed(e);
            }
        });
        jbtnMerge.setToolTipText("Merge the slected graph with another");
        jbtnMerge.setText("Merge");
        jbtnMerge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jbtnMerge_actionPerformed(e);
            }
        });
        jspCategories.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jspCategories.setPreferredSize(new Dimension(50, 7));
        jbtnList.setToolTipText("List the nodes of the selected graph");
        jbtnList.setText("List");
        jbtnList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jbtnList_actionPerformed(e);
            }
        });
        jbtnRDF.setMinimumSize(new Dimension(59, 27));
        jbtnRDF.setToolTipText("Output the selected graph to RDF for VLUM");
        jbtnRDF.setText("RDF");
        jbtnRDF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jbtnRDF_actionPerformed(e);
            }
        });
		jbtnOWL.setMinimumSize(new Dimension(59, 27));
		jbtnOWL.setToolTipText("Output the selected graph to OWL Lite");
		jbtnOWL.setText("OWL");
		jbtnOWL.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jbtnOWL_actionPerformed(e);
			}
		});
        jbtnDOT.setToolTipText("Output the selected graph to dot for the DOT graph generator");
        jbtnDOT.setText("DOT");
        jbtnDOT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jbtnDOT_actionPerformed(e);
            }
        });
		jbtnXML.setToolTipText("Output the selected graph to XML");
		jbtnXML.setText("XML");
		jbtnXML.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jbtnXML_actionPerformed(e);
			}
		});
        jtfQuery.setMinimumSize(new Dimension(40, 21));
        jMenuFileOpen.setText("Open Graph From File...");
        jMenuFileOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jbtnOpen_actionPerformed(e);
            }
        });
        jMenuFileSave.setText("Save Selected Graph...");
        jMenuFileSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jbtnSave_actionPerformed(e);
            }
        });
        jMenuOutput.setText("Output");
        jMenuRDF.setText("Output to RDF...");
        jMenuRDF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jbtnRDF_actionPerformed(e);
            }
        });
		jMenuOWL.setText("Output to OWL...");
		jMenuOWL.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jbtnOWL_actionPerformed(e);
			}
		});
        jMenuDOT.setText("Output to DOT...");
        jMenuDOT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jbtnDOT_actionPerformed(e);
            }
        });
        jMenuClustDOT.setText("Output clustered DOT...");
        jMenuClustDOT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jMenuClustDOT_actionPerformed(e);
            }
        });
        jbtnGenerate.setToolTipText("Generate a model from text input using selected graph");
        jbtnGenerate.setText("Generate");
        jbtnGenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jbtnGenerate_actionPerformed(e);
            }
        });
        jMenuInput.setText("Input");
        jMenuInputGenerate.setText("Generate model...");
        jMenuInputGenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jbtnGenerate_actionPerformed(e);
            }
        });
        jMenuInputKeyword.setText("Keyword scan");
        jMenuInputKeyword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jMenuInputKeyword_actionPerformed(e);
            }
        });
        jMenuInputCopy.setActionCommand("Copy selected keywords");
        jMenuInputCopy.setText("Copy selected keywords");
        jMenuInputCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jMenuInputCopy_actionPerformed(e);
            }
        });
        jMenuOutputNorm.setText("Normalise now");
        jMenuOutputNorm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jMenuOutputNorm_actionPerformed(e);
            }
        });
        jMenuOutputList.setText("List graph");
        jMenuOutputList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jbtnList_actionPerformed(e);
            }
        });
        jMenuInputCLinks.setText("Copy	Links");
        jMenuInputCLinks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jMenuInputCLinks_actionPerformed(e);
            }
        });
        jMenuClone.setText("Clone...");
        jMenuClone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jMenuClone_actionPerformed(e);
            }
        });
        jMenuGrow.setText("Grow...");
        jMenuGrow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jMenuGrow_actionPerformed(e);
            }
        });
        jMenuDumpNames.setText("Dump node names (for agrep)...");
        jMenuDumpNames.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jMenuDumpNames_actionPerformed(e);
            }
        });
        jToolBar.add(jbtnOpen);
        jToolBar.add(jbtnSave);
        jToolBar.add(jbtnGenerate, null);
        jToolBar.add(jbtnSubgraph, null);
        jToolBar.add(jtfQuery, null);
        jToolBar.add(jbtnMerge, null);
        jToolBar.add(jbtnList, null);
        jToolBar.add(jbtnRDF, null);
		jToolBar.add(jbtnOWL, null);
        jToolBar.add(jbtnDOT, null);
		jToolBar.add(jbtnXML, null);
		jToolBar.add(jbtnPeers, null);
        jMenuHelp.add(jMenuHelpAbout);
        jMenuBar1.add(jMenuFile);
        jMenuBar1.add(jMenuInput);
        jMenuBar1.add(jMenuOutput);
        jMenuBar1.add(jMenuHelp);
        this.setJMenuBar(jMenuBar1);
        contentPane.add(jToolBar, BorderLayout.NORTH);
        contentPane.add(statusBar, BorderLayout.SOUTH);
        contentPane.add(jspOutput, BorderLayout.CENTER);
        jspOutput.add(jspCategories, JSplitPane.LEFT);
        jspCategories.add(jscrGraphs, JSplitPane.TOP);
        jscrGraphs.getViewport().add(jlstGraphs, null);
        jspCategories.add(jscrCategories, JSplitPane.BOTTOM);
        jspOutput.add(jspKeywords, JSplitPane.RIGHT);
        jspKeywords.add(jscrKeywords, JSplitPane.TOP);
        jscrKeywords.getViewport().add(jlstKeywords, null);
        jspKeywords.add(jscrOutput, JSplitPane.BOTTOM);
        jscrOutput.getViewport().add(jtaOutput, null);
        jscrCategories.getViewport().add(jlstCategories, null);
        jMenuFile.add(jMenuFileOpen);
        jMenuFile.add(jMenuFileSave);
        jMenuFile.addSeparator();
        jMenuFile.add(jMenuFileExit);
		jMenuOutput.add(jMenuRDF);
		jMenuOutput.add(jMenuOWL);
        jMenuOutput.add(jMenuDOT);
        jMenuOutput.add(jMenuClustDOT);
        jMenuOutput.addSeparator();
        jMenuOutput.add(jMenuClone);
        jMenuOutput.add(jMenuGrow);
        jMenuOutput.addSeparator();
        jMenuOutput.add(jMenuOutputNorm);
        jMenuOutput.add(jMenuOutputList);
        jMenuOutput.add(jMenuDumpNames);
        jMenuInput.add(jMenuInputGenerate);
        jMenuInput.addSeparator();
        jMenuInput.add(jMenuInputKeyword);
        jMenuInput.add(jMenuInputCopy);
        jMenuInput.add(jMenuInputCLinks);
        jspOutput.setDividerLocation(200);
        jspCategories.setDividerLocation(200);

        jlstGraphs.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                jlstGraphs_valueChanged(e);
            }
        });
        jlstCategories.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                jlstCategories_valueChanged(e);
            }
        });
        jspKeywords.setDividerLocation(0);

    }
    /**File | Exit action performed*/
    public void jMenuFileExit_actionPerformed(ActionEvent e) {
        System.exit(0);
    }
    /**Help | About action performed*/
    public void jMenuHelpAbout_actionPerformed(ActionEvent e) {
        Interface_AboutBox dlg = new Interface_AboutBox(this);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.show();
    }
    /**Overridden so we can exit when window is closed*/
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            jMenuFileExit_actionPerformed(null);
        }
    }

    public void addGraph(String key, HashGraph g) {
        graphs.put(key, g);
        statusBar.setText("Opening graph... Done");
        jlstGraphs.setListData(graphs.keySet().toArray());
    }

    void jbtnOpen_actionPerformed(ActionEvent e) {
        setMerge(false);
        jfc.setFileFilter(fdgFilter);
        jfc.showOpenDialog(this);
        if (jfc.getSelectedFile() != null && jfc.getSelectedFile().exists()) {
            try {
                statusBar.setText("Opening graph...");
                GraphLoader gl = new GraphLoader(this, "Loading Graph...", true);
                Point p = getLocationOnScreen();
                Point glp = new Point(getWidth() / 2 - gl.getWidth() / 2, getHeight() / 2 - gl.getHeight() / 2);
                glp.translate(p.x, p.y);
                gl.setLocation(glp);
                gl.openGraph(jfc.getSelectedFile(), this);
            } catch (Exception ee) {
                System.err.println(ee);
                ee.printStackTrace();
                statusBar.setText("Error loading graph. See stderr.");
            }
        }
//	System.out.println(graphs.keySet());
    }

    Vector categories = new Vector();
    HashMap graphs = new HashMap();
    HashGraph currentGraph = null;
    JOptionPane jop = new JOptionPane();
    JFileChooser jfc = new JFileChooser();
    boolean merging = false;
    boolean copying = false;
    boolean cluster = false;
    boolean growing = false;
    private ExtFilter fdgFilter = new ExtFilter(".fdg", "FOLDOC graphs (*.fdg)");
    private ExtFilter rdfFilter = new ExtFilter(".rdf", "RDF files (*.rdf)");
	private ExtFilter owlFilter = new ExtFilter(".owl", "OWL files (*.owl)");
	private ExtFilter xmlFilter = new ExtFilter(".xml", "XML files (*.xml)");
    private ExtFilter dotFilter = new ExtFilter(".dot", "Dot graphs (*.dot)");
    private ExtFilter allFilter = new ExtFilter("", "All Files (*.*)");
    private JMenuItem jMenuOutputNorm = new JMenuItem();
    private JMenuItem jMenuOutputList = new JMenuItem();
    private JMenuItem jMenuInputCLinks = new JMenuItem();
    private JMenuItem jMenuGrow = new JMenuItem();
    private JMenuItem jMenuClone = new JMenuItem();
    private JMenuItem jMenuDumpNames = new JMenuItem();
    //my code:
    private void loadCategories() throws IOException{
        statusBar.setText("Loading Categories...");
        BufferedReader catFile = new BufferedReader(new FileReader(Parser.CATEGORY_FILE));
        String s;
        while ((s = catFile.readLine()) != null) {
            categories.add(s);
        }
        jlstCategories.setListData(categories);
        statusBar.setText("Loading Categories... Done.");
    }

    private void saveGraph(HashGraph g, File file) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(
                                                        new FileOutputStream(file)));
        g.writeData(oos);
    }

    private void myInit() throws IOException {
        loadCategories();
    }

    void jbtnSubgraph_actionPerformed(ActionEvent e) {
        setMerge(false);
        if (currentGraph != null && jtfQuery.getText().length() > 0) {
            if (!currentGraph.contains(jtfQuery.getText())) {
                JOptionPane.showMessageDialog(this,
                		                      "That specified node does not exist in the selected graph",
											  "Error", 
											  JOptionPane.ERROR_MESSAGE);
                return;
            }
            String sg = JOptionPane.showInputDialog(this,
            		                                "Please specify a name for this subgraph",
													"Name",
													JOptionPane.PLAIN_MESSAGE);
            double dist;
            try {
                dist = Double.parseDouble
				         (JOptionPane.showInputDialog
				         		(this,
				         		 "Please enter a maximum distance",
								 "Distance",
								 JOptionPane.PLAIN_MESSAGE));
            } catch (NumberFormatException nfe) {
                jop.showMessageDialog(this, "You must enter a double", "Error", jop.ERROR_MESSAGE);
                return;
            }
            statusBar.setText("Generating subgraph... ");
            graphs.put(sg, currentGraph.subgraph(jtfQuery.getText(), dist));
            jlstGraphs.setListData(graphs.keySet().toArray());
            statusBar.setText("Generating subgraph... Done.");
        } else {
            jop.showMessageDialog(this, "You must select a graph and specify a node", "Error", jop.ERROR_MESSAGE);
        }
    }

    void jlstGraphs_valueChanged(ListSelectionEvent e) {
        if (merging) {
            setMerge(false);
            String sg = jop.showInputDialog(this, "Please specify a name for this merged graph", "Name", jop.PLAIN_MESSAGE);
            double infinity;
            try {
                infinity = Double.parseDouble(jop.showInputDialog(this, "Please enter a number for 'infinty'", "Infinity", jop.PLAIN_MESSAGE));
            } catch (NumberFormatException nfe) {
                jop.showMessageDialog(this, "You must enter a double", "Error", jop.ERROR_MESSAGE);
                return;
            }
            statusBar.setText("Merging graphs... ");
            graphs.put(sg, currentGraph.costMerge((HashGraph)graphs.get(jlstGraphs.getSelectedValue()), infinity));
            jlstGraphs.setListData(graphs.keySet().toArray());
            statusBar.setText("Merging graphs... Done.");
        } else if (copying) {
            setMerge(false);
            currentGraph.copyLinks((HashGraph)graphs.get(jlstGraphs.getSelectedValue()));
            statusBar.setText("Copied Links");
        } else if (growing) {
            growCurrent((HashGraph)graphs.get(jlstGraphs.getSelectedValue()));
        } else {
            currentGraph = (HashGraph)graphs.get(jlstGraphs.getSelectedValue());
            if (currentGraph != null)
                statusBar.setText("Active graph changed: Size = " + currentGraph.size() +
                                  "  Density = " + currentGraph.density());
        }
        setMerge(false);
    }

    void jlstCategories_valueChanged(ListSelectionEvent e) {
        if (noCurrent()) return;
        setMerge(false);
        ArrayList vals = new ArrayList();
        Node n = currentGraph.get(Parser.CATEGORY_PREPEND + jlstCategories.getSelectedValue());
        if (n == null) {
            jtaOutput.setText("");
            statusBar.setText("No nodes for selected category.");
            return;
        }
        statusBar.setText("Applying category filter...");
        for(Iterator it = n.values().iterator(); it.hasNext(); )
            vals.add(it.next());
        Collections.sort(vals, new LinkCostCompare());
        StringBuffer sb = new StringBuffer();
        for (Iterator it = vals.iterator(); it.hasNext(); ) {
            Link l = (Link)it.next();
            sb.append(l.not(n).toString());
            //sb.append(" : " + l.getWeight());
            sb.append('\n');
        }
        jtaOutput.setText(sb.toString());
        statusBar.setText("Applying category filter... Done.");
    }

    void jbtnList_actionPerformed(ActionEvent e) {
        if (noCurrent()) return;
        setMerge(false);
        if (currentGraph.size() < 1000 || jop.showConfirmDialog(this,
               "The selected graph is more than 1000 nodes, are you sure you wish to list them?",
               "Big Graph", jop.YES_NO_OPTION, jop.QUESTION_MESSAGE)
                   == JOptionPane.YES_OPTION) {
            listGraph(currentGraph);
        }
    }

    void listGraph(HashGraph g) {
        statusBar.setText("Listing nodes... ");
        //we want to sort the values based on their cost
        ArrayList vals = new ArrayList();
        for(Iterator it = g.nodeIterator(); it.hasNext(); )
            vals.add(it.next());
        Collections.sort(vals, new NodeCostCompare());
        StringBuffer sb = new StringBuffer();
        for (Iterator it = vals.iterator(); it.hasNext(); ) {
            sb.append(it.next().toString());
            sb.append('\n');
        }
        jtaOutput.setText(sb.toString());
        jtaOutput.setSelectionStart(0);
        jtaOutput.setSelectionEnd(0);
        statusBar.setText("Listing nodes... Done.");
    }

    void setMerge(boolean merge) {
        if (merging && !merge)
            statusBar.setText("Merge cancelled");
        else if (merge)
            statusBar.setText("Merge mode...");
        merging = merge;
        if (!merge)
            copying = false;
    }

    void jbtnMerge_actionPerformed(ActionEvent e) {
        if (currentGraph == null) {
            jop.showMessageDialog(this, "Please select an initial graph");
            setMerge(false);
        } else {
            jop.showMessageDialog(this, "Now select a graph to merge with");
            setMerge(true);
        }
    }

    void jbtnSave_actionPerformed(ActionEvent e) {
        if (noCurrent()) return;
        setMerge(false);
        jfc.setFileFilter(fdgFilter);
        jfc.showSaveDialog(this);
        if (jfc.getSelectedFile() != null) {
            try {
                statusBar.setText("Saving graph...");
                saveGraph(currentGraph, jfc.getSelectedFile());
                statusBar.setText("Saving graph... Done.");
            } catch (Exception ee) {
                System.err.println(ee);
                ee.printStackTrace();
                statusBar.setText("Error saving graph. See stderr.");
            }
        }
//	jlstGraphs.setListData(graphs.keySet().toArray());
//	System.out.println(graphs.keySet());
    }

	void jbtnXML_actionPerformed(ActionEvent e) {
		if (noCurrent()) return;
		if (currentGraph.size() > 999 && jop.showConfirmDialog(null,
							"Graph size is greater than 999. \nOutput anyway?",
							"Large Graph",
							jop.YES_NO_OPTION) == jop.NO_OPTION)
			return;
		jfc.setFileFilter(xmlFilter);
		jfc.showSaveDialog(this);
		if (jfc.getSelectedFile() != null) {
			PrintWriter rdf = null;
			try {
				statusBar.setText("Outputting graph to XML...");
				rdf = new PrintWriter(new BufferedWriter(
									  new FileWriter(jfc.getSelectedFile())));
				if (currentGraph.size() <= 999)
					OutputXML.outputXML(currentGraph, rdf, 3);
				else
					OutputXML.outputXML(currentGraph, rdf);
				statusBar.setText("Outputting graph to XML... Done.");
			} catch (Exception ee) {
				System.err.println(ee);
				ee.printStackTrace();
				statusBar.setText("Error outputting graph to XML. See stderr.");
			} finally {
				if (rdf != null)
					rdf.close();
			}
		}
	}

	void jbtnOWL_actionPerformed(ActionEvent e) {
		if (noCurrent()) return;
		if (currentGraph.size() > 999 && jop.showConfirmDialog(null,
							"Graph size is greater than 999. \nOutput anyway?",
							"Large Graph",
							jop.YES_NO_OPTION) == jop.NO_OPTION)
			return;
		jfc.setFileFilter(owlFilter);
		jfc.showSaveDialog(this);
		if (jfc.getSelectedFile() != null) {
			PrintWriter rdf = null;
			try {
				statusBar.setText("Outputting graph to OWL Lite...");
				rdf = new PrintWriter(new BufferedWriter(
									  new FileWriter(jfc.getSelectedFile())));
//				if (currentGraph.size() <= 999)
//					OutputOWL.outputOWL(currentGraph, rdf, 3);
//				else
//					OutputOWL.outputOWL(currentGraph, rdf);
				OutputOWL.outputOWL(currentGraph, rdf);
				statusBar.setText("Outputting graph to OWL Lite... Done.");
			} catch (Exception ee) {
				System.err.println(ee);
				ee.printStackTrace();
				statusBar.setText("Error outputting graph to OWL. See stderr.");
			} finally {
				if (rdf != null)
					rdf.close();
			}
		}
	}

    void jbtnRDF_actionPerformed(ActionEvent e) {
        if (noCurrent()) return;
        if (currentGraph.size() > 999 && jop.showConfirmDialog(null,
                            "Graph size is greater than 999. \nOutput anyway?",
                            "Large Graph",
                            jop.YES_NO_OPTION) == jop.NO_OPTION)
            return;
        jfc.setFileFilter(rdfFilter);
        jfc.showSaveDialog(this);
        if (jfc.getSelectedFile() != null) {
            PrintWriter rdf = null;
            try {
                statusBar.setText("Outputting graph to RDF...");
                rdf = new PrintWriter(new BufferedWriter(
                                      new FileWriter(jfc.getSelectedFile())));
                if (currentGraph.size() <= 999)
                    OutputRDF.outputRDF(currentGraph, rdf, 3);
                else
                    OutputRDF.outputRDF(currentGraph, rdf);
                statusBar.setText("Outputting graph to RDF... Done.");

                if (  jop.showConfirmDialog(null,
                      "Do you wish to generate html files in the same directory?",
                      "Generate HTML",
                      jop.YES_NO_OPTION) == jop.YES_OPTION) {
                    String rdfname = jfc.getSelectedFile().getName();
                    int extStart = rdfname.lastIndexOf('.');
                    String postfix = extStart > 0 ?
                                        rdfname.substring(0, extStart) :
                                        rdfname;
                    OutputRDF.makeHTMLSet(jfc.getSelectedFile().getParentFile().getAbsoluteFile(),
                                          rdfname,
                                          postfix,
                                          ((Node)currentGraph.nodeIterator().next()).getKey());
                }
            } catch (Exception ee) {
                System.err.println(ee);
                ee.printStackTrace();
                statusBar.setText("Error outputting graph to RDF. See stderr.");
            } finally {
                if (rdf != null)
                    rdf.close();
            }
        }
    }

    void jMenuClustDOT_actionPerformed(ActionEvent e) {
         cluster = true;
         jbtnDOT_actionPerformed(e);
         cluster = false;
    }

    void jbtnDOT_actionPerformed(ActionEvent e) {
        if (noCurrent()) return;
        int minKids = 1;
        int minLinks = 1;
        try {
            minKids = Integer.parseInt(jop.showInputDialog(null,
                  "Don't output nodes with less than this many children",
                  "Children limit", jop.PLAIN_MESSAGE));
        } catch (NumberFormatException nfe) {}
        try {
            minLinks = Integer.parseInt(jop.showInputDialog(null,
                  "Don't output nodes with less than this many links (in or out)",
                  "Peerage limit", jop.PLAIN_MESSAGE));
        } catch (NumberFormatException nfe) {}
        jfc.setFileFilter(dotFilter);
        jfc.showSaveDialog(this);
        if (jfc.getSelectedFile() != null) {
            PrintWriter dot = null;
            try {
                statusBar.setText("Outputting graph to DOT...");
                dot = new PrintWriter(new BufferedWriter(
                                      new FileWriter(jfc.getSelectedFile())));
                if (cluster) {
                    OutputDOT.autoDOTCluster(currentGraph, dot, minKids, minLinks);
                } else {
                    OutputDOT.outputDOT(currentGraph, dot, minKids, minLinks);
                }
                statusBar.setText("Outputting graph to DOT... Done.");
            } catch (Exception ee) {
                System.err.println(ee);
                ee.printStackTrace();
                statusBar.setText("Error outputting graph to DOT. See stderr.");
            } finally {
                if (dot != null)
                    dot.close();
            }
        }
    }

    private HashGraph generateGraph(Reader input) throws IOException, NumberFormatException {
        HashGraph g = new HashGraph();
        StreamTokenizer tok;
        double infinity, dist;
        try {
            dist = Double.parseDouble(jop.showInputDialog(this, "Please enter a maximum distance", "Distance", jop.PLAIN_MESSAGE));
            infinity = Double.parseDouble(jop.showInputDialog(this, "Please enter a number for 'infinty'", "Infinity", jop.PLAIN_MESSAGE));
        } catch (NumberFormatException nfe) {
            jop.showMessageDialog(this, "You must enter a double", "Error", jop.ERROR_MESSAGE);
            throw nfe;
        }
        tok = new StreamTokenizer(input);
        tok.quoteChar('"');
        int t;
        Normalizer norm;
        Node n;
        double curInf = 0; //progressive infinity for new subgraphs
        boolean first = true;
        while ((t = tok.nextToken()) != tok.TT_EOF) {
            if (t == tok.TT_WORD || t == '"') {
                //subgraph
                n = currentGraph.get(tok.sval);
                if (n != null) {
                    System.err.println("Adding " + tok.sval);
                    if (first) {
                        g = currentGraph.subgraph(tok.sval, dist);
                        first = false;
                    }
                    else
                        g = g.costMerge(currentGraph.subgraph(tok.sval, dist), infinity, curInf);
                    curInf += infinity; //the 'infinity' for nodes already in g

                    //listGraph(g);
                    //jop.showConfirmDialog(null, "wait");
                }
            }
        }
        g.copyLinks(currentGraph); //get all the links from the original graph
        return g;
    }

    void jbtnGenerate_actionPerformed(ActionEvent e) {
        if (noCurrent()) return;
        setMerge(false);
        String sg = jop.showInputDialog(this, "Please specify a name for this generated graph", "Name", jop.PLAIN_MESSAGE);
        statusBar.setText("Tokenizing input and generating graph...");
        try {
            if (jop.showConfirmDialog(null, "Input from a file? (Otherwise from text window)", "Select Input", jop.YES_NO_OPTION) == jop.NO_OPTION) {
                graphs.put(sg, generateGraph(new StringReader(jtaOutput.getText())));
            } else {
                jfc.setFileFilter(allFilter);
                jfc.showOpenDialog(this);
                if (jfc.getSelectedFile() != null && jfc.getSelectedFile().exists()) {
                    graphs.put(sg, generateGraph(new BufferedReader(new FileReader(jfc.getSelectedFile()))));
                }
            }
            statusBar.setText("Graph generation complete.");
            jlstGraphs.setListData(graphs.keySet().toArray());
        } catch (Exception ee) {
            System.err.println(ee);
            ee.printStackTrace();
            statusBar.setText("Error generating graph. See stderr.");
        }
    }

    private boolean noCurrent() {
        if (currentGraph == null) {
            statusBar.setText("You must select a graph first");
            return true;
        }
        return false;
    }

    void jMenuInputKeyword_actionPerformed(ActionEvent e) {
        if (noCurrent()) return;
        ArrayList results = new ArrayList();
        Vector words = new Vector();
        StreamTokenizer tok = new StreamTokenizer(new StringReader(jtaOutput.getText()));
        tok.quoteChar('"');
        int t;
        try {
            while ((t = tok.nextToken()) != tok.TT_EOF)
                if (t == tok.TT_WORD || t == '"')
                    words.add(tok.sval);
        } catch (IOException ioe) {}
        int index;
        Outer:
        for (Iterator it = currentGraph.nodeIterator(); it.hasNext();) {
            String search = ((Node)it.next()).getKey();
            for (Iterator wit = words.iterator(); wit.hasNext();) {
                 if ((index = search.toLowerCase().indexOf(((String)wit.next()).toLowerCase())) >= 0) {
                    results.add(new StringIndexPair(search, index));
                    continue Outer;
                 }
            }
        }
        Collections.sort(results);
        jlstKeywords.setListData(results.toArray());
        if (jspKeywords.getDividerLocation() < 25)
            jspKeywords.setDividerLocation(100);
    }

    void jMenuInputCopy_actionPerformed(ActionEvent e) {
        Object[] words = jlstKeywords.getSelectedValues();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < words.length; ++i) {
            sb.append("\n\"");
            sb.append(words[i]);
            sb.append("\"");
        }
        jtaOutput.setText(sb.toString());
    }

    void jMenuOutputNorm_actionPerformed(ActionEvent e) {
        if (noCurrent()) return;
        Normalizer n = new Normalizer(currentGraph);
        n.normalize(currentGraph);
    }

    void jMenuInputCLinks_actionPerformed(ActionEvent e) {
        if (noCurrent()) return;
        copying = true;
        statusBar.setText("Copy Mode: please select the graph to copy links from.");
    }

    void jMenuClone_actionPerformed(ActionEvent e) {
        if (noCurrent()) return;
        setMerge(false);
        String sg = jop.showInputDialog(this, "Please specify a name for the clone", "Name", jop.PLAIN_MESSAGE);
        statusBar.setText("Cloning graph...");
        try {
            graphs.put(sg, currentGraph.clone());
        } catch (CloneNotSupportedException cnse) {}
        jlstGraphs.setListData(graphs.keySet().toArray());
        statusBar.setText("Cloning graph... Done.");
    }

    void jMenuGrow_actionPerformed(ActionEvent e) {
        if (noCurrent()) return;
        growing = true;
        statusBar.setText("Grow Mode: please select the parent graph to grow within.");
    }

    void growCurrent(HashGraph with) {
        setMerge(false);
        growing = false;
        String sg = jop.showInputDialog(this, "Please specify a name for the grown graph", "Name", jop.PLAIN_MESSAGE);
        statusBar.setText("Growing graph...");
        double dist;
        try {
            dist = Double.parseDouble(jop.showInputDialog(this, "Please enter an amount to grow", "Max Distance", jop.PLAIN_MESSAGE));
        } catch (NumberFormatException nfe) {
            jop.showMessageDialog(this, "You must enter a double", "Error", jop.ERROR_MESSAGE);
            return;
        }
        graphs.put(sg, with.grow(currentGraph.nodeIterator(), dist));
        jlstGraphs.setListData(graphs.keySet().toArray());
        statusBar.setText("Growing graph... Done.");
    }

    void jMenuDumpNames_actionPerformed(ActionEvent e) {
        setMerge(false);
        if (noCurrent()) return;
        jfc.setFileFilter(allFilter);
        jfc.showSaveDialog(this);
        PrintWriter out = null;
        if (jfc.getSelectedFile() != null) try {
            String[] strings = new String[currentGraph.size()];
            int i = 0;
            out = new PrintWriter(new BufferedWriter(new FileWriter(jfc.getSelectedFile())));
            for (Iterator it = currentGraph.nodeIterator(); i < strings.length && it.hasNext(); ++i)
                strings[i] = ((Node)it.next()).getKey();
            Arrays.sort(strings, String.CASE_INSENSITIVE_ORDER);
            for (i = 0; i < strings.length; ++i)
                out.println(strings[i]);
        } catch (IOException ioe) {
            System.err.println(ioe);
            ioe.printStackTrace();
            statusBar.setText("An error occured. See stdout");
        } finally {
            try {
                out.close();
            } catch (Exception x) {}
        }
    }
}

class StringIndexPair implements Comparable {
    String s;
    int index;
    public StringIndexPair(String s, int index) {
        this.s = s;
        this.index = index;
    }
    public boolean equals (Object o) {
        StringIndexPair other = (StringIndexPair)o;
        return other.index == index && other.s.length() == s.length();
    }
    public int compareTo(Object o) {
        StringIndexPair other = (StringIndexPair)o;
        int comp = (new Integer(index)).compareTo(new Integer(other.index));
        if (comp == 0)
            return (new Integer(s.length())).compareTo(new Integer(other.s.length()));
        return comp;
    }
    public String toString() {
        return s;
    }
}
