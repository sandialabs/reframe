package gov.sandia.reframe.ui.params.nodes;

import javax.swing.Icon;

import replete.ui.images.concepts.CommonConcepts;
import replete.ui.images.concepts.ImageLib;
import replete.ui.tree.NodeBase;

public class NodeRoot extends NodeBase {

    @Override
    public Icon getIcon(boolean expanded) {
        return ImageLib.get(CommonConcepts.FILE);
    }

    @Override
    public boolean isCollapsible() {
        return false;
    }

    @Override
    public String toString() {
        return "Top-Level Rule Sets";
    }
}
