package gov.sandia.reframe.ui.library;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import gov.sandia.cortext.fileparsing.pdf.HNode;
import gov.sandia.cortext.fileparsing.pdf.PdfExtractionParameters;
import gov.sandia.reframe.AppState;
import gov.sandia.reframe.ParseRecord;
import gov.sandia.reframe.Pdf;
import gov.sandia.reframe.ui.ExportResult;
import gov.sandia.reframe.ui.Exporter;
import gov.sandia.reframe.ui.UIController;
import gov.sandia.reframe.ui.images.ReframeImageModel;
import replete.event.ChangeNotifier;
import replete.hash.Md5Util;
import replete.io.FileUtil;
import replete.text.StringUtil;
import replete.ui.GuiUtil;
import replete.ui.button.RButton;
import replete.ui.fc.RFileChooser;
import replete.ui.fc.RFilterBuilder;
import replete.ui.images.concepts.CommonConcepts;
import replete.ui.lay.Lay;
import replete.ui.list.RList;
import replete.ui.panels.RPanel;
import replete.ui.windows.Dialogs;
import replete.ui.windows.notifications.msg.NotificationInfo;
import replete.util.OsUtil;

public class PdfLibraryPanel extends RPanel {


    ////////////
    // FIELDS //
    ////////////

    // Constants
    private static final File INIT_PATH = new File("C:\\Users\\dtrumbo\\work\\Data\\CCDE_UCNI\\DOE");

    //UI
    private RList<Pdf> lst;
    private DefaultListModel<Pdf> model;
    private RButton btnAdd;
    private RButton btnRemove;
    private RButton btnSetFiles;
    private RButton btnShow;
    private RButton btnExplorer;
    private RButton btnSystem;
    private RButton btnExportXml;
    private RButton btnExportDoc;
    private RButton btnImportDoc;
    private RButton btnHistory;
    private JLabel lblExpand;
    private JLabel lblCollapse;
    private JPanel pnlMin;
    private JPanel pnlFull;
    private boolean expanded = true;


    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public PdfLibraryPanel(final UIController uiController) {

        model = new DefaultListModel<>();
        for(Pdf pdf : AppState.getState().getPdfs()) {
            model.addElement(pdf);
        }

        pnlMin = Lay.BxL(
            Lay.lb(" ", CommonConcepts.LIBRARY, "size=14"),
            lblExpand = Lay.lb(ReframeImageModel.AR_GR_R,
                "cursor=hand,ttt=Expand"),
            "eb=5,dimw=26,bg=FFF4C1"
        );

        pnlFull = Lay.BL(
            "N", Lay.BL(
                "C", Lay.FL("L", "nogap",
                    Lay.lb("<html><u>Library</u></html>",
                        CommonConcepts.LIBRARY, "size=14"),
                    lblCollapse = Lay.lb(ReframeImageModel.AR_BL_L,
                        "cursor=hand,ttt=Collapse,eb=5l")
                )
            ),
            "C", Lay.BL(
                "C", Lay.p(Lay.sp(lst = Lay.lst()), "eb=5bt"),
                "E", Lay.BxL("Y",
                    Lay.p(btnAdd = Lay.btn(CommonConcepts.ADD, 2), "eb=5tl,alignx=0.5,maxH=20"),
                    Lay.p(btnRemove = Lay.btn(CommonConcepts.REMOVE, 2), "eb=5tl,alignx=0.5,maxH=20"),
                    Lay.p(btnSetFiles = Lay.btn(CommonConcepts.EDIT, 2), "eb=5tl,alignx=0.5,maxH=20"),
                    Box.createVerticalStrut(10),
                    Lay.p(btnShow = Lay.btn(CommonConcepts.SEARCH, 2), "eb=5tl,alignx=0.5,maxH=20"),
                    Lay.p(btnSystem = Lay.btn(ReframeImageModel.READER, 2), "eb=5tl,alignx=0.5,maxH=20"),
                    Lay.p(btnExplorer = Lay.btn(ReframeImageModel.EXPLORER, 2), "eb=5tl,alignx=0.5,maxH=20"),
                    Box.createVerticalStrut(10),
                    Lay.p(btnExportXml = Lay.btn(CommonConcepts.XML, 2), "eb=5tl,alignx=0.5,maxH=20"),
                    Lay.p(btnExportDoc = Lay.btn(CommonConcepts.EXPORT, 2), "eb=5tl,alignx=0.5,maxH=20"),
                    Lay.p(btnImportDoc = Lay.btn(CommonConcepts.IMPORT, 2), "eb=5tl,alignx=0.5,maxH=20"),
                    Box.createVerticalStrut(10),
                    Lay.p(btnHistory = Lay.btn(CommonConcepts.DATE_TIME, 2), "eb=5tl,alignx=0.5,maxH=20"),
                    Box.createVerticalGlue()
                )
            ),
            "eb=5,prefw=250,bg=FFF4C1,chtransp"  // chtransp will affect list if it already has its model
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

        lst.setModel(model);

        btnAdd.setToolTipText("Add Documents...");
        btnRemove.setToolTipText("Remove Documents");
        btnSetFiles.setToolTipText("Set Document File Paths...");
        btnShow.setToolTipText("Show Documents");
        btnSystem.setToolTipText("Show With System Editor");
        btnExplorer.setToolTipText("Show In Windows Explorer");
        btnExportXml.setToolTipText("Export Tarjetas XML...");
        btnExportDoc.setToolTipText("Export Documents...");
        btnImportDoc.setToolTipText("Import Documents...");
        btnHistory.setToolTipText("Show Parse History");

        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RFileChooser chooser = RFileChooser.getChooser("Add PDF Documents", true);
                chooser.setCurrentDirectory(INIT_PATH);
                RFilterBuilder builder = new RFilterBuilder(chooser, false);
                builder.append("PDF Documents (*.pdf)", "pdf");
                JFrame parent = GuiUtil.fra(PdfLibraryPanel.this);
                if(chooser.showOpen(parent)) {
                    List<Integer> addedIndxs = new ArrayList<>();
                    List<File> alreadyInModel = new ArrayList<>();

                    for(File file : chooser.getSelectedFilesResolved()) {
                        for(int p = 0; p < model.getSize(); p++) {
                            if(model.get(p).getFile().equals(file)) {
                                alreadyInModel.add(file);
                            }
                        }
                        Pdf newPdf = new Pdf(file);
                        newPdf.setExtractionParams(new PdfExtractionParameters());
                        model.addElement(newPdf);
                        addedIndxs.add(model.getSize() - 1);
                        AppState.getState().getPdfs().add(newPdf);
                        NotificationInfo info = new NotificationInfo()
                            .setTitle("Added new document \"" + file + "\".");
                        uiController.addNotification(info);
                        uiController.setDirty(true);
                    }

                    int[] i = new int[addedIndxs.size()];
                    for(int x = 0; x < addedIndxs.size(); x++) {
                        i[x] = addedIndxs.get(x);
                    }
                    lst.setSelectedIndices(i);

                    if(!alreadyInModel.isEmpty()) {
                        String str = "";
                        for(File f : alreadyInModel) {
                            str += "      " + f + "\n";
                        }
                        Dialogs.showWarning(uiController.getParent(),
                            "The following files already appear to be in your library.\n" +
                            "This may or may not be what you want to do.\n\n" +
                            str, "Warning");
                    }
                }
            }
        });

        btnRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selCount = lst.getSelectedIndices().length;
                if(selCount == 0) {
                    return;
                }
                String msg;
                if(selCount == 1) {
                    msg = "Are you sure you want to delete this document?";
                } else {
                    msg = "Are you sure you want to delete these " + selCount + " documents?";
                }
                String sep = "\n       ";
                msg += " This will also delete all associated parameters and hierarchy information.\n" + sep;
                for(Pdf pdf : lst.getSelectedValuesList()) {
                    msg += pdf.getTitle() + sep;
                }
                JFrame parent = GuiUtil.fra(PdfLibraryPanel.this);
                if(Dialogs.showConfirm(parent, msg, "Delete?", true)) {
                    List<Pdf> selpdfs = new ArrayList<Pdf>(lst.getSelectedValuesList());
                    // Call a notifier so that tabs get deleted
                    fireCloseNotifier();
                    // Delete the pdf from the GUI pdf list
                    for (Pdf pdf : selpdfs){
                        model.removeElement(pdf);
                    }
                    // Remove the pdf from the application state.
                    AppState.getState().getPdfs().removeAll(selpdfs);
                    uiController.setDirty(true);
                }
            }
        });
        btnSetFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selCount = lst.getSelectedIndices().length;
                if(selCount == 1) {
                    RFileChooser chooser = RFileChooser.getChooser("Set Document Path");
                    chooser.setApproveButtonText("Set Path");
                    chooser.setApproveButtonMnemonic('S');
                    chooser.setCurrentDirectory(INIT_PATH);
                    RFilterBuilder builder = new RFilterBuilder(chooser, false);
                    builder.append("PDF Documents (*.pdf)", "pdf");
                    JFrame parent = GuiUtil.fra(PdfLibraryPanel.this);
                    if(chooser.showOpen(parent)) {
                        File chosenFile = chooser.getSelectedFileResolved();
                        Pdf pdf = lst.getSelectedValue();
                        if(!checkMd5Continue(parent, chosenFile, pdf)) {
                            return;
                        }
                        pdf.setFile(chosenFile);
                        uiController.setDirty(true);
                    }
                } else {
                    RFileChooser chooser = RFileChooser.getChooser("Set Document Paths Base Directory",
                        JFileChooser.DIRECTORIES_ONLY);
                    chooser.setApproveButtonText("Set Base Directory");
                    chooser.setApproveButtonMnemonic('S');
                    chooser.setCurrentDirectory(INIT_PATH);
                    RFilterBuilder builder = new RFilterBuilder(chooser, false);
                    builder.append("PDF Documents (*.pdf)", "pdf");
                    JFrame parent = GuiUtil.fra(PdfLibraryPanel.this);
                    if(chooser.showOpen(parent)) {
                        File dir = chooser.getSelectedFileResolved();
                        boolean atLeastOneChanged = false;
                        for(Pdf pdf : lst.getSelectedValuesList()) {
                            File newFile = new File(dir, pdf.getFile().getName());
                            if(!checkMd5Continue(parent, newFile, pdf)) {
                                continue;
                            }
                            pdf.setFile(newFile);
                            atLeastOneChanged = true;
                        }
                        if(atLeastOneChanged) {
                            uiController.setDirty(true);
                        }
                    }
                }
            }
        });
        btnShow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireOpenNotifier();
            }
        });
        btnExplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(OsUtil.isWindows()) {
                    for(Pdf pdf : lst.getSelectedValuesList()) {
                        if(FileUtil.isReadableFile(pdf.getFile())) {
                            OsUtil.openExplorer(pdf.getFile(), true);
                        } else {
                            Dialogs.showWarning(uiController.getParent(),
                                "The following file cannot be shown in Windows Explorer\n" +
                                "because it does not exist:\n\n    " + pdf.getFile().getAbsolutePath(),
                                "Warning");
                        }
                    }
                } else {
                    Dialogs.showWarning(uiController.getParent(),
                        "This feature is only available on Windows.",
                        "Sorry...");
                }
            }
        });
        btnSystem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(OsUtil.isWindows() || OsUtil.isMac()) {
                    for(Pdf pdf : lst.getSelectedValuesList()) {
                        if(FileUtil.isReadableFile(pdf.getFile())) {
                            OsUtil.openSystemEditor(pdf.getFile());
                        } else {
                            Dialogs.showWarning(uiController.getParent(),
                                "The following file cannot be shown in with the system editor\n" +
                                "because it does not exist:\n\n    " + pdf.getFile().getAbsolutePath(),
                                "Warning");
                        }
                    }
                } else {
                    Dialogs.showWarning(uiController.getParent(),
                        "This feature is only available on Windows or Mac.",
                        "Sorry...");
                }
            }
        });
        btnExportXml.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RFileChooser chooser = RFileChooser.getChooser("Export Tarjetas XML to Directory", JFileChooser.DIRECTORIES_ONLY);
                chooser.setCurrentDirectory(INIT_PATH);
                JFrame parent = GuiUtil.fra(PdfLibraryPanel.this);
                if(chooser.showSave(parent)) {
                    File dir = chooser.getSelectedFile();
                    int performed = 0;
                    int nodeCount = 0;
                    for(Pdf pdf : lst.getSelectedValuesList()) {
                        if(pdf.getResult() != null &&
                                pdf.getResult().getHierarchy() != null &&
                                pdf.getResult().getHierarchy().getRoot() != null) {
                            try {
                                ExportResult result = Exporter.export(dir, pdf);
                                performed++;
                                nodeCount += result.getNodeCount();

                                List<HNode> warningNodes = result.getWarningNodes();
                                if(!warningNodes.isEmpty()) {
                                    String msg = "";
                                    for(HNode node : warningNodes) {
                                        msg += node.toSimpleString() + "\n";
                                    }
                                    int w = warningNodes.size();
                                    String s = StringUtil.s(w);
                                    Dialogs.showWarning(parent,
                                        "The following " + w + " node" + s + " neither have child nodes\n" +
                                        "nor any text content.  They were written to the\n" +
                                        "XML but this may be an error.\n\n" + msg,
                                        "Empty Leaf Nodes"
                                    );
                                }
                            } catch(Exception ex) {
                                Dialogs.showDetails(parent, "An error occurred exporting:\n\n      " + pdf.getFile(), "Export Error", ex);
                            }
                        }
                    }

                    int notPerformed = lst.getSelectedIndices().length - performed;
                    String s1 = StringUtil.s(performed);
                    String s2 = StringUtil.s(notPerformed);
                    String extra = notPerformed == 0 ? "" :
                        "\n\nDocuments that were not exported\n" +
                        "may not have been parsed yet.";

                    int response = Dialogs.showMulti(parent,
                        performed + " document" + s1 + " exported.\n" +
                        notPerformed + " document" + s2 + " NOT exported.\n" +
                        nodeCount + " total nodes exported." +
                        extra,
                        "Export Complete", new String[] {"OK", "Open &Directory"},
                        JOptionPane.INFORMATION_MESSAGE
                    );

                    if(response == 1) {
                        OsUtil.openExplorer(dir);
                    }
                }
            }
        });
        btnExportDoc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uiController.exportPdfs();
            }
        });
        btnImportDoc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uiController.importPdfs();
            }
        });
        btnHistory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uiController.showHistory();
            }
        });

        lst.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateButtonEnabledness();
            }
        });

        lst.addDoubleClickListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                fireOpenNotifier();
            }
        });

        updateButtonEnabledness();

        uiController.addRefreshListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                model.clear();
                for(Pdf pdf : AppState.getState().getPdfs()) {
                    model.addElement(pdf);
                }
            }
        });
    }

    private boolean checkMd5Continue(JFrame parent, File chosenFile, Pdf pdf) {
        if(!pdf.getParseHistory().isEmpty()) {
            ParseRecord lastRecord = pdf.getParseHistory().get(pdf.getParseHistory().size() - 1);
            if(lastRecord.getPdfMd5Hash() != null) {
                if(chosenFile.exists()) {
                    String chosenMd5 = Md5Util.getMd5(chosenFile);
                    if(!chosenMd5.equals(lastRecord.getPdfMd5Hash())) {
                        if(!Dialogs.showConfirm(parent,
                                "The selected file does not have the same MD5 " +
                                "hash as the most recent file parsed according " +
                                "to the parse history.\n\nSelected File:\n    Name: " + chosenFile.getName() + "\n    MD5: " +
                                chosenMd5 + "\n\nLast Parsed:\n    Name: " + "<NAME?>" + "\n    MD5: " + lastRecord.getPdfMd5Hash() +
                                "\n\nDo you wish to continue anyway?",
                                        "Possible Incorrect File", true)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }


    //////////////////////////
    // ACCESSORS / MUTATORS //
    //////////////////////////

    // Accessors

    public boolean isExpanded() {
        return expanded;
    }
    public List<Pdf> getSelected() {
        return lst.getSelectedValuesList();
    }

    // Mutators

    public void setSelectedDocument(Pdf pdf) {
        lst.setSelectedValue(pdf, true);
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
    private void updateButtonEnabledness() {
        boolean enabled = lst.getSelectedIndex() != -1;
        btnRemove.setEnabled(enabled);
        btnSetFiles.setEnabled(enabled);
        btnShow.setEnabled(enabled);
        btnSystem.setEnabled(enabled);
        btnExplorer.setEnabled(enabled);
        btnExportXml.setEnabled(enabled);
        btnExportDoc.setEnabled(enabled);
        btnHistory.setEnabled(enabled);
    }


    ///////////////
    // NOTIFIERS //
    ///////////////

    private ChangeNotifier openNotifier = new ChangeNotifier(this);
    public void addOpenListener(ChangeListener listener) {
        openNotifier.addListener(listener);
    }
    private void fireOpenNotifier() {
        openNotifier.fireStateChanged();
    }

    private ChangeNotifier closeNotifier = new ChangeNotifier(this);
    public void addCloseListener(ChangeListener listener) {
        closeNotifier.addListener(listener);
    }
    private void fireCloseNotifier() {
        closeNotifier.fireStateChanged();
    }
    private ChangeNotifier expandNotifier = new ChangeNotifier(this);
    public void addExpandListener(ChangeListener listener) {
        expandNotifier.addListener(listener);
    }
    private void fireExpandNotifier() {
        expandNotifier.fireStateChanged();
    }
}
