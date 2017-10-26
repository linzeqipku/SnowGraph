package graphlocater.wrapper;

import org.apache.commons.lang3.StringUtils;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.Serializable;
import java.util.*;

public class PhraseInfo implements Serializable {
    private static final long serialVersionUID = -8383713376186053397L;
    public static final String TABLE_NAME = "phrases";

    public static final int PHRASE_TYPE_DEFAULT = 0;
    public static final int PHRASE_TYPE_VP = 1;
    public static final int PHRASE_TYPE_NP = 2;

    public static final int PROOF_SCORE_DEFAULT = -1;
    private int id;
    private int phraseType = PHRASE_TYPE_DEFAULT;

    /**
     * The parent ({@code}SentenceInfo) of the phrase.
     */
    private int parentId = -1;

    private String text;
    private String syntaxTree;
    private Set<String> wordSet = null;

    private List<Proof> proofs;
    private String	proofString;
	private int	proofScore = PROOF_SCORE_DEFAULT;


    public PhraseInfo() {
        super();
        proofs = new ArrayList<>();
    }

    public void addProof(Proof proof) {
        if (proofs == null)
            proofs = new ArrayList<>();
        proofs.add(proof);
    }

    public boolean hasProof(ProofType type) {
        if (type == null)
            return false;
        for (Proof proof : proofs) {
            if (type.equals(proof.getType()))
                return true;
        }
        return false;
    }

    public String printProofs() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < proofs.size(); i++) {
            sb.append(proofs.get(i).toString());
            if (i < proofs.size() - 1)
                sb.append(System.getProperty("line.separator"));
        }
        sb.insert(0, "[ProofScore] " + getProofScore() + System.getProperty("line.separator"));
        return sb.toString();
    }

    public String getProofString() {
        if (StringUtils.isBlank(proofString))
            proofString = Proof.concatenateProofs(proofs);
        return proofString;
    }

    public int getProofScore() {
        if (proofScore == PROOF_SCORE_DEFAULT) {
            if (proofs == null || proofs.size() <= 0)
                return proofScore;

            proofScore = 0;
            for (Proof proof : proofs) {
                proofScore += proof.getScore();
            }
        }
        return proofScore;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPhraseType() {
        return phraseType;
    }

    public void setPhraseType(int phraseType) {
        this.phraseType = phraseType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSyntaxTree() {
        return syntaxTree;
    }

    public void setSyntaxTree(String syntaxTree) {
        this.syntaxTree = syntaxTree;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public Set<String> getCleanWordSet(){
        if (wordSet != null)
            return wordSet;
        wordSet = new HashSet<>();
        String cleanText = text.replaceAll("[^a-zA-Z]", " ")
                .trim().toLowerCase();
        EnglishStemmer stemmer = new EnglishStemmer();
        for (String word : cleanText.split("\\s+")){
            if (word.length() <= 2)
                continue;
            stemmer.setCurrent(word);
            stemmer.stem();
            wordSet.add(stemmer.getCurrent());
        }
        return wordSet;
    }
}