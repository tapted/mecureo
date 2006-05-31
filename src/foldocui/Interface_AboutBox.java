package foldocui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.swing.border.*;

public class Interface_AboutBox extends JDialog implements ActionListener {

    private JPanel panel1 = new JPanel();
    private JPanel panel2 = new JPanel();
    private JPanel insetsPanel1 = new JPanel();
    private JPanel insetsPanel2 = new JPanel();
    private JPanel insetsPanel3 = new JPanel();
    private JButton button1 = new JButton();
    private JLabel imageLabel = new JLabel();
    private JLabel label1 = new JLabel();
    private JLabel label2 = new JLabel();
    private JLabel label3 = new JLabel();
    private JLabel label4 = new JLabel();
    private BorderLayout borderLayout1 = new BorderLayout();
    private BorderLayout borderLayout2 = new BorderLayout();
    private FlowLayout flowLayout1 = new FlowLayout();
    private GridLayout gridLayout1 = new GridLayout();
    private String product = "FOLDOC Text Dictionary Parser";
    private String version = "0.2";
    private String copyright = "";
    private String comments = "";
    public Interface_AboutBox(Frame parent) {
        super(parent);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        pack();
    }
    /**Component initialization*/
    private void jbInit() throws Exception  {
        //imageLabel.setIcon(new ImageIcon(Interface_AboutBox.class.getResource("[Your Image]")));
        this.setTitle("About");
        setResizable(false);
        panel1.setLayout(borderLayout1);
        panel2.setLayout(borderLayout2);
        insetsPanel1.setLayout(flowLayout1);
        insetsPanel2.setLayout(flowLayout1);
        insetsPanel2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gridLayout1.setRows(4);
        gridLayout1.setColumns(1);
        label1.setText(product);
        label2.setText(version);
        label3.setText(copyright);
        label4.setText(comments);
        insetsPanel3.setLayout(gridLayout1);
        insetsPanel3.setBorder(BorderFactory.createEmptyBorder(10, 60, 10, 10));
        button1.setText("Ok");
        button1.addActionListener(this);
        insetsPanel2.add(imageLabel, null);
        panel2.add(insetsPanel2, BorderLayout.WEST);
        this.getContentPane().add(panel1, null);
        insetsPanel3.add(label1, null);
        insetsPanel3.add(label2, null);
        insetsPanel3.add(label3, null);
        insetsPanel3.add(label4, null);
        panel2.add(insetsPanel3, BorderLayout.CENTER);
        insetsPanel1.add(button1, null);
        panel1.add(insetsPanel1, BorderLayout.SOUTH);
        panel1.add(panel2, BorderLayout.NORTH);
    }
    /**Overridden so we can exit when window is closed*/
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            cancel();
        }
        super.processWindowEvent(e);
    }
    /**Close the dialog*/
    void cancel() {
        dispose();
    }
    /**Close the dialog on a button event*/
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button1) {
            cancel();
        }
    }
}