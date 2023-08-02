package gov.sandia.reframe.ui.elements;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import gov.sandia.cortext.fileparsing.pdf.ExtractionResult;
import gov.sandia.cortext.fileparsing.pdf.PdfElement;
import gov.sandia.reframe.Pdf;
import gov.sandia.reframe.ui.PdfViewerPanel;
import gov.sandia.reframe.ui.images.ReframeImageModel;
import gov.sandia.reframe.ui.params.PdfParametersPanel;
import replete.event.ChangeNotifier;
import replete.text.StringUtil;
import replete.ui.GuiUtil;
import replete.ui.button.RButton;
import replete.ui.images.concepts.CommonConcepts;
import replete.ui.lay.Lay;
import replete.ui.panels.RPanel;
import replete.ui.table.RTable;
import replete.ui.text.RTextField;
import replete.ui.text.editor.REditor;
import replete.ui.uiaction.PopupMenuActionDescriptor;
import replete.ui.uiaction.UIAction;
import replete.ui.uiaction.UIActionListener;
import replete.ui.uiaction.UIActionMap;
import replete.ui.uiaction.UIActionPopupMenu;
import replete.ui.windows.Dialogs;

public class PdfElementsPanel extends RPanel {


    ////////////
    // FIELDS //
    ////////////

    // Constants
    private static final int ELEM_COL = 1;
    private static final int PAGE_COL = 2;

    // Core
    private Pdf pdf;
    private int currentPage = 1;

    // UI
    private JLabel lblCount;
    private JLabel lblTotal;
    private RTable tblElements;
    private ElementTableModel mdlElements = new ElementTableModel();
    private RTextField txtPage;
    private RTextField txtFind;
    private JPanel pnlNorth;
    private JPanel pnlControls;
    private String lastFind;
    private GraphicalPanel pnlGraphical;
    private PdfParametersPanel pnlParams;
    private REditor edText;
    private boolean suppressTablePageUpdate;
    private ExtractionResult result;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public PdfElementsPanel(final Pdf pdf) {
        this.pdf = pdf;
        RButton btnRewindMore, btnRewind, btnForward, btnForwardMore;

        pnlControls = Lay.BL(
            "W", Lay.FL("L", "nogap",
                lblCount = Lay.lb("", CommonConcepts.INFO, "eb=5l"),
                Box.createRigidArea(new Dimension(1, 26))
            ),
            "E", Lay.FL("nogap",
                Lay.lb("Find:", "eb=5r"), txtFind = Lay.tx("", 7, "selectall"),
                Lay.p(btnRewindMore = Lay.btn(CommonConcepts.REWIND_MORE, 2), "eb=5lr"),
                Lay.p(btnRewind = Lay.btn(CommonConcepts.REWIND, 2), "eb=5r"),
                txtPage = Lay.tx("1", 3, "selectall"),
                lblTotal = Lay.lb("/ ?", "eb=5lr"),
                Lay.p(btnForward = Lay.btn(CommonConcepts.FORWARD, 2), "eb=5r"),
                btnForwardMore = Lay.btn(CommonConcepts.FORWARD_MORE, 2)
            ),
            "eb=5b,alltransp"
        );

        Lay.BLtg(this,
            "N", pnlNorth = Lay.BL("opaque=false"),
            "C", Lay.TBL(
                "Table", Lay.sp(tblElements = Lay.tbl(mdlElements), "hsb=asneeded"),
                "Text", edText = Lay.ed("", "ruler=true,editable=false"),
                "Graphical", pnlGraphical = new GraphicalPanel()
            ),
            "eb=5,bg=" + PdfViewerPanel.BG
        );

        tblElements.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        tblElements.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e)) {
                    int clickedRow = tblElements.rowAtPoint(e.getPoint());
                    boolean found = false;
                    for(int row : tblElements.getSelectedRows()) {
                        if(row == clickedRow) {
                            found = true;
                            break;
                        }
                    }
                    if(!found) {
                        tblElements.getSelectionModel().setSelectionInterval(
                            clickedRow, clickedRow);
                    }
                    ElementsTableActionMap actionMap = new ElementsTableActionMap();
                    JPopupMenu mnuPopup = new UIActionPopupMenu(actionMap);
                    mnuPopup.show(tblElements, e.getX(), e.getY());
                }
            }
        });

        txtFind.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String match = txtFind.getText().toLowerCase();
                if(match.isEmpty()) {
                    return;
                }
                int matchChar = 0;
                int rowStart = 0;
                int extra = match.equals(lastFind) ? 1 : 0;
                for(int r = Math.max(0, tblElements.getSelectedRow() + extra); r < mdlElements.getRowCount(); r++) {
                    String element = ((String) mdlElements.getValueAt(r, ELEM_COL)).toLowerCase();

                    if(match.charAt(matchChar) == element.charAt(0)) {
                        if(matchChar == 0) {
                            rowStart = r;
                        }
                        matchChar++;
                    } else {
                        matchChar = 0;
                    }

                    if(matchChar == match.length()) {
                        tblElements.ensureIndexIsVisible(r);
                        tblElements.getSelectionModel().setSelectionInterval(rowStart, r);
                        updateFromSelected();
                        break;
                    }
                }
                lastFind = match;
            }
        });
        txtPage.setHorizontalAlignment(JTextField.CENTER);
        txtPage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int targetPage = Integer.parseInt(txtPage.getText());
                    scrollToPage(targetPage, false);
                } catch(Exception ex) {
                    JFrame parent = GuiUtil.fra(PdfElementsPanel.this);
                    Dialogs.showDetails(parent, "An error occurred selecting the page.", "Error", ex);
                }
            }
        });
        btnRewindMore.setToolTipText("Back 10 Pages");
        btnRewind.setToolTipText("Back 1 Page");
        btnForward.setToolTipText("Forward 1 Page");
        btnForwardMore.setToolTipText("Forward 10 Pages");

        btnRewindMore.addActionListener(new MoveListener(-10));
        btnRewind.addActionListener(new MoveListener(-1));
        btnForward.addActionListener(new MoveListener(1));
        btnForwardMore.addActionListener(new MoveListener(10));

        tblElements.setFillsViewportHeight(true);
        tblElements.setColumnWidths(
            new int[][] {
                {60, 60, 60},   // #
                {90, 90, 90},   // Elem 70
                {50, 50, 50},   // Page
                {200, 200, -1}, // Font
                {50, 50, 50},   // Size
                {60, 60, 60},   // Bold
                {60, 60, 60},   // Italic
                {90, 90, 90},   // X
                {90, 90, 90},   // Y
                {90, 90, 90},   // Width
                {90, 90, 90},   // Height
            }
        );

        tblElements.addSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(!e.getValueIsAdjusting()) {
                    if(!suppressTablePageUpdate) {
                        updateFromSelected();
                    }
                }
            }
        });

        initializeColumnStyles();
    }

    public void setResult(ExtractionResult result) {
        this.result = result;
    }

    public ExtractionResult getResult() {
        return result;
    }

    public void setParametersPanel(PdfParametersPanel pnlParams) {
        this.pnlParams = pnlParams;
    }

    private void initializeColumnStyles() {
        DefaultTableCellRenderer rendNum = new DefaultTableCellRenderer();
        rendNum.setHorizontalAlignment(JLabel.RIGHT);

        DefaultTableCellRenderer rendElem = new DefaultTableCellRenderer() {
            Font rendElemFont = new Font("Courier New", Font.BOLD, 12);
            @Override
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                        row, column);
                setFont(rendElemFont);
                return this;
            }
        };
        rendElem.setHorizontalAlignment(JLabel.CENTER);
        rendElem.setForeground(Color.red);

        DefaultTableCellRenderer rendSize = new DefaultTableCellRenderer() {
            private Color rendSizeColor = Color.red;
            private Font rendSizeFont = new JLabel().getFont();
            private Font rendSizePlainFont = rendSizeFont.deriveFont(Font.PLAIN);

            @Override
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);
                if(value instanceof Float) {
                    float f = (Float) value;
                    if(f != 12.0F) {
                        setFont(rendSizeFont);
                        setForeground(rendSizeColor);
                    } else {
                        setFont(rendSizePlainFont);
                        setForeground(Color.black);
                    }
                }
                return this;
            }
        };
        rendSize.setHorizontalAlignment(JLabel.RIGHT);

        DefaultTableCellRenderer rendStyle = new DefaultTableCellRenderer() {
            private Color rendStyleColor = Lay.clr("2C8904");
            private Font rendStyleFont = new JLabel().getFont();
            private Font rendStylePlainFont = rendStyleFont.deriveFont(Font.PLAIN);
            @Override
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);
                if("YES".equals(value)) {
                    setFont(rendStyleFont);
                    setForeground(rendStyleColor);
                } else {
                    setFont(rendStylePlainFont);
                    setForeground(Color.black);
                }
                return this;
            }
        };
        rendStyle.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer rendXY = new DefaultTableCellRenderer() {
            private Color rendSizeColor = Lay.clr("0428CC");
            private Font rendSizeFont = new JLabel().getFont();
            private Font rendSizePlainFont = rendSizeFont.deriveFont(Font.PLAIN);

            @Override
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);
                if(value instanceof Float) {     // Unnecessary
                    float f = (Float) value;
                    setText(String.format("%.7f", value));
                    if(f == (int) f) {
                        setFont(rendSizeFont);
                        setForeground(rendSizeColor);
                    } else {
                        setFont(rendSizePlainFont);
                        setForeground(Color.black);
                    }
                }
                return this;
            }
        };
        rendXY.setHorizontalAlignment(JLabel.RIGHT);

        tblElements.getColumnModel().getColumn(0).setCellRenderer(rendNum);
        tblElements.getColumnModel().getColumn(1).setCellRenderer(rendElem);
        tblElements.getColumnModel().getColumn(4).setCellRenderer(rendSize);
        tblElements.getColumnModel().getColumn(5).setCellRenderer(rendStyle);
        tblElements.getColumnModel().getColumn(6).setCellRenderer(rendStyle);
        tblElements.getColumnModel().getColumn(7).setCellRenderer(rendXY);
        tblElements.getColumnModel().getColumn(8).setCellRenderer(rendXY);
        tblElements.getColumnModel().getColumn(9).setCellRenderer(rendXY);
        tblElements.getColumnModel().getColumn(10).setCellRenderer(rendXY);
    }

    private class MoveListener implements ActionListener {
        private int movement;
        public MoveListener(int movement) {
            this.movement = movement;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            scrollToPage(currentPage + movement, true);
        }
    }

    public void updateUIAfterInitialize(List<PdfElement> elements) {
        lblTotal.setText("/ " + elements.get(elements.size() - 1).getPage());
        pnlNorth.add(pnlControls, BorderLayout.CENTER);
        pnlNorth.updateUI();
    }

    public void updateElementsFromPdf(List<PdfElement> elements) {
        if(elements == null) {
            elements = new ArrayList<>();
        }

        int maxPage = 1;
        for(PdfElement elem : elements) {
            if(elem.getPage() > maxPage) {
                maxPage = elem.getPage();
            }
        }
        pnlGraphical.setResult(pdf.getExtractionResult());
        if(currentPage > maxPage) {
            pnlGraphical.setPage(maxPage - 1);
        } else {
            pnlGraphical.setPage(0);
        }

        mdlElements.setElements(elements);

        lblCount.setText("<html>Elements: <font color='blue'>" +
            StringUtil.commas(mdlElements.getElements().size()) + "</font></html>");

        // Fill text editor
        float y = mdlElements.getElements().get(0).getY();
        float size = mdlElements.getElements().get(0).getFontSize();
        StringBuilder builder = new StringBuilder();
        boolean bold = mdlElements.getElements().get(0).isBold();
        float x = mdlElements.getElements().get(0).getX();
        for(PdfElement elem : mdlElements.getElements()) {
            if(elem.getX() - x > 4) {
                builder.append(" ");
                x = elem.getX();
            }
            if(Math.abs(elem.getY() - y) > 1.0F) {
//                System.out.println("CHANGE " + y + " = > " + elem.getY());
//                txtP.getTextPane().append("\n");
                builder.append("\n");
                y = elem.getY();
            }
//            System.out.println(y);
//            if(size != elem.getFontSize()) {
//                txtP.getTextPane().append(builder.toString(), new Font("Arial", Font.PLAIN, (int) size));
//                builder.delete(0, builder.length());
//                size = elem.getFontSize();
//            }
            builder.append(elem.getText());
//            txtP.append(elem.getText());
        }
        edText.getTextPane().clear();
        edText.getTextPane().append(builder.toString());
    }

    private void scrollToPage(int targetPage, boolean buttons) {
        int lastPage = mdlElements.findLastPage();  // 0 means no elements

        if(lastPage == 0) {
            targetPage = 1;

        } else if(targetPage < 1) {
            if(buttons) {
                targetPage = lastPage + targetPage;    // Wrap around when using buttons
            } else {
                targetPage = 1;
            }

        } else if(targetPage > lastPage) {
            if(buttons) {
                targetPage = targetPage - lastPage;        // Wrap around when using buttons
            } else {
                targetPage = lastPage;
            }
        }

        currentPage = targetPage;
        txtPage.setText("" + currentPage);
        pnlGraphical.setPage(currentPage - 1);

        // Select table row
        int selRow = -1;
        for(int r = 0; r < mdlElements.getRowCount(); r++) {
            int nextPage = (Integer) mdlElements.getValueAt(r, PAGE_COL);

            if(nextPage <= currentPage) {
                selRow = r;
            }
            if(nextPage >= currentPage) {
                break;
            }
        }

        if(selRow >= 0 && selRow < mdlElements.getRowCount()) {
            suppressTablePageUpdate = true;
            tblElements.getSelectionModel().setSelectionInterval(selRow, selRow);
            suppressTablePageUpdate = false;
            tblElements.ensureIndexIsVisible(selRow);
        }
    }

    private void updateFromSelected() {
        int row = tblElements.getSelectedRow();
        if(row != -1) {
            currentPage = (Integer) mdlElements.getValueAt(row, PAGE_COL);
            txtPage.setText(currentPage);
        }
    }

    private class ElementsTableActionMap extends UIActionMap {
        private ElementsTableActionMap() {
            UIActionListener listener = new UIActionListener() {
                @Override
                public void actionPerformed(ActionEvent e, UIAction action) {
                    fireCreateRuleNotifier();
                }
            };
            createAction("add-rule", listener)
                .addDescriptor(
                    new PopupMenuActionDescriptor()
                        .setText("Create Rule...")
                        .setIcon(ReframeImageModel.RULE));
        }
    }

    private ChangeNotifier createRuleNotifier = new ChangeNotifier(this);
    public void addCreateRuleListener(ChangeListener listener) {
        createRuleNotifier.addListener(listener);
    }
    private void fireCreateRuleNotifier() {
        createRuleNotifier.fireStateChanged();
    }

    public List<PdfElement> getSelectedElements() {
        int[] rows = tblElements.getSelectedRows();
        List<PdfElement> elems = mdlElements.getElements();
        List<PdfElement> selectedElems = new ArrayList<PdfElement>();
        for(int row : rows) {
            PdfElement elem = elems.get(row);
            selectedElems.add(elem);
        }
        return selectedElems;
    }
}
