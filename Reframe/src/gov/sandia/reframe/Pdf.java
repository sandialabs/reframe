package gov.sandia.reframe;

import java.io.File;

import gov.sandia.cortext.fileparsing.pdf.ExtractionResult;
import gov.sandia.cortext.fileparsing.pdf.PdfExtractionParameters;
import gov.sandia.cortext.fileparsing.pdf.PdfParseParameters;
import gov.sandia.cortext.fileparsing.pdf.PdfParseResult;

public class Pdf {


    ////////////
    // FIELDS //
    ////////////

    private File file;
    private PdfExtractionParameters eParams;
    private transient ExtractionResult extraction;
    private PdfParseParameters params;
    private PdfParseResult result; // Result of "parse" method
    private transient PdfParseResult prevResult;
    private ParseHistory parseHistory;


    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public Pdf(File file) {
        this.file = file;
    }


    //////////////////////////
    // ACCESSORS / MUTATORS //
    //////////////////////////

    // Accessors

    public File getFile() {
        return file;
    }
    public PdfParseParameters getParams() {
        return params;
    }
    public ExtractionResult getExtractionResult() {
        return this.extraction;
    }
    public PdfExtractionParameters getExtractionParams() {
        return eParams;
    }
    public PdfParseResult getResult() {
        return result;
    }
    public PdfParseResult getPrevResult() {
        return prevResult;
    }
    public ParseHistory getParseHistory() {
        if(parseHistory == null) {
            parseHistory = new ParseHistory();
        }
        return parseHistory;
    }
    
    // Accessors (Computed)

    public String getTitle() {
        return file.getName();
    }

    // Mutators

    public void setFile(File file) {          // For portability between systems
        this.file = file;
    }
    public void setParams(PdfParseParameters params) {
        this.params = params;
    }
    public void setExtractionParams(PdfExtractionParameters eParams) {
        this.eParams = eParams;
    }
    public void setResult(PdfParseResult result) {
        prevResult = this.result;
        this.result = result;
    }
    public void setExtractionResult(ExtractionResult extraction) {
        this.extraction = extraction;
    }
    public void clearPrevResult() {
        prevResult = null;
        System.gc();
    }


    ////////////////
    // OVERRIDDEN //
    ////////////////

    @Override
    public String toString() {
        return getTitle();
    }


    ///////////////////
    // SERIALIZATION //
    ///////////////////

    private Object readResolve() {
        // Copy legacy fields from PdfParseParameters to PdfExtractionParameters.
        if(eParams == null && params != null) {
            eParams = new PdfExtractionParameters()
                          .setMaxPage(params.getMaxPage())
                          .setMaxXAllowed(params.getMaxXAllowed())
                          .setMaxYAllowed(params.getMaxYAllowed())
                          .setMinXAllowed(params.getMinXAllowed())
                          .setMinYAllowed(params.getMinYAllowed());
        }
        return this;
    }
}
