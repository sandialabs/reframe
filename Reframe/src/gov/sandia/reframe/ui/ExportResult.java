package gov.sandia.reframe.ui;

import java.util.List;

import gov.sandia.cortext.fileparsing.pdf.HNode;

public class ExportResult {


    ////////////
    // FIELDS //
    ////////////

    private String xmlContent;
    private int nodeCount;
    private List<HNode> warningNodes;


    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public ExportResult(String xmlContent, int nodeCount, List<HNode> warningNodes) {
        this.xmlContent = xmlContent;
        this.nodeCount = nodeCount;
        this.warningNodes = warningNodes;
    }


    ///////////////
    // ACCESSORS //
    ///////////////

    public String getXmlContent() {
        return xmlContent;
    }
    public int getNodeCount() {
        return nodeCount;
    }
    public List<HNode> getWarningNodes() {
        return warningNodes;
    }
}
