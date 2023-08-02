package gov.sandia.reframe.ui.hierarchy.nodes;

import javax.swing.Icon;

import gov.sandia.reframe.ui.images.ReframeImageModel;
import replete.ui.images.concepts.ImageLib;

public class NodeDeltaNodeTrace extends NodeHierBase {


    ////////////
    // FIELDS //
    ////////////

    private String msg;


    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public NodeDeltaNodeTrace(String msg) {
        this.msg = msg;
    }


    ///////////////
    // ACCESSORS //
    ///////////////

    public String getMsg() {
        return msg;
    }

    private String parse(String msg) {
        String iElem = "<i>";
        String end = "</i></html>";
        int iPos = msg.indexOf(iElem);
        return msg.substring(iPos + iElem.length(), msg.length() - end.length());
    }


    ////////////////
    // OVERRIDDEN //
    ////////////////

    @Override
    public String getDisplayText() {
        return parse(msg);
    }
    @Override
    public int getPage() {
        return 0;
    }
    @Override
    public boolean containsText(String strLower) {
        return parse(msg).toLowerCase().contains(strLower);
    }

    @Override
    public Icon getIcon(boolean expanded) {
        return ImageLib.get(ReframeImageModel.TRACE);
    }

    @Override
    public String toString() {
        return msg;
    }
}
