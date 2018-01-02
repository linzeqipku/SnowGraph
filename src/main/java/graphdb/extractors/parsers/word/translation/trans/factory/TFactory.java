package graphdb.extractors.parsers.word.translation.trans.factory;

import graphdb.extractors.parsers.word.translation.trans.Translator;

public interface TFactory {
	Translator get(String id);
}
