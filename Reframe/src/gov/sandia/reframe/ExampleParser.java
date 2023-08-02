package gov.sandia.reframe;

import java.io.File;
import java.util.List;

import gov.sandia.cortext.fileparsing.pdf.HNode;
import gov.sandia.cortext.fileparsing.pdf.Hierarchy;
import gov.sandia.cortext.fileparsing.pdf.PdfParseParameters;
import gov.sandia.cortext.fileparsing.pdf.PdfParseResult;
import replete.collections.RArrayList;
import replete.numbers.RandomUtil;
import replete.threads.ThreadUtil;

public class ExampleParser {

    public PdfParseResult parse(File file, PdfParseParameters params) {

        List<String> segments = new RArrayList<>("Argon", "Neon", "Xeon");

        HNode node1a1 = new HNode()
            .setBullet("(1)")
            .setText("Nam massa turpis, ultrices nec rhoncus vitae, blandit ac enim. Aenean interdum aliquam urna, aliquet condimentum arcu interdum a. Quisque ultricies ut nu");

        HNode node1a2 = new HNode()
            .setBullet("(2)")
            .setText("Vestibulum ut dictum libero. Cras volutpat arcu dui, nec imperdiet sem consectetur quis. Morbi vel finibus sapien. Nunc felis velit, faucibus ut odio eg");

        HNode node1a = new HNode()
            .setBullet("a.")
            .setTitle("ABC Badges.")
            .setText("The following requirments apply.")
            .addChild(node1a1)
            .addChild(node1a2);

        HNode node1b1 = new HNode()
            .setBullet("(1)")
            .setText("Donec magna elit, feugiat in nisl ut, feugiat tincidunt lectus. Donec in neque luctus, convallis ipsum ac, posuere erat. Curabitur sit amet u");

        HNode node1b2 = new HNode()
            .setBullet("(2)")
            .setText("Proin molestie a diam eget tristique. Etiam vulputate urna vel odio lacinia auctor. Nam lacinia augue vitae lorem gravida malesua");

        HNode node1b = new HNode()
            .setBullet("b.")
            .setTitle("Office of Funding Badge.")
            .setText("X must prepare and distribute specifications for the X badge.")
            .addChild(node1b1)
            .addChild(node1b2);

        HNode node1 = new HNode()
            .setBullet("1.")
            .setTitle("GENERAL REQUIREMENTS.")
            .setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse porta nisl nulla, id rhoncus quam dignissim et. Etiam in sollicitudin nisi. Morbi pretium massa et eleifend con")
            .setLevelLabel("H1")
            .addChild(node1a)
            .addChild(node1b);

        HNode node = new HNode()
            .setTitle("CHAPTER XI")
            .setText("ABC ROAD TV, WALL, AND FOOD BANANA")
            .setLevelLabel("Chapter")
            .addChild(node1);

        Hierarchy hier = new Hierarchy()
            .setRoot(node);

        PdfParseResult result = new PdfParseResult()
            .setSegments(segments);

        if(RandomUtil.flip()) {
            result.setHierarchy(hier);
        }

        ThreadUtil.sleep(3000);

        return result;
    }

}
