package org.sitenv.service.ccda.smartscorecard.loader;

public interface VocabularyLoaderFactory {
    VocabularyLoader getVocabularyLoader(String loaderType);
}
