package foldocui;

import javax.swing.filechooser.*;


public class FDGFilter extends FileFilter {
    public boolean accept(java.io.File f) {
        return f.isDirectory() || f.getAbsolutePath().endsWith(".fdg");
    }
    public String getDescription() {
        return "FOLDOC graphs";
    }
}