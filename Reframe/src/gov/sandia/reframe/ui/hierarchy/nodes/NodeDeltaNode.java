package gov.sandia.reframe.ui.hierarchy.nodes;

import javax.swing.Icon;

import gov.sandia.reframe.ui.hierarchy.DeltaNode;
import gov.sandia.reframe.ui.images.ReframeImageModel;
import replete.ui.images.concepts.ImageLib;

public class NodeDeltaNode extends NodeHierBase {


    ////////////
    // FIELDS //
    ////////////

    private DeltaNode node;
    private boolean diffChild;


    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public NodeDeltaNode(DeltaNode node) {
        this.node = node;
    }


    ///////////////
    // ACCESSORS //
    ///////////////

    public DeltaNode getNode() {
        return node;
    }


    ////////////////
    // OVERRIDDEN //
    ////////////////

    @Override
    public String getDisplayText() {
        return node.getDisplayText();
    }
    @Override
    public int getPage() {
        return node.getPage();
    }
    @Override
    public boolean containsText(String strLower) {
        return node.containsText(strLower);
    }

    @Override
    public boolean isCollapsible() {
        return true;
    }

    public void setDiffChild(boolean diffChild) {
        this.diffChild = diffChild;
    }

    @Override
    public Icon getIcon(boolean expanded) {
        if(!node.isSame()) {
            return ImageLib.get(ReframeImageModel.DIFF);
        }
        if(diffChild) {
            return ImageLib.get(ReframeImageModel.DIFF_CH);
        }
        return ImageLib.get(ReframeImageModel.SAME);
    }

    @Override
    public String toString() {
        return node.toString();
    }
}
