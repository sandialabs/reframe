package gov.sandia.reframe.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import gov.sandia.cortext.fileparsing.pdf.HNode;
import gov.sandia.reframe.Pdf;
import replete.io.FileUtil;
import replete.text.Joiner;
import replete.text.StringUtil;

public class Exporter {
    private static final String PATH_SEP = "~";

    public static ExportResult export(File dir, Pdf pdf) {
        String docName = FileUtil.getNameWithoutExtension(pdf.getFile());
        File xmlFile = new File(dir, docName + ".xml");
        ExportResult result = createContent(pdf);
        FileUtil.writeTextContent(xmlFile, result.getXmlContent());
        return result;
    }

    protected static ExportResult createContent(Pdf pdf) {
        StringBuilder builder = new StringBuilder();
        String fileName = pdf.getFile().getName();
        // TODO better way to do this?
        fileName = fileName.replace(".pdf", "");
        String docName = FileUtil.getNameWithoutExtension(pdf.getFile());
        HNode titleNode = pdf.getResult().getHierarchy().getRoot();
        String title = titleNode.getCombinedTitleForExport();
        String guid = UUID.randomUUID().toString();
        builder.append("<document name=\"" + fileName + "\" title=\"" + title + "\" guid=\"" + guid + "\">\n\n");
        List<HNode> path = new ArrayList<>();
        List<HNode> warningNodes = new ArrayList<>();
        int nodeCount = appendNode(builder, pdf.getResult().getHierarchy().getRoot(), path, docName, warningNodes);
        builder.append("</document>\n");
        return new ExportResult(builder.toString(), nodeCount, warningNodes);
    }

    private static int appendNode(StringBuilder builder,
                                   HNode node, List<HNode> path,
                                   String docName, List<HNode> warningNodes) {
        int nodeCount = 0;

        path.add(node);

        String text = node.getCleanedTextForExport();

        // If the node hasn't been asked to be removed from the output...
        if(!node.isRemoveFromExportFromExport()) {

            boolean isLeaf = node.getChildren().isEmpty();
            boolean isBlank = StringUtil.isBlank(text);

            // If the node has text or if it is a leaf, output to the XML.
            if(!isBlank || isLeaf) {

                String guid = UUID.randomUUID().toString();    //"(test-mode)"
                List<HNode> nodes = new ArrayList<>(path.subList(1, path.size()));

                // TODO This is a hack until we support paragraphs at the parser level.
                nodes.add(new HNode().setBullet("Paragraph 1"));

                Joiner J = new Joiner()
                    .setItems(nodes)
                    .setMid(PATH_SEP)
                    .setAccessorMethod("getCombinedTitleForExport");

                String pathStr = null;
//                try {
                    pathStr = J.joinList();   // Not sure why reflection is intermittently failing.
//                } catch(Exception e) {
//                    e.printStackTrace();
//                    pathStr = J.joinList();
//                    pathStr = "[[ERROR]]";
//                }


                if (isBlank) {
                    text = "[No Text]";
                }

                String hier = "Library" + PATH_SEP + docName + PATH_SEP + pathStr;
                builder.append("  <paragraph guid=\"" + guid + "\" ");
                builder.append("relevant=\"True\" ");
                builder.append("pages=\"" + node.getPage() + "\" ");
                builder.append("hierarchy=\"" + StringUtil.cleanXmlAttr(hier) + "\" ");
                builder.append("text=\"" + StringUtil.cleanXmlAttr(text) + "\"/>\n\n");
                // We need better & replacement.  If there's already a &quot; in the text, prolly shouldn't replace it....

                nodeCount++;
            }

            // Keep track of blank leaves.
            if(isBlank && isLeaf) {
                warningNodes.add(node);
            }
        }

        // Recurse on children.
        for(HNode child : node.getChildren()) {
            nodeCount += appendNode(builder, child, path, docName, warningNodes);
        }

        path.remove(path.size() - 1);

        return nodeCount;
    }
}
