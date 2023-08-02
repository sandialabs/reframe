package gov.sandia.reframe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import replete.xstream.XStreamWrapper;

public class AppState {

//    public static File appDir = new File(User.getHome(), ".reframe");
//    public static File appFile = new File(appDir, "state.xml");


    ///////////////
    // SINGLETON //
    ///////////////

    private static AppState state = new AppState();
    public static AppState getState() {
        return state;
    }


    ////////////
    // FIELDS //
    ////////////

    private File libraryFile;
    private List<Pdf> pdfs;
    private boolean dirty;


    /////////////////
    // CONSTRUCTOR //
    /////////////////

    private AppState() {
        pdfs = new ArrayList<>();
    }


    //////////////////////////
    // ACCESSORS / MUTATORS //
    //////////////////////////

    // Accessors

    public File getLibraryFile() {
        return libraryFile;
    }
    public List<Pdf> getPdfs() {
        return pdfs;
    }
    public boolean isLibraryDirty() {
        return dirty;
    }

    // Mutators

    public void setLibraryFile(File libraryFile) {
        this.libraryFile = libraryFile;
    }
    public void setPdfs(List<Pdf> pdfs) {
        this.pdfs = pdfs;
    }
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }


    //////////////////
    // READ / WRITE //
    //////////////////

    public static void saveLibrary() {
//        appDir.mkdirs();
        try {
//            if(appFile.exists()) {
//                appFile.renameTo(new File(appFile.getParentFile(), "state-previous.xml"));
//            }
            XStreamWrapper.writeToFile(state, state.getLibraryFile());
            state.setDirty(false);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void readLibrary(File target) throws Exception {
        state = (AppState) XStreamWrapper.loadTarget(target);
        state.setLibraryFile(target);
        state.setDirty(false);

        for(Pdf pdf : AppState.getState().getPdfs()) {
            if(pdf.getResult() != null && pdf.getResult().getHierarchy() != null && pdf.getResult().getHierarchy().getRoot() != null) {
                pdf.getResult().getHierarchy().getRoot().fixRedacted();
            }
        }
    }
}
