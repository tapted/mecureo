package foldocui;

import javax.swing.filechooser.*;


/**
 * Filter for an arbitrary extension
 * @author Trent Apted
 * @version 0.4
 */
class ExtFilter extends FileFilter {
    private String ext;
    private String desc;
    public ExtFilter(String extension, String description) {
        ext = extension;
        desc = description;
    }
    public boolean accept(java.io.File f) {
        return ext.length() == 0 || f.isDirectory() || f.getAbsolutePath().endsWith(ext);
    }
    public String getDescription() {
        return desc;
    }
}