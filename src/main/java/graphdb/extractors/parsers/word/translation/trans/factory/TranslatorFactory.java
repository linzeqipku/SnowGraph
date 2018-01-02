package graphdb.extractors.parsers.word.translation.trans.factory;

import java.net.URISyntaxException;

import graphdb.extractors.parsers.word.translation.trans.Translator;
import graphdb.extractors.parsers.word.translation.trans.exception.DupIdException;

final public class TranslatorFactory extends AbstractTranslatorFactory{

	public TranslatorFactory() throws ClassNotFoundException, InstantiationException, IllegalAccessException, DupIdException, URISyntaxException {
		super();
	}

	@Override
	public Translator get(String id) {
		return translatorMap.get(id);
	}

}
