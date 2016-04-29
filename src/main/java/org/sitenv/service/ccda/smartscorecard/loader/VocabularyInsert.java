package org.sitenv.service.ccda.smartscorecard.loader;

import java.sql.Connection;
import java.sql.SQLException;

public interface VocabularyInsert {
    boolean doInsert(String sql, Connection connection) throws SQLException;
}
