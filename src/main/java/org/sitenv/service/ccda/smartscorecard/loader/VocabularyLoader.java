package org.sitenv.service.ccda.smartscorecard.loader;

import java.io.File;
import java.sql.Connection;
import java.util.List;

public interface VocabularyLoader {
    void load(List<File> file, Connection connection);
}
