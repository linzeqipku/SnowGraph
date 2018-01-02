package graphdb.extractors.parsers.word.translation.trans;

public interface Translator {
	public String trans(LANG from, LANG targ, String query) throws Exception;
}
