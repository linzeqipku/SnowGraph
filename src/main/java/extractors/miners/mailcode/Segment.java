package extractors.miners.mailcode;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Segment {
    private boolean isCode;
    private List<String> sentences;

    public Segment(List<String> sentences) {
        this.sentences = sentences;
        this.isCode = false;
    }

    public Iterator<String> sentenceIterator() {
        return sentences.iterator();
    }

    public void setCode(boolean code) {
        isCode = code;
    }

    public boolean isCode() {
        return isCode;
    }

    int getSentenceNumber() {
        return sentences.size();
    }

    public List<String> getSubSentences(int start, int end) {
        return sentences.subList(start, end);
    }

    public String getText() {
        return sentences.stream().collect(Collectors.joining("\n"));
    }

    public String getText(int start, int end) {
        return sentences.stream().skip(start).limit(end - start).collect(Collectors.joining("\n"));
    }
}
