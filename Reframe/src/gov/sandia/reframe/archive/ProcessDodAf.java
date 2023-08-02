package gov.sandia.reframe.archive;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import replete.text.StringUtil;

public class ProcessDodAf {
    public static void main(String[] args) throws IOException {
//
//        File dir = new File("C:\\Users\\dtrumbo\\work\\eclipse-main\\Orbweaver\\resources-UCNI");
//
//        CSVReader reader = new CSVReader(new FileReader(new File(dir, "dod-af.csv")));
//        List<String[]> lines = reader.readAll();
//        reader.close();
//
//        List<String[]> finalDocs = new ArrayList<String[]>();
//        for(String[] line : lines) {
//            String dodDoc = CSVProcessor.massage(line[7]);
//            String afDoc = CSVProcessor.massage(line[13]);
//            String dodKey = StringUtil.join(StringUtil.slice(line, 3, 7), "/");
//            String afKey = StringUtil.join(StringUtil.slice(line, 9, 13), "/");
//
//            // Just add text in AF column
//            if(dodDoc.contains("<No corresponding")) {
//                add(finalDocs, afKey, null, afDoc);
//
//            // Both are same document ("+=" means "and the AF document is the same, no key provided")
//            } else if(afDoc.contains("<Requirement same") || afDoc.equals("")) {
//                add(finalDocs, dodKey, "?", dodDoc);
//
//            // They both have content
//            } else {
//                //finalDocs.add(new String[] {dodKey, dodDoc});
//                finalDocs.add(new String[] {dodKey + "+" + afKey, dodDoc + " " + afDoc});
//            }
//        }
//
//        CSVWriter writer = new CSVWriter(new FileWriter(new File(dir, "dod-af-output.csv")));
//        writer.writeAll(finalDocs);
//        writer.close();
    }

    private static void add(List<String[]> finalDocs, String key, String postKey, String doc) {
        Map<String, String> docParts = getCellParts(doc);
        String pk = postKey == null ? "" : "+" + postKey;

        for(String partKey : docParts.keySet()) {
            String partDoc = docParts.get(partKey);
            if(partKey == null) {
                finalDocs.add(new String[] {key + pk, partDoc});
            } else {
                finalDocs.add(new String[] {key + "/" + partKey + pk, partDoc});
            }
        }
    }

    private static Map<String, String> getCellParts(String text) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        text = removeUnneededParenthesizations(text);

        // Could be enhanced to have every bullet type look for an actual sentence ending
        // before it, to make sure that "(2)" isn't part of the content, and not a bullet.
        String[] bulletPatterns = {
            "\\([a-zA-Z]\\)",            // "(a)", "(c)", "(G)"
            "\\([0-9]\\)",               // "(1)", "(9)"
            "(?:(?:^|\\s)[a-zA-Z])\\.",  // " a.", "b.", "F."
            "(?:(?:^|\\s)[0-9])\\."      // " 3.", "5."
        };

        Pattern p = Pattern.compile(StringUtil.join(bulletPatterns, "|"));
        Matcher m = p.matcher(text);
        int segStart = 0;
        String key = null;
        while(m.find()) {
            if(m.start() != 0) {
                result.put(key, text.substring(segStart, m.start()).trim());
            }
            key = m.group().trim();
            segStart = m.end();
        }
        result.put(key, text.substring(segStart).trim());
        if(result.isEmpty()) {
            result.put(null, text);
        }
        return result;
    }

    private static String removeUnneededParenthesizations(String text) {
        text = text.replaceAll("\\(AF\\)", "");
        text = text.replaceAll("\\(Added\\)", "");
        text = text.replaceAll("\\(U\\)", "");
        text = text.replaceAll("\\(DOD UCNI\\)", "");
        text = text.replaceAll("\\(DoD UCNI\\)", "");
        return text;
    }
}
