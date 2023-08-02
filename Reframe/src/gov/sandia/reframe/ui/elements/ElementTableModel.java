package gov.sandia.reframe.ui.elements;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import gov.sandia.cortext.fileparsing.pdf.PdfElement;
import replete.text.StringUtil;

public class ElementTableModel extends DefaultTableModel {


    ///////////
    // FIELD //
    ///////////

    private List<PdfElement> elements = new ArrayList<>();


    //////////////////////////
    // ACCESSORS / MUTATORS //
    //////////////////////////

    // Accessor

    public List<PdfElement> getElements() {
        return elements;
    }

    // Accessor (Computed)
    public int findLastPage() {
        int max = 0;
        for(PdfElement elem : elements) {
            if(elem.getPage() > max) {
                max = elem.getPage();
            }
        }
        return max;
    }

    // Mutator

    public void setElements(List<PdfElement> elements) {
        this.elements = elements;
        fireTableDataChanged();
    }

    ////////////////
    // OVERRIDDEN //
    ////////////////

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch(columnIndex) {
            case  0: return String.class;
            case  1: return String.class;
            case  2: return Integer.class;
            case  3: return String.class;
            case  4: return Float.class;
            case  5: return String.class;
            case  6: return String.class;
            case  7: return Float.class;
            case  8: return Float.class;
            case  9: return Float.class;
            case 10: return Float.class;
        }
        return null;  // Won't happen
    }
    @Override
    public int getColumnCount() {
        return 11;
    }
    @Override
    public String getColumnName(int columnIndex) {
        switch(columnIndex) {
            case  0: return "#";
            case  1: return "Element";
            case  2: return "Page";
            case  3: return "Font";
            case  4: return "Size";
            case  5: return "Bold?";
            case  6: return "Italic?";
            case  7: return "X";
            case  8: return "Y";
            case  9: return "Width";
            case 10: return "Height";
        }
        return null;  // Won't happen
    }
    @Override
    public int getRowCount() {
        if(elements == null) {
            return 0;
        }
        return elements.size();
    }
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        PdfElement info = elements.get(rowIndex);
        switch(columnIndex) {
            case  0: return StringUtil.commas(rowIndex);
            case  1: return info.getText() + " {" + code(info.getText()) + "}";
            case  2: return info.getPage();
            case  3: return info.getFontName();
            case  4: return info.getFontSize();
            case  5: return info.isBold() ? "YES" : "NO";
            case  6: return info.isItalic() ? "YES" : "NO";
            case  7: return info.getX();
            case  8: return info.getY();
            case  9: return info.getW();
            case 10: return info.getH();
        }
        return null;  // Won't happen
    }

    private String code(String text) {
        if(text != null && !text.isEmpty()) {
            String ret = "";
            for(int c = 0; c < text.length(); c++) {
                ret += ((int) text.charAt(c)) + ",";
            }
            ret = StringUtil.cut(ret, 1);
            return ret;
        }
        return "?";
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
