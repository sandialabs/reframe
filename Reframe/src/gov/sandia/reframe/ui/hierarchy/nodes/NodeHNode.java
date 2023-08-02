package gov.sandia.reframe.ui.hierarchy.nodes;

import javax.swing.Icon;

import gov.sandia.cortext.fileparsing.pdf.HNode;
import gov.sandia.reframe.ui.images.ReframeImageModel;
import replete.ui.images.concepts.ImageLib;

public class NodeHNode extends NodeHierBase {


    ////////////
    // FIELDS //
    ////////////

    private HNode node;


    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public NodeHNode(HNode node) {
        this.node = node;
    }


    //////////////////////////
    // ACCESSORS / MUTATORS //
    //////////////////////////

    // Accessors

    public HNode getNode() {
        return node;
    }
    public HNode getNodeOverridden() {                      // Convenience
        return node.getOverrideNode();
    }

    // Mutators

    public void setNodeOverridden(HNode overriddenNode) {   // Convenience
        node.setOverrideNode(overriddenNode);
    }


    ////////////////
    // OVERRIDDEN //
    ////////////////

    @Override
    public String getDisplayText() {
        return getNodeOverridden() != null ? getNodeOverridden().getText() : node.getText();
    }
    @Override
    public int getPage() {
        return node.getPage();
    }
    @Override
    public boolean containsText(String strLower) {
        return node.containsText(strLower) || (getNodeOverridden() != null && getNodeOverridden().containsText(strLower));
    }

    @Override
    public boolean isCollapsible() {
        return true;
    }

    @Override
    public Icon getIcon(boolean expanded) {
        if(node.getParseExceptionFlags() != null && !node.getParseExceptionFlags().isEmpty()) {
            return
                getNodeOverridden() != null ?
                    ImageLib.get(ReframeImageModel.NODE_OVER_EXCEPTION) :
                    ImageLib.get(ReframeImageModel.NODE_EXCEPTION);
        }
        return
            (getNodeOverridden() != null ?
                (getNodeOverridden().isRemoveFromExport() ?
                    ImageLib.get(ReframeImageModel.DIFF) :
                        ImageLib.get(ReframeImageModel.NODE_OVER)
                ) :
                ImageLib.get(ReframeImageModel.NODE));
    }

    @Override
    public String toString() {
        return getNodeOverridden() != null ? getNodeOverridden().toString() : node.toString();
    }
}
