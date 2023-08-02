package gov.sandia.reframe;

public class ParseRecord {


    ////////////
    // FIELDS //
    ////////////

    private String user;              // Populated from OS
    private long started;             // Populated from RWorker
    private long ended;               // Populated from RWorker
    private int nodeCount;            // Populated from PDF parse result's hierarchy
    private int nodeCountNonBlank;    // Populated from PDF parse result's hierarchy
    private Exception error;          // Caught in RWorker completion
    private String pdfMd5Hash;        // Populated from RWorker
    // Future: PDF Path


    //////////////////////////
    // ACCESSORS / MUTATORS //
    //////////////////////////

    // Accessors

    public String getUser() {
        return user;
    }
    public long getStarted() {
        return started;
    }
    public long getEnded() {
        return ended;
    }
    public int getNodeCount() {
        return nodeCount;
    }
    public int getNodeCountNonBlank() {
        return nodeCountNonBlank;
    }
    public Exception getError() {
        return error;
    }
    public String getPdfMd5Hash() {
        return pdfMd5Hash;
    }

    // Mutators (Builder)

    public ParseRecord setUser(String user) {
        this.user = user;
        return this;
    }
    public ParseRecord setStarted(long started) {
        this.started = started;
        return this;
    }
    public ParseRecord setEnded(long ended) {
        this.ended = ended;
        return this;
    }
    public ParseRecord setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
        return this;
    }
    public ParseRecord setNodeCountNonBlank(int nodeCountNonBlank) {
        this.nodeCountNonBlank = nodeCountNonBlank;
        return this;
    }
    public ParseRecord setError(Exception error) {
        this.error = error;
        return this;
    }
    public ParseRecord setPdfMd5Hash(String pdfMd5Hash) {
        this.pdfMd5Hash = pdfMd5Hash;
        return this;
    }
}
