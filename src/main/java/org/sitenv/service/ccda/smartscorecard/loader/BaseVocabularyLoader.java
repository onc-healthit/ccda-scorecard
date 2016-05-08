package org.sitenv.service.ccda.smartscorecard.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class BaseVocabularyLoader {

    public boolean doInsert(String sql, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean inserted = true;
        try{
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.execute();
        }finally {
            if(preparedStatement != null){
                preparedStatement.close();
            }
        }
       return inserted;
    }
}
