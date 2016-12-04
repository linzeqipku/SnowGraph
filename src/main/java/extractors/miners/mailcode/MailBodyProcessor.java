package extractors.miners.mailcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class MailBodyProcessor {
    public static List<String> bodyToLines(String text) {
        BufferedReader in = new BufferedReader(new StringReader(text));
        ArrayList<String> lineList = new ArrayList<>();
        String line;
        try {
            while ((line = in.readLine()) != null) {
                lineList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lineList;
    }

    public static List<Segment> linesToSegments(List<String> lines) {
        ArrayList<Segment> segmentList = new ArrayList<>();
        Segment seg;
        ArrayList<String> sentenceList = new ArrayList<>();
        boolean start = true;
        for (String line : lines) {
            // 忽略段落开始的空行
            if (start && line.trim().isEmpty()) continue;
            // 一个段落开始
            if (start) {
                sentenceList = new ArrayList<>();
                start = false;
            }
            // 将一行话加入段落
            if (!line.trim().isEmpty()) {
                sentenceList.add(line);
                continue;
            }
            // 一个段落结束
            seg = new Segment(sentenceList);
            segmentList.add(seg);
            start = true;
        }
        return segmentList;
    }

    public static List<Segment> filterCodes(List<Segment> segments) {
        for (Segment seg : segments) {
            if (CodeJudge.isCode(seg.getText())) {
                if (seg.getSentenceNumber() < 200) seg.setCode(true);
            }
        }
        List<Segment> mergedSegment;
        mergedSegment = CodeMerge.continualCodeMerge(segments);
        mergedSegment = CodeMerge.SplitCodeSegment(mergedSegment);
        for (Segment seg : mergedSegment) {
            if (!seg.isCode()) {
                if (CodeJudge.isCode(seg.getText())) {
                    if (seg.getSentenceNumber() < 200) seg.setCode(true);
                }
            }
        }
        mergedSegment = CodeMerge.SplitCodeSegment(mergedSegment);
        //mergedSegment = CodeMerge.continualCodeMerge(segments);
        return mergedSegment;
    }

}
