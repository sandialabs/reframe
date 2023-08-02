package gov.sandia.reframe.ui;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.event.ChangeListener;

import gov.sandia.reframe.AppState;
import gov.sandia.reframe.Pdf;
import replete.event.ChangeNotifier;
import replete.io.FileUtil;
import replete.ui.fc.RFileChooser;
import replete.ui.fc.RFilterBuilder;
import replete.ui.windows.Dialogs;
import replete.ui.windows.notifications.msg.NotificationInfo;
import replete.xstream.XStreamWrapper;

public class UIController {


    ////////////
    // FIELDS //
    ////////////

    // Constants
    private static final File INIT_PATH = new File("C:\\Users\\dtrumbo\\work\\Data\\CCDE_UCNI");
    private ReframeFrame parent;


    public void toggleView() {
        parent.toggleView();
    }

    public ReframeFrame getParent() {
        return parent;
    }
    public void setParent(ReframeFrame parent) {
        this.parent = parent;
        parent.updateTitle();
    }

    public void exit() {
        parent.close();
    }

    public void addNotification(NotificationInfo info) {
        parent.getNotificationModel().getInfos().add(info);
    }

    public void notImpl() {
        Dialogs.showWarning(parent, "This feature is not yet implemented.", "Sorry!");
    }


    //////////
    // FILE //
    //////////

    public void newLibrary() {
        if(checkSave()) {
            AppState.getState().setDirty(false);           // Not dirty because nothing to lose yet!
            AppState.getState().setLibraryFile(null);
            AppState.getState().setPdfs(new ArrayList<Pdf>());

            fireRefreshNotifier();
            parent.updateTitle();
        }
    }
    public void openLibrary() {
        RFileChooser chooser = RFileChooser.getChooser("Open Library");
        chooser.setCurrentDirectory(INIT_PATH);
        RFilterBuilder builder = new RFilterBuilder(chooser, false);
        builder.append("Reframe Libraries (*.rlib)", "rlib");
        if(chooser.showOpen(parent)) {
            if(checkSave()) {
                try {
                    AppState.readLibrary(chooser.getSelectedFile());
                    fireRefreshNotifier();
                } catch(Exception e) {
                    Dialogs.showDetails(parent, "An error has occurred opening the library.", "Error", e);
                }
                parent.updateTitle();
            }
        }
    }
    public boolean saveLibrary() {
        if(AppState.getState().getLibraryFile() == null) {
            return saveLibraryAs();
        }
        parent.saveParameters();
        AppState.saveLibrary();
        parent.updateTitle();
        return true;
    }
    public boolean saveLibraryAs() {
        RFileChooser chooser = RFileChooser.getChooser("Save Library");
        chooser.setCurrentDirectory(INIT_PATH);
        RFilterBuilder builder = new RFilterBuilder(chooser, false);
        builder.append("Reframe Libraries (*.rlib)", "rlib");
        if(chooser.showSave(parent)) {
            AppState.getState().setLibraryFile(chooser.getSelectedFile());
            parent.saveParameters();
            AppState.saveLibrary();
            parent.updateTitle();
            return true;
        }
        return false;
    }
    public boolean checkSave() {
        if(AppState.getState().isLibraryDirty()) {
            int choice = Dialogs.showMulti(parent,
                "Do you want to save changes to this library?",
                "Save Library?", new String[] {"&Yes", "&No", "&Cancel"});
            if(choice == 0) {
                return saveLibrary();
            } else if(choice == 1) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }
    public void setDirty(boolean dirty) {
        AppState.getState().setDirty(dirty);
        parent.updateTitle();
    }

    public void importPdfs() {
        RFileChooser chooser = RFileChooser.getChooser("Import Reframe Documents", true);
        chooser.setCurrentDirectory(INIT_PATH);
        RFilterBuilder builder = new RFilterBuilder(chooser, false);
        builder.append("Reframe Documents (*.rdoc)", "rdoc");
        if(chooser.showOpen(parent)) {
            boolean added = false;
            for(File f : chooser.getSelectedFilesResolved()) {
                try {
                    Pdf pdf = (Pdf) XStreamWrapper.loadTarget(f);
                    AppState.getState().getPdfs().add(pdf);
                    added = true;
                } catch(Exception e) {
                    Dialogs.showDetails(parent, "An error occurred importing document from file.\n\n" +
                        "Source: " + f, "Error", e);
                }
            }
            if(added) {
                parent.saveParameters();
                fireRefreshNotifier();
                setDirty(true);
            }
        }
    }

    public void exportPdfs() {
        RFileChooser chooser = RFileChooser.getChooser(
            "Export Reframe Documents To Directory", false,
            JFileChooser.DIRECTORIES_ONLY);
        chooser.setCurrentDirectory(INIT_PATH);
        RFilterBuilder builder = new RFilterBuilder(chooser, false);
        builder.append("Reframe Documents (*.rdoc)", "rdoc");
        if(chooser.showOpen(parent)) {
            for(Pdf pdf : parent.getSelected()) {
                String name = FileUtil.getNameWithoutExtension(pdf.getFile()) + ".rdoc";
                File target = new File(chooser.getSelectedFile(), name);
                if(target.exists()) {
                    if(!Dialogs.showConfirm(parent, "The file\n\n      " + target +
                            "\n\nalready exists.  Overwrite?", "Overwrite?", true)) {
                        continue;
                    }
                }
                try {
                    XStreamWrapper.writeToFile(pdf, target);
                } catch(Exception e) {
                    Dialogs.showDetails(parent, "An error occurred exporting document to file.\n\n" +
                        "PDF: " + pdf.getFile() + "\nDestination: " + target, "Error", e);
                }
            }
        }
    }

    public void setSelectedTab(Pdf pdf) {
        parent.setSelectedTab(pdf);
    }


    //////////////
    // NOTIFIER //
    //////////////

    private ChangeNotifier refreshNotifier = new ChangeNotifier(this);
    public void addRefreshListener(ChangeListener listener) {
        refreshNotifier.addListener(listener);
    }
    private void fireRefreshNotifier() {
        refreshNotifier.fireStateChanged();
    }

    public void showHistory() {
        if(!parent.getSelected().isEmpty()) {
            Pdf pdf = parent.getSelected().get(0);
            HistoryDialog dlg = new HistoryDialog(parent, pdf);
            dlg.setVisible(true);
        }
    }
}
