package gov.sandia.reframe.ui.params;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreePath;

import gov.sandia.cortext.fileparsing.pdf.PdfExtractionParameters;
import gov.sandia.cortext.fileparsing.pdf.PdfParseParameters;
import gov.sandia.cortext.fileparsing.pdf.Rule;
import gov.sandia.cortext.fileparsing.pdf.RuleSet;
import gov.sandia.cortext.fileparsing.pdf.RuleSetHierarchy;
import gov.sandia.cortext.fileparsing.pdf.RuleSetNode;
import gov.sandia.reframe.Pdf;
import gov.sandia.reframe.ui.UIController;
import gov.sandia.reframe.ui.images.ReframeImageModel;
import gov.sandia.reframe.ui.params.nodes.NodeRoot;
import gov.sandia.reframe.ui.params.nodes.NodeRule;
import gov.sandia.reframe.ui.params.nodes.NodeRuleSet;
import replete.event.ChangeNotifier;
import replete.ui.GuiUtil;
import replete.ui.button.RButton;
import replete.ui.images.concepts.CommonConcepts;
import replete.ui.lay.Lay;
import replete.ui.panels.RPanel;
import replete.ui.text.DocumentChangeListener;
import replete.ui.text.RTextField;
import replete.ui.tree.NodeBase;
import replete.ui.tree.RTree;
import replete.ui.tree.RTreeNode;
import replete.ui.tree.TModel;
import replete.ui.windows.Dialogs;

public class PdfParametersPanel extends RPanel {


    ////////////
    // FIELDS //
    ////////////

    // UI
    private UIController uiController;
    private Pdf pdf;
    private RButton btnExtractSettings;
    private RButton btnExtract;
    private RButton btnProcess;
    private RTreeNode nRoot;
    private RTree treParams;
    private JLabel lblExpand;
    private JLabel lblCollapse;
    private JPanel pnlMin;
    private JPanel pnlFull;
    private boolean expanded = true;

    // Copy
    private RTreeNode nCopiedRuleSet;
    private RTreeNode nCopiedRule;


    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public PdfParametersPanel(final UIController uiController, final Pdf pdf) {
        this.uiController = uiController;
        this.pdf = pdf;

        RButton btnAddRuleSet;
        RButton btnAddRule;
        RButton btnEdit;
        RButton btnRemove;
        RButton btnCopy;
        RButton btnPaste;
        RButton btnUp;
        RButton btnDown;
        RButton btnExpand;
        RButton btnExpandAll;
        RButton btnCollapse;
        RButton btnCollapseAll;

        nRoot = new RTreeNode(new NodeRoot());

        pnlMin = Lay.BxL(
            Lay.lb(" ", CommonConcepts.SORT_ASC, "size=14"),
            lblExpand = Lay.lb(ReframeImageModel.AR_GR_L,
                "cursor=hand,ttt=Expand"),
            "eb=5,dimw=26,bg=C9ECFF"
        );

        pnlFull = Lay.BL(
            "N", Lay.FL("L", "nogap",
                    Lay.lb("<html><u>PDF Configuration Settings</u></html>",
                        "size=14"),
                    lblCollapse = Lay.lb(ReframeImageModel.AR_BL_R,
                        "cursor=hand,ttt=Collapse,eb=5l")
            ),
            "C", Lay.BL(
                "N", Lay.BL(
                    "N", Lay.FL("L", "nogap",
                            Lay.lb("<html><u>Extraction</u></html>",
                                ReframeImageModel.EXTRACT, "size=14")
                        ),
                    "S", Lay.BL(
                            "W", btnExtractSettings = Lay.btn("Extraction Settings", CommonConcepts.COMPUTATION),
                            "E", btnExtract         = Lay.btn("&Extract", CommonConcepts.FORWARD, "htext=left"))
                ),
                "C", Lay.BL(
                    "N", Lay.BL(
                        "C", Lay.FL("L", "nogap",
                            Lay.lb("<html><u>Parsing</u></html>",
                                CommonConcepts.SORT_ASC, "size=14")
                        ),
                        "E", btnProcess = Lay.btn("&Parse", CommonConcepts.FORWARD, "htext=left")
                    ),
                    "C", Lay.BL(
                        "C", Lay.p(Lay.sp(treParams = Lay.tr(nRoot)), "eb=5bt"),
                        "E", Lay.BxL("Y",
                            Lay.p(btnAddRuleSet = Lay.btn(ReframeImageModel.RULESET_ADD, 2), "eb=5tl,alignx=0.5,maxH=20"),
                            Lay.p(btnAddRule = Lay.btn(ReframeImageModel.RULE_ADD, 2), "eb=5tl,alignx=0.5,maxH=20"),
                            Lay.p(btnEdit = Lay.btn(CommonConcepts.EDIT, 2), "eb=5tl,alignx=0.5,maxH=20"),
                            Lay.p(btnRemove = Lay.btn(CommonConcepts.REMOVE, 2), "eb=5tl,alignx=0.5,maxH=20"),
                            Box.createVerticalStrut(10),
                            Lay.p(btnCopy = Lay.btn(CommonConcepts.COPY, 2), "eb=5tl,alignx=0.5,maxH=20"),
                            Lay.p(btnPaste = Lay.btn(CommonConcepts.PASTE, 2), "eb=5tl,alignx=0.5,maxH=20"),
                            Box.createVerticalStrut(10),
                            Lay.p(btnUp = Lay.btn(CommonConcepts.MOVE_UP, 2), "eb=5tl,alignx=0.5,maxH=20"),
                            Lay.p(btnDown = Lay.btn(CommonConcepts.MOVE_DOWN, 2), "eb=5tl,alignx=0.5,maxH=20"),
                            Box.createVerticalStrut(10),
                            Lay.p(btnExpandAll = Lay.btn(CommonConcepts.EXPAND_ALL, 2), "eb=5tl,alignx=0.5,maxH=20"),
                            Lay.p(btnExpand = Lay.btn(CommonConcepts.EXPAND, 2), "eb=5tl,alignx=0.5,maxH=20"),
                            Lay.p(btnCollapseAll = Lay.btn(CommonConcepts.COLLAPSE_ALL, 2), "eb=5tl,alignx=0.5,maxH=20"),
                            Lay.p(btnCollapse = Lay.btn(CommonConcepts.COLLAPSE, 2), "eb=5tl,alignx=0.5,maxH=20"),
                            Box.createVerticalGlue()
                        )
                    )
                )
            ),
            "eb=5,bg=C9ECFF,chtransp"
        );

        Lay.BLtg(this,
            "C", pnlFull
        );

        lblExpand.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                expandLibrary();
            }
        });
        lblCollapse.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                collapseLibrary();
            }
        });

        treParams.setToggleClickCount(0);

        btnAddRuleSet.setToolTipText("Add Rule Set");
        btnAddRule.setToolTipText("Add Rule");
        btnEdit.setToolTipText("Edit...");
        btnRemove.setToolTipText("Remove");
        btnCopy.setToolTipText("Copy");
        btnPaste.setToolTipText("Paste");
        btnUp.setToolTipText("Move Up");
        btnDown.setToolTipText("Move Down");
        btnExpandAll.setToolTipText("Expand All");
        btnExpand.setToolTipText("Expand");
        btnCollapseAll.setToolTipText("Collapse All");
        btnCollapse.setToolTipText("Collapse");

        btnExtractSettings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame parent = GuiUtil.fra(PdfParametersPanel.this);
                ExtractionSettingsDialog dlg = new ExtractionSettingsDialog(parent, pdf.getExtractionParams());
                dlg.setVisible(true);
                if(dlg.getResult() == RuleEditDialog.OK) {
                    pdf.setExtractionParams(dlg.getExtractionParameters());
                    uiController.setDirty(true);
                }
            }
        });

        btnExtract.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireExtractNotifier();
            }
        });

        btnProcess.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireProcessNotifier();
            }
        });

        treParams.addDoubleClickListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                RTreeNode nSel = treParams.getTSelectionNode();
                if(nSel != null) {
                    doEdit();
                }
            }
        });
        treParams.addEnterKeyListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                RTreeNode nSel = treParams.getTSelectionNode();
                if(nSel != null) {
                    doEdit();
                }
            }
        });

        btnAddRuleSet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RTreeNode nSel = treParams.getTSelectionNode();
                if(nSel == null) {
                    nSel = nRoot;
                }
                NodeBase uSel = nSel.get();
                if(uSel instanceof NodeRule) {
                    nSel = nSel.getRParent();
                }
                TModel model = treParams.getTModel();
                RTreeNode nNew = model.append(nSel, new NodeRuleSet());
                treParams.expand(nSel);
                uiController.setDirty(true);
                treParams.select(nNew);
            }
        });

        btnAddRule.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Rule rule = new Rule();
                addRuleToTree(rule);
            }
        });

        btnEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doEdit();
            }
        });

        btnRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RTreeNode[] nSels = treParams.getTSelectionNodes();
                if(nSels != null) {
                    TModel model = treParams.getTModel();
                    for(RTreeNode nSel : nSels) {
                        if(!nSel.isRoot()) {
                            model.remove(nSel);
                            uiController.setDirty(true);
                        }
                    }
                }
            }
        });

        btnCopy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RTreeNode nSel = treParams.getTSelectionNode();
                if(nSel == null) {
                    return;
                }

                if(nSel.get() instanceof NodeRuleSet) {
                    nCopiedRuleSet = nSel;
                    nCopiedRule = null;

                } else if(nSel.get() instanceof NodeRule) {
                    nCopiedRule = nSel;
                    nCopiedRuleSet = null;
                }
            }
        });

        btnPaste.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(treParams.getTSelectionNode() == null) {
                    return;
                }

                if(nCopiedRule != null) {
                    for(RTreeNode nSel : treParams.getTSelectionNodes()) {
                        if(nSel.get() instanceof NodeRuleSet) {
                            pasteRuleInto(nSel);
                        }
                    }

                } else if(nCopiedRuleSet != null) {
                    for(RTreeNode nSel : treParams.getTSelectionNodes()) {
                        if(nSel.get() instanceof NodeRuleSet || nSel.get() instanceof NodeRoot) {
                            pasteRuleSetInto(nSel);
                        }
                    }
                }
            }
        });

        btnUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RTreeNode nSel = treParams.getTSelectionNode();
                if(nSel == null) {
                    return;
                }
                if(nSel.get() instanceof NodeRoot) {
                    return;
                }
                RTreeNode nParent = nSel.getRParent();

                int i = nParent.getIndex(nSel);
                int x;
                if(nSel.get() instanceof NodeRule) {
                    x = 0;
                } else {
                    x = countRuleNodes(nParent);
                }

                if(i > x) {
                    TModel model = treParams.getTModel();
                    model.remove(nSel);
                    model.insertNodeInto(nSel, nParent, i - 1);

                    TreePath path = treParams.getPath(nSel);
                    treParams.setSelectionPath(path);
                    treParams.scrollPathToVisible(path);
                    treParams.updateUI();

                    uiController.setDirty(true);
                }
            }
        });

        btnDown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RTreeNode nSel = treParams.getTSelectionNode();
                if(nSel == null) {
                    return;
                }
                if(nSel.get() instanceof NodeRoot) {
                    return;
                }
                RTreeNode nParent = nSel.getRParent();

                int i = nParent.getIndex(nSel);
                int x;
                if(nSel.get() instanceof NodeRule) {
                    x = countRuleNodes(nParent) - 1;
                } else {
                    x = nParent.getCount() - 1;
                }

                if(i < x) {
                    TModel model = treParams.getTModel();
                    model.remove(nSel);
                    model.insertNodeInto(nSel, nParent, i + 1);

                    TreePath path = treParams.getPath(nSel);
                    treParams.setSelectionPath(path);
                    treParams.scrollPathToVisible(path);
                    treParams.updateUI();

                    uiController.setDirty(true);
                }
            }
        });

        btnCollapse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                treParams.collapseSelected();
            }
        });

        btnCollapseAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                treParams.collapseAllSelected();
            }
        });

        btnExpand.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                treParams.expandSelected();
            }
        });

        btnExpandAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                treParams.expandAllSelected();
            }
        });

        if(pdf.getParams() != null) {
            if(pdf.getParams().getRuleSetHierarchy() != null) {
                RuleSetHierarchy hier = pdf.getParams().getRuleSetHierarchy();
                if(hier.getRoot() != null) {
                    RuleSetNode rootRsn = hier.getRoot();
                    RTreeNode nAlmostRoot = new RTreeNode(new NodeRuleSet());
                    nRoot.add(nAlmostRoot);
                    populateFromParams(nAlmostRoot, rootRsn);
                    treParams.expandAll();
                    treParams.updateUI();
                }
            }
        }
    }

    private void setTextParam(RTextField txt, float value, float dflt) {
        if(value == dflt) {
            txt.setText("");
        } else {
            txt.setText(value);
        }
    }

    DocumentListener dirtyListener = new DocumentChangeListener() {
        @Override
        public void documentChanged(DocumentEvent e) {
            uiController.setDirty(true);
        }
    };

    private void populateFromParams(RTreeNode nAlmostRoot, RuleSetNode rsn) {
        NodeRuleSet uRuleSet = nAlmostRoot.get();
        uRuleSet.setLevelLabel(rsn.getLevelLabel());
        uRuleSet.setChildBulletPattern(rsn.getChildBulletPattern());
        uRuleSet.setChildPatternReplace(rsn.isChildPatternReplace());
        uRuleSet.setSiblingBulletPattern(rsn.getSiblingBulletPattern());
        uRuleSet.setSiblingPatternReplace(rsn.isSiblingPatternReplace());
        RuleSet ruleSet = rsn.getRuleSet();
        if(ruleSet != null && !ruleSet.isEmpty()) {
            for(Rule rule : ruleSet.getRules()) {
                nAlmostRoot.add(new NodeRule(rule));
            }
        }
        for(RuleSetNode childRsn : rsn.getChildren()) {
            RTreeNode nChild = nAlmostRoot.add(new NodeRuleSet());
            populateFromParams(nChild, childRsn);

        }
    }

    protected void collapseLibrary() {
        removeAll();
        add(pnlMin, BorderLayout.CENTER);
        updateUI();
        expanded = false;
        fireExpandNotifier();
    }
    protected void expandLibrary() {
        removeAll();
        add(pnlFull, BorderLayout.CENTER);
        updateUI();
        expanded = true;
        fireExpandNotifier();
    }

    //////////
    // MISC //
    //////////

    // TODO - Turn this into real preprocessing rule...
    // For "DOE O 473.3 PPO.df" & "DOE M 470.4-2B UCNI.pdf" use
    //    78.6F and 753.0F
    // For "UCNI 5210-41M Vol 2.PDF" use
    //    88.0F and 714.0F  (though some page #'s start at 716....)
    // For "UCNI 5210-41M Vol 1.PDF" use
    //    85.8F and 690.0F
    // For "UCNI 5210-41M Vol 3.PDF" use
    //    93.0F and 713.0F

    public PdfExtractionParameters createExtractionParameters() {
        return pdf.getExtractionParams();
    }

    public PdfParseParameters createParseParameters() {
        PdfParseParameters params = new PdfParseParameters();

        if(nRoot.getCount() > 0) {
            RuleSetNode rootRsn = new RuleSetNode();
            params.getRuleSetHierarchy().setRoot(rootRsn);
            populateParamsFromTree(nRoot.getRChildAt(0), rootRsn);  // Only first child right now
        }

        return params;
    }

    private void populateParamsFromTree(RTreeNode n, RuleSetNode rsn) {
        if(n.get() instanceof NodeRuleSet) {
            NodeRuleSet uRuleSet = n.get();
            String levelLabel = uRuleSet.getLevelLabel();
            String childBulletPattern = uRuleSet.getChildBulletPattern();
            boolean childPatternReplace = uRuleSet.getChildPatternReplace();
            String siblingBulletPattern = uRuleSet.getSiblingBulletPattern();
            boolean siblingPatternReplace = uRuleSet.getSiblingPatternReplace();
            rsn.setLevelLabel(levelLabel);
            rsn.setChildBulletPattern(childBulletPattern);
            rsn.setSiblingBulletPattern(siblingBulletPattern);
            rsn.setChildPatternReplace(childPatternReplace);
            rsn.setSiblingPatternReplace(siblingPatternReplace);
            RuleSet rs = new RuleSet()
                .setLevelLabel(levelLabel);
            for(RTreeNode nRule : n.getRChildren(NodeRule.class)) {
                NodeRule uRule = nRule.get();
                rs.addRule(uRule.getRule());
            }
            rsn.setRuleSet(rs);
            for(RTreeNode nChild : n.getRChildren(NodeRuleSet.class)) {
                RuleSetNode childRsn = new RuleSetNode();
                rsn.addChild(childRsn);
                populateParamsFromTree(nChild, childRsn);
            }
        }
    }

    private void doEdit() {
        RTreeNode nSel = treParams.getTSelectionNode();
        if(nSel == null) {
            return;
        }

        if(nSel.get() instanceof NodeRuleSet) {
            NodeRuleSet uRuleSet = nSel.get();
            JFrame parent = GuiUtil.fra(PdfParametersPanel.this);
            RuleSetEditDialog dlg = new RuleSetEditDialog(parent,
                uRuleSet.getLevelLabel(), uRuleSet.getChildBulletPattern(), uRuleSet.getSiblingBulletPattern(), uRuleSet.getChildPatternReplace(), uRuleSet.getSiblingPatternReplace(),false);
            dlg.setVisible(true);
            if(dlg.getResult() == RuleSetEditDialog.OK) {
                String newLabel = dlg.getLevelLabel();
                if(alreadyUsed(newLabel, nSel)) {
                    Dialogs.showWarning(parent,
                        "This level label is used by another sibling rule set\n" +
                        "under their parent rule set.  It is recommended that\n" +
                        "you use unique level labels under a given parent rule\n" +
                        "set for clarity.",
                        "Level Label Warning"
                    );
                }
                uRuleSet.setLevelLabel(newLabel);
                uRuleSet.setChildBulletPattern(dlg.getChildBulletPattern());
                uRuleSet.setSiblingBulletPattern(dlg.getSiblingBulletPattern());
                uRuleSet.setChildPatternReplace(dlg.getChildPatternReplace());
                uRuleSet.setSiblingPatternReplace(dlg.getSiblingPatternReplace());
                treParams.updateUI();
                uiController.setDirty(true);
            }

        } else if(nSel.get() instanceof NodeRule) {
            NodeRule uRule = nSel.get();
            JFrame parent = GuiUtil.fra(PdfParametersPanel.this);
            RuleEditDialog dlg = new RuleEditDialog(parent, uRule.getRule(), false);
            dlg.setVisible(true);
            if(dlg.getResult() == RuleEditDialog.OK) {
                uRule.setRule(dlg.getRule());
                treParams.updateUI();
                uiController.setDirty(true);
            }
        }
    }

    private boolean alreadyUsed(String newLabel, RTreeNode nSel) {
        for(RTreeNode nSib : nSel.getRSiblings(NodeRuleSet.class)) {
            NodeRuleSet uRuleSet = nSib.get();
            if(uRuleSet.getLevelLabel().equals(newLabel)) {
                return true;
            }
        }
        return false;
    }

    public void setProcessButtonEnabled(boolean enabled) {
        btnProcess.setEnabled(enabled);
    }

    public void setExtractButtonEnabled(boolean enabled) {
        btnExtract.setEnabled(enabled);
    }

    private int countRuleNodes(RTreeNode nSel) {
        int pos = 0;
        for(; pos < nSel.getCount(); pos++) {
            if(!(nSel.getObjectAt(pos) instanceof NodeRule)) {
                break;
            }
        }
        return pos;
    }

    private void pasteRuleInto(RTreeNode nSel) {
        TModel model = treParams.getTModel();
        RTreeNode nNew = copyRuleNode(nCopiedRule);
        int pos = countRuleNodes(nSel);
        model.insertNodeInto(nNew, nSel, pos);
        uiController.setDirty(true);
    }

    private void pasteRuleSetInto(RTreeNode nSel) {
        TModel model = treParams.getTModel();
        RTreeNode nNew = copyRuleSetNode(nCopiedRuleSet);
        model.append(nSel, nNew);
        uiController.setDirty(true);
    }

    public RTreeNode copyRuleNode(RTreeNode nRule) {
        NodeRule uCurRule = (NodeRule) nRule.get();
        NodeRule uNewRule = new NodeRule(uCurRule.getRule().copy());
        return new RTreeNode(uNewRule);
    }

    public RTreeNode copyRuleSetNode(RTreeNode nRuleSet) {
        String levelLabel = ((NodeRuleSet) nRuleSet.get()).getLevelLabel();
        String childBulletPattern = ((NodeRuleSet) nRuleSet.get()).getChildBulletPattern();
        String siblingBulletPattern = ((NodeRuleSet) nRuleSet.get()).getSiblingBulletPattern();
        boolean childPatternReplace = ((NodeRuleSet) nRuleSet.get()).getChildPatternReplace();
        boolean siblingPatternReplace = ((NodeRuleSet) nRuleSet.get()).getSiblingPatternReplace();
        NodeRuleSet uNewRuleSet = new NodeRuleSet(levelLabel, childBulletPattern, childPatternReplace, siblingBulletPattern, siblingPatternReplace);
        RTreeNode nNewRuleSet = new RTreeNode(uNewRuleSet);

        for(RTreeNode nChild : nRuleSet) {
            if(nChild.get() instanceof NodeRuleSet) {
                nNewRuleSet.add(copyRuleSetNode(nChild));

            } else if(nChild.get() instanceof NodeRule) {
                nNewRuleSet.add(copyRuleNode(nChild));
            }
        }

        return nNewRuleSet;
    }

    public void addRuleFromExternal(Rule derivedRule) {
        JFrame parent = GuiUtil.fra(PdfParametersPanel.this);
        RuleEditDialog dlg = new RuleEditDialog(parent, derivedRule, true);
        dlg.setVisible(true);
        if(dlg.getResult() == RuleEditDialog.OK) {
            Rule editedRule = dlg.getRule();
            addRuleToTree(editedRule);
        }
    }

    private void addRuleToTree(Rule rule) {
        RTreeNode nSel = treParams.getTSelectionNode();
        if(nSel == null) {
            return;
        }
        NodeBase uSel = nSel.get();
        if(uSel instanceof NodeRoot) {
            return;
        }
        if(uSel instanceof NodeRule) {
            nSel = nSel.getRParent();
        }
        TModel model = treParams.getTModel();
        int pos = countRuleNodes(nSel);
        RTreeNode nNew = new RTreeNode(new NodeRule(rule));
        model.insertNodeInto(nNew, nSel, pos);
        treParams.expand(nSel);
        uiController.setDirty(true);
        treParams.select(nNew);
    }


    public boolean isExpanded() {
        return expanded;
    }

    ///////////////
    // NOTIFIERS //
    ///////////////

    private ChangeNotifier extractNotifier = new ChangeNotifier(this);
    public void addExtractListener(ChangeListener listener) {
        extractNotifier.addListener(listener);
    }
    private void fireExtractNotifier() {
        extractNotifier.fireStateChanged();
    }
    private ChangeNotifier processNotifier = new ChangeNotifier(this);
    public void addProcessListener(ChangeListener listener) {
        processNotifier.addListener(listener);
    }
    private void fireProcessNotifier() {
        processNotifier.fireStateChanged();
    }
    private ChangeNotifier expandNotifier = new ChangeNotifier(this);
    public void addExpandListener(ChangeListener listener) {
        expandNotifier.addListener(listener);
    }
    private void fireExpandNotifier() {
        expandNotifier.fireStateChanged();
    }
}
