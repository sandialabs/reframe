package gov.sandia.reframe.ui.hierarchy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import gov.sandia.cortext.fileparsing.pdf.HNode;
import gov.sandia.cortext.fileparsing.pdf.PdfParseResult;
import gov.sandia.cortext.fileparsing.pdf.Range;
import gov.sandia.reframe.Pdf;
import gov.sandia.reframe.ui.PdfViewerPanel;
import gov.sandia.reframe.ui.elements.GraphicalPanel;
import gov.sandia.reframe.ui.hierarchy.nodes.NodeDeltaNode;
import gov.sandia.reframe.ui.hierarchy.nodes.NodeDeltaNodeTrace;
import gov.sandia.reframe.ui.hierarchy.nodes.NodeHNode;
import gov.sandia.reframe.ui.hierarchy.nodes.NodeHierBase;
import gov.sandia.reframe.ui.images.ReframeImageModel;
import replete.collections.ArrayUtil;
import replete.collections.Pair;
import replete.text.StringUtil;
import replete.ui.GuiUtil;
import replete.ui.button.RButton;
import replete.ui.images.concepts.CommonConcepts;
import replete.ui.lay.Lay;
import replete.ui.panels.RPanel;
import replete.ui.tabbed.RTabbedPane;
import replete.ui.text.RTextPane;
import replete.ui.tree.RTree;
import replete.ui.tree.RTreeNode;
import replete.ui.tree.RTreePath;
import replete.ui.windows.Dialogs;

public class PdfHierarchyPanel extends RPanel {


    ////////////
    // FIELDS //
    ////////////

    // Core

    private Pdf pdf;

    // UI

    private RTabbedPane tabs;
    private RTree treCurrent = RTree.empty();
    private RTree treDelta = RTree.empty();
    private JLabel lblRows;
    private JLabel lblOv;
    private JSplitPane splCenter;
    private GraphicalPanel pnlGraphical;
    private boolean graphicalOn = false;

    private JTextField txtPage;
    private JTextField txtFind;
    private JPanel pnlNorth;
    private JPanel pnlInit;
    private JPanel pnlControls;
    private String lastSearch;
    private JLabel lblTotal;
    private RTextPane txtText;

    private int prevRows = -1;
    private int prevNonBlankRows = -1;

    private Map<RTreeNode, HNode> nodesWithOverrides = new LinkedHashMap<>();

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public PdfHierarchyPanel(final Pdf pdf) {
        this.pdf = pdf;

        RButton btnRewindMore, btnRewind, btnForward, btnForwardMore;
        JToggleButton btnToggleGraphical;

        pnlInit = Lay.FL("L", "nogap",
            Lay.lb("<html><i>Press Parse...</i></html>", CommonConcepts.RUN),
            "eb=5b,opaque=false"
        );

        pnlControls = Lay.BL(
            "W", Lay.FL("L", "nogap",
                lblRows = Lay.lb("", CommonConcepts.INFO),
                lblOv = Lay.lb("0", ReframeImageModel.NODE_OVER, "eb=5l"),
                Box.createRigidArea(new Dimension(1, 26))
            ),
            "E", Lay.FL("nogap",
                Lay.p(btnToggleGraphical = Lay.btnt("Graphical", ReframeImageModel.GR, 2), "eb=5r"),
                Lay.lb("Find:", "eb=5r"), txtFind = Lay.tx("", 7, "selectall"),
                Lay.p(btnRewindMore = Lay.btn(CommonConcepts.REWIND_MORE, 2), "eb=5lr"),
                Lay.p(btnRewind = Lay.btn(CommonConcepts.REWIND, 2), "eb=5r"),
                txtPage = Lay.tx("", 3, "selectall"),
                lblTotal = Lay.lb("/ ?", "eb=5lr"),
                Lay.p(btnForward = Lay.btn(CommonConcepts.FORWARD, 2), "eb=5r"),
                btnForwardMore = Lay.btn(CommonConcepts.FORWARD_MORE, 2)
            ),
            "eb=5b,alltransp"
        );

        JButton btnFontInc, btnFontDec;
        Lay.BLtg(this,
            "N", pnlNorth = Lay.BL("C", pnlInit, "opaque=false"),
            "C", tabs = Lay.TBL(
                "Current",
                    CommonConcepts.FAVORITE,
                    Lay.sp(treCurrent = Lay.tr()),
                "Delta",
                    ReframeImageModel.DELTA,
                    Lay.sp(treDelta = Lay.tr())
            ),
            "S", Lay.BL(
                "N", Lay.BL(
                    "W", Lay.lb("<html><u>Text</u></html>"),
                    "E", Lay.FL(
                        Lay.lb("Font:"),
                        Lay.p(btnFontInc = Lay.btn(CommonConcepts.EXPAND), "eb=5l"),
                        Lay.p(btnFontDec = Lay.btn(CommonConcepts.COLLAPSE), "eb=5l"),
                        "eb=5tb,nogap"
                    )
                ),
                "C", Lay.sp(txtText = Lay.txp("", "editable=false")),
                "prefh=80"
            ),
            "chtransp,eb=5,bg=" + PdfViewerPanel.BG
        );

        pnlGraphical = new GraphicalPanel();

        btnToggleGraphical.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleGraphicalPanel();
            }
        });

        btnFontInc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                float newSize = treCurrent.getFont().getSize() + 2.0F;
                Font newFont = treCurrent.getFont().deriveFont(newSize);
                treCurrent.setFont(newFont);
                treDelta.setFont(newFont);
                txtText.setFont(newFont);
//                txtText.setAllStyleSizes((int) newSize);
            }
        });
        btnFontDec.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                float newSize = treCurrent.getFont().getSize() - 2.0F;
                if(newSize >= 10.0F) {
                    Font newFont = treCurrent.getFont().deriveFont(newSize);
                    treCurrent.setFont(newFont);
                    treDelta.setFont(newFont);
                    txtText.setFont(newFont);
//                    txtText.setAllStyleSizes((int) newSize);
                }
            }
        });

        treCurrent.setShowsRootHandles(true);
        treDelta.setShowsRootHandles(true);

        treCurrent.addSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent arg0) {
                updateFromSelected();
            }
        });
        treDelta.addSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent arg0) {
                updateFromSelected();
            }
        });

        treCurrent.setToggleClickCount(0);
        treDelta.setToggleClickCount(0);

        treCurrent.addDoubleClickListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if(treCurrent.getSelectionRows().length == 1) {
                    JFrame parent = GuiUtil.fra(PdfHierarchyPanel.this);
                    NodeHNode uNode = (NodeHNode) treCurrent.getSelObject();
                    HNode hOriginal = uNode.getNode();
                    HNode hOverridden = uNode.getNodeOverridden();
                    HNodeOverrideDialog dlg = new HNodeOverrideDialog(parent, hOriginal, hOverridden);
                    dlg.setVisible(true);

                    if(dlg.getResult() == HNodeOverrideDialog.SET) {
                        String bullet = dlg.getOverrideBullet();
                        boolean bulletProblem = dlg.getOverrideBulletProblem();
                        String title = dlg.getOverrideTitle();
                        String text = dlg.getOverrideText();

                        hOverridden = new HNode();
                        hOverridden.setBullet(bullet);
                        hOverridden.setBulletProblem(bulletProblem);
                        hOverridden.setTitle(title);
                        hOverridden.setText(text);
                        hOverridden.setRemoveFromExport(dlg.isRemove());

                        hOverridden.setLevelLabel(hOriginal.getLevelLabel());
                        hOverridden.setChildBulletPattern(hOriginal.getChildBulletPattern());
                        hOverridden.setChildPatternReplace(hOriginal.isChildPatternReplace());
                        hOverridden.setSiblingBulletPattern(hOriginal.getSiblingBulletPattern());
                        hOverridden.setSiblingPatternReplace(hOriginal.isSiblingPatternReplace());
                        hOverridden.setPage(hOriginal.getPage());
                        hOverridden.setBulletGuessed(hOriginal.isBulletGuessed());
                        //nOverridden.setChildren(hOriginal.getChildren());  Should not have children

                        uNode.setNodeOverridden(hOverridden);
                        saveOverrideNode(treCurrent.getSelNode(), hOriginal);

                    } else if(dlg.getResult() == HNodeOverrideDialog.CLEAR) {
                        uNode.setNodeOverridden(null);
                        saveOverrideNode(treCurrent.getSelNode(), hOriginal);
                    }

                    treCurrent.updateUI();
                }
            }
        });

        tabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateFromSelected();
            }
        });

        txtFind.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String match = txtFind.getText().toLowerCase();
                if(match.isEmpty()) {
                    return;
                }
                int extra = match.equals(lastSearch) ? 1 : 0;
                RTree activeTree = getActiveTree();
                int selRow = ArrayUtil.isBlank(activeTree.getSelectionRows()) ? -1 : activeTree.getSelectionRows()[0];
                int start = Math.max(0, selRow + extra);
                boolean foundOne = false;
                for(int r = start; r < start + activeTree.getRowCount(); r++) {
                    int row = r % activeTree.getRowCount();
                    RTreePath P = activeTree.getTPathForRow(row);
                    RTreeNode N = P.getLast();
                    NodeHierBase U = N.get();
                    if(U.containsText(match)) {
                        activeTree.scrollRowToVisible(row);
                        activeTree.setSelectionRow(row);
                        updateFromSelected();
                        foundOne = true;
                        break;
                    }
                }
                if(!foundOne) {
                    Dialogs.showWarning(GuiUtil.fra(PdfHierarchyPanel.this),
                        "No match found for \"" + match + "\".", "Find In Hierarchy");
                }
                lastSearch = match;
            }
        });
        txtPage.setHorizontalAlignment(JTextField.CENTER);
        txtPage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int targetPage = Integer.parseInt(txtPage.getText());
                    scrollToPage(targetPage);
                } catch(Exception ex) {
                    JFrame parent = GuiUtil.fra(PdfHierarchyPanel.this);
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
    }

    private void toggleGraphicalPanel() {
        if(graphicalOn) {
            remove(splCenter);
            add(tabs, BorderLayout.CENTER);
            updateUI();
            graphicalOn = false;
        } else {
            remove(tabs);
            splCenter = Lay.SPL(tabs, pnlGraphical);
            splCenter.setDividerLocation(0.5);
            splCenter.setResizeWeight(0.5);
            add(splCenter, BorderLayout.CENTER);
            updateUI();
            graphicalOn = true;
        }
    }

    protected void saveOverrideNode(RTreeNode selNode, HNode hOriginal) {
        int del;
        if(hOriginal.getOverrideNode() == null) {
            nodesWithOverrides.remove(selNode);
            del = -1;;
        } else {
            nodesWithOverrides.put(selNode, hOriginal);
            del = 1;
        }
        updateOvLabel(del);
    }

    private void updateOvLabel(int del) {
        String delStr = "";
        if(del != 0) {
            delStr = " (" + ((del > 0) ? "+" : "") + del + ")";
            delStr = "<font color='red'>" + delStr + "</font>";
        }

        lblOv.setText("<html>" + nodesWithOverrides.size() + delStr + "</html>");
    }

    public void updateGraphicalPanel() {
        pnlGraphical.setResult(pdf.getExtractionResult());
    }

    public void updateFromPdf() {
        if(pdf.getResult() != null &&
                pdf.getResult().getHierarchy() != null &&
                pdf.getResult().getHierarchy().getRoot() != null) {
            treCurrent.setRootVisible(true);
            int ovCountBefore = nodesWithOverrides.size();
            List<HNode> notFound = transferOverrides();
            int[] curTreeData = populateCurrentTree();
            pnlNorth.remove(pnlInit);
            pnlNorth.add(pnlControls, BorderLayout.CENTER);
            updateGraphicalPanel();

            // Delta Tree
            int numChangedNodes = 0;
            if(pdf.getPrevResult() != null &&
                    pdf.getPrevResult().getHierarchy() != null &&
                    pdf.getPrevResult().getHierarchy().getRoot() != null) {
                treDelta.setRootVisible(true);
                numChangedNodes = populateDeltaTree();
            } else {
                treDelta.clear();
            }

            int nonBlank = curTreeData[0];
            int maxPage = curTreeData[1];

            String rcDelStr = "";
            if(prevRows != -1 && prevRows != treCurrent.getRowCount()) {
                int del = treCurrent.getRowCount() - prevRows;
                rcDelStr = " (" + ((del > 0) ? "+" : "") + del + ")";
                rcDelStr = "<font color='red'>" + rcDelStr + "</font>";
            }

            String rcNbDelStr = "";
            if(prevNonBlankRows != -1 && prevNonBlankRows != nonBlank) {
                int del = nonBlank - prevNonBlankRows;
                rcNbDelStr = " (" + ((del > 0) ? "+" : "") + del + ")";
                rcNbDelStr = "<font color='red'>" + rcNbDelStr + "</font>";
            }

            String changedNodes = "";
            if(numChangedNodes != 0) {
                changedNodes = "; <font color='red'>\u0394 " + numChangedNodes + "</font>";
            }

            lblRows.setText("<html>Rows: <font color='blue'>" +
                treCurrent.getRowCount() + "</font>" + rcDelStr +
                "; Text Rows: <font color='blue'>" + nonBlank +
                "</font>" + rcNbDelStr + changedNodes + "</html>");

            lblTotal.setText("/ " + maxPage);

            updateOvLabel(ovCountBefore == 0 ? 0 : nodesWithOverrides.size() - ovCountBefore);

            prevRows = treCurrent.getRowCount();
            prevNonBlankRows = nonBlank;

            if(!notFound.isEmpty()) {
                String s = "";
                for(HNode h : notFound) {
                    s += h.toSimpleString() + "\n";
                }
                JFrame parent = GuiUtil.fra(this);
                Dialogs.showWarning(parent,
                    "The following override nodes could not be matched to\n" +
                    "the new hierarchy and were not retained:\n\n" + s,
                    "Overrides Not Retained"
                );
            }

        } else {
            pnlNorth.remove(pnlControls);
            pnlNorth.add(pnlInit, BorderLayout.CENTER);
            treCurrent.clear();
        }
        pnlNorth.updateUI();
    }

    private List<HNode> transferOverrides() {
        List<HNode> notFound = new ArrayList<>();
        if(!nodesWithOverrides.isEmpty()) {
            HNode hRoot = pdf.getResult().getHierarchy().getRoot();
            for(RTreeNode n : nodesWithOverrides.keySet()) {
                HNode h = nodesWithOverrides.get(n);
                List<HNode> hList = new ArrayList<>();
                hList.add(h);
                HNode hParent = h.getParent();
                while(hParent != null) {
                    hList.add(0, hParent);
                    hParent = hParent.getParent();
                }
                List<HNode> search = new ArrayList<>();
                search.add(hRoot);

                boolean outerFound = true;
                HNode sFound = null;
                for(HNode hComponent : hList) {
                    boolean found = false;
                    for(int i = 0; i < search.size(); i++) {
                        HNode s = search.get(i);
                        sFound = s;

                        boolean eqLabel = hComponent.getLevelLabel().equalsIgnoreCase(s.getLevelLabel());
                        boolean eqBullet = nonBlankEquals(hComponent.getBullet(), s.getBullet());
                        boolean eqTitle = nonBlankEquals(hComponent.getTitle(), s.getTitle());
                        boolean eqPage = hComponent.getPage() == s.getPage();

                        if(eqLabel && (eqBullet || eqTitle) && eqPage) {
                            search = s.getChildren();
                            found = true;
                            break;
                        }
                    }
                    if(!found) {
                        outerFound = false;
                        break;
                    }
                }
                if(outerFound && sFound != null) {
                    sFound.setOverrideNode(h.getOverrideNode());
                } else {
                    notFound.add(h);
                }
            }
        }
        return notFound;
    }

    private boolean nonBlankEquals(String str1, String str2) {
        if(str1 == null || str1.equals("") || str2 == null || str2.equals("")) {   // One of them doesn't contain any information, so can't correlate
            return false;
        }
        return str1.equals(str2);    // Strings that are a single space (" ") allowed here
    }

    protected int[] populateCurrentTree() {
        PdfParseResult result = pdf.getResult();
        HNode hNode = result.getHierarchy().getRoot();
        nodesWithOverrides.clear();
        RTreeNode nRoot = new RTreeNode(new NodeHNode(hNode));   // Why this not in populateCurrentTree?
        if(hNode.getOverrideNode() != null) {
            nodesWithOverrides.put(nRoot, hNode);
        }
        populateCurrentTree(hNode, nRoot);
        treCurrent.setModel(nRoot);
        treCurrent.expandAll();

        int nonBlank = 0;
        int maxPage = Integer.MIN_VALUE;
        for(int r = 0; r < treCurrent.getRowCount(); r++) {
            RTreePath P = treCurrent.getTPathForRow(r);
            RTreeNode N = P.getLast();
            NodeHNode U = N.get();
            HNode H = U.getNode();
            String text = H.getCleanedText();
            if(!StringUtil.isBlank(text)) {
                nonBlank++;
            }
            if(H.getPage() > maxPage) {
                maxPage = H.getPage();
            }
        }

        return new int[] {nonBlank, maxPage};
    }

    private void populateCurrentTree(HNode hNode, RTreeNode nRoot) {
        for(HNode hChild : hNode.getChildren()) {
            RTreeNode nNew = nRoot.add(new NodeHNode(hChild));
            if(hChild.getOverrideNode() != null) {
                nodesWithOverrides.put(nNew, hChild);
            }
            populateCurrentTree(hChild, nNew);
        }
    }

    protected int populateDeltaTree() {
        PdfParseResult prevResult = pdf.getPrevResult();
        PdfParseResult result = pdf.getResult();
        HNode hNodePrev = prevResult.getHierarchy().getRoot();
        HNode hNodeCur = result.getHierarchy().getRoot();

        DeltaNode dRoot = new DeltaNode();
        int numChangedNodes = diff(hNodePrev, hNodeCur, dRoot);

        RTreeNode nRoot = new RTreeNode(new NodeDeltaNode(dRoot));
        populateDeltaTree(dRoot, nRoot);
        treDelta.setModel(nRoot);
        treDelta.expandAll();
        return numChangedNodes;
    }

    private int diff(HNode hPrev, HNode hCur, DeltaNode dCur) {
        int numChangedNodes = 0;
        dCur.init(hPrev, hCur);
        if(dCur.isSameLabelTitlePage()) {     // Imperfect way to guess at two revisions of node representing same thing
            int min = Math.min(hPrev.getChildren().size(), hCur.getChildren().size());
            for(int i = 0; i < min; i++) {
                HNode hPrevChild = hPrev.getChildren().get(i);
                HNode hCurChild = hCur.getChildren().get(i);
                DeltaNode dChild = new DeltaNode();
                numChangedNodes += diff(hPrevChild, hCurChild, dChild);
                dCur.addChild(dChild);
            }
        }
        return numChangedNodes + (dCur.isSame() ? 0 : 1);
    }

    private void addTrace(Pair<String, String> trace, RTreeNode nDelta) {
        nDelta.add(new NodeDeltaNodeTrace(trace.getValue1()));
        nDelta.add(new NodeDeltaNodeTrace(trace.getValue2()));
    }

    private boolean populateDeltaTree(DeltaNode deltaNode, RTreeNode nDelta) {
        boolean thisDiff = false;

        if(deltaNode.getLabelDiff() != null) {
            addTrace(deltaNode.getLabelDiff(), nDelta);
            thisDiff = true;
        }
        if(deltaNode.getBulletDiff() != null) {
            addTrace(deltaNode.getBulletDiff(), nDelta);
            thisDiff = true;
        }
        if(deltaNode.getTitleDiff() != null) {
            addTrace(deltaNode.getTitleDiff(), nDelta);
            thisDiff = true;
        }
        if(deltaNode.getTextDiff() != null) {
            addTrace(deltaNode.getTextDiff(), nDelta);
            thisDiff = true;
        }
        if(deltaNode.getPageDiff() != null) {
            addTrace(deltaNode.getPageDiff(), nDelta);
            thisDiff = true;
        }
        if(deltaNode.getChildrenDiff() != null) {
            addTrace(deltaNode.getChildrenDiff(), nDelta);
            thisDiff = true;
        }

        for(DeltaNode hChild : deltaNode.getChildren()) {
            RTreeNode nNew = nDelta.add(new NodeDeltaNode(hChild));
            boolean childDiff = populateDeltaTree(hChild, nNew);
            thisDiff = thisDiff || childDiff;
        }

        if(thisDiff) {
            ((NodeDeltaNode) nDelta.get()).setDiffChild(true);
        }

        return thisDiff;
    }

    private class MoveListener implements ActionListener {
        private int movement;
        public MoveListener(int movement) {
            this.movement = movement;
        }
        @Override
        public void actionPerformed(ActionEvent arg0) {
            RTree activeTree = getActiveTree();

            // Get the first selected row
            int row = ArrayUtil.isBlank(activeTree.getSelectionRows()) ? -1 : activeTree.getSelectionRows()[0];
            int curPage;                             // Will hold the first selected row's page

            // If there is nothing selected, then pretend we're on page 0
            if(row == -1) {
                curPage = 0;

            // Else find the page for the first selected row
            } else {
                RTreePath P = activeTree.getTPathForRow(row);
                RTreeNode N = P.getLast();
                NodeHierBase U = N.get();
                curPage = U.getPage();
            }
            int targetPage = curPage + movement;    // Calculate the page user wants to be at

            scrollToPage(targetPage);
        }
    }

    private void scrollToPage(int targetPage) {
        int selRow = -1;                        // Will hold the row in the table that should be selected
        RTree activeTree = getActiveTree();

        // If the user is wanting to go before the first page, select the first row
        if(targetPage < 1) {
            selRow = 0;

        // Else scan all the
        } else {
            boolean found = false;
            for(int r = 0; r < activeTree.getRowCount(); r++) {
                RTreePath P = activeTree.getTPathForRow(r);
                RTreeNode N = P.getLast();
                NodeHierBase U = N.get();
                int nextPage = U.getPage();

                if(nextPage >= targetPage) {
                    selRow = r;
                    found = true;
                    break;
                }
            }
            if(!found) {
                selRow = activeTree.getRowCount() - 1;
            }
        }
        if(selRow >= 0 && selRow < activeTree.getRowCount()) {
            activeTree.scrollRowToVisible(selRow);
            activeTree.setSelectionRow(selRow);
            updateFromSelected();
        }
    }

    private RTree getActiveTree() {
        return tabs.getSelectedIndex() == 0 ? treCurrent : treDelta;
    }

    private void updateFromSelected() {
        RTree activeTree = getActiveTree();
        int row = ArrayUtil.isBlank(activeTree.getSelectionRows()) ? -1 : activeTree.getSelectionRows()[0];
        txtText.clear();
        if(row != -1) {
            RTreePath P = activeTree.getTPathForRow(row);
            RTreeNode N = P.getLast();
            NodeHierBase U = N.get();
            String rowStr = "" + U.getPage();
            String txt = U.getDisplayText();
            String[] parts = StringUtil.maxBoth(txt, 100);
            txtPage.setText(rowStr);
            Font bold = getFont().deriveFont(Font.BOLD);
            if(parts.length == 1) {
                if(parts[0] == null || parts[0].equals("")) {
                    txtText.append("(blank)", getFont().deriveFont(Font.ITALIC), Color.BLUE);
                } else {
                    txtText.append("{", bold, Color.BLUE);
                    if(parts[0].equals(" ") || parts[0].equals("{SPACE}")) {   // Kinda a hack since "{SPACE}" could actually be real text.
                        txtText.append("{SPACE}", bold, Lay.clr("FF6600"));
                    } else {
                        txtText.append(parts[0]);
                    }
                    txtText.append("}", bold, Color.BLUE);
                }
            } else {
                txtText.append("{", bold, Color.BLUE);
                txtText.append(StringUtil.rightTrim(parts[0]));
                txtText.append(" ... ", bold, Color.red);
                txtText.append(StringUtil.leftTrim(parts[1]));
                txtText.append("}", bold, Color.BLUE);
            }

            if(U instanceof NodeHNode) {
                HNode hNode = ((NodeHNode) U).getNode();
                pnlGraphical.setPage(hNode.getPage() - 1);

                Range bulletRange = hNode.getBulletRange();
                Range titleRange = hNode.getTitleRange();
                Range textRange = hNode.getTextRange();

                Map<Integer, Integer> originalToFiltered = null;
                if(pdf != null && pdf.getResult() != null && pdf.getExtractionResult() != null) {
                    originalToFiltered = pdf.getExtractionResult().getOriginalToFiltered();
                }

                if(originalToFiltered != null && (bulletRange != null || titleRange != null || textRange != null)) {
                    Map<Range, Pair<Color, Color>> hl = new HashMap<>();

                    if(bulletRange != null) {
                        hl.put(
                            bulletRange = fixRange(bulletRange, originalToFiltered),
                            new Pair<Color, Color>(Color.blue, Lay.clr("DDF2FF"))
                        );
                    }
                    if(titleRange != null) {
                        hl.put(
                            titleRange = fixRange(titleRange, originalToFiltered),
                            new Pair<Color, Color>(Lay.clr("00890D"), Lay.clr("CCFFCF"))
                        );
                    }
                    if(textRange != null) {
                        hl.put(
                            textRange = fixRange(textRange, originalToFiltered),
                            new Pair<Color, Color>(Color.red, Lay.clr("FFE8E0"))
                        );
                    }

                    pnlGraphical.setNodeHighlights(hl);
                    System.out.println("SET HNODE ONTO GRAPHICAL: BR=" + bulletRange + "/TR=" + titleRange + "/TX=" + textRange);
                    System.out.println( "  ^ " + hNode.getDiagnosticString());
                }

            } else if(U instanceof NodeDeltaNode) {
                pnlGraphical.setNodeHighlights(null);   // How to get HNode?

            } else if(U instanceof NodeDeltaNodeTrace) {
                pnlGraphical.setNodeHighlights(null);   // How to get HNode?
            }

        } else {
            pnlGraphical.setNodeHighlights(null);
            txtPage.setText("");
        }
    }

    private Range fixRange(Range range, Map<Integer, Integer> originalToFiltered) {
        return new Range(
            originalToFiltered.get(range.getStart()),
            originalToFiltered.get(range.getEndNonIncl() - 1) + 1
        );
    }
}
