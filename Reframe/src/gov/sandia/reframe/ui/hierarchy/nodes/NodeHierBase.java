package gov.sandia.reframe.ui.hierarchy.nodes;

import replete.ui.tree.NodeBase;

public abstract class NodeHierBase extends NodeBase {

    public abstract String getDisplayText();
    public abstract int getPage();
    public abstract boolean containsText(String strLower);

}
