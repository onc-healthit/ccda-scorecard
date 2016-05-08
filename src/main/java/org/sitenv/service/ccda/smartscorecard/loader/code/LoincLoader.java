package org.sitenv.service.ccda.smartscorecard.loader.code;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.apache.log4j.Logger;
import org.sitenv.service.ccda.smartscorecard.loader.BaseVocabularyLoader;
import org.sitenv.service.ccda.smartscorecard.loader.VocabularyLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


@Component(value = "LOINC")
public class LoincLoader extends BaseVocabularyLoader implements VocabularyLoader {
    private static Logger logger = Logger.getLogger(LoincLoader.class);

    @Override
    public void load(List<File> filesToLoad, Connection connection) {
        BufferedReader br = null;
        FileReader fileReader = null;
        try {
            String insertQueryPrefix = "insert into LOINC (ID, CODE, DISPLAYNAME, CODESYSTEM, EXAMPLEUCUM, EXAMPLEUCUMDISPLAY) values ";
            StrBuilder insertQueryBuilder = new StrBuilder(insertQueryPrefix);
            int totalCount = 0, pendingCount = 0;

            for (File file : filesToLoad) {
                if (file.isFile() && !file.isHidden()) {
                    logger.debug("Loading LOINC File: " + file.getName());
                    int count = 0;
                    fileReader = new FileReader(file);
                    br = new BufferedReader(fileReader);
                    String available;
                    while ((available = br.readLine()) != null) {
                        if ((count++ == 0)) {
                            continue; // skip header row
                        } else {
                            String[] line = StringUtils.splitPreserveAllTokens(available, ",", 0);
                            if(available.length() > 0)
                            {
                            	if (pendingCount++ > 0) {
                                    insertQueryBuilder.append(",");
                                }
                                insertQueryBuilder.append("(");
                                insertQueryBuilder.append("DEFAULT");
                                insertQueryBuilder.append(",'");
                                insertQueryBuilder.append(StringUtils.strip(line[0], "\"").toUpperCase());
                                insertQueryBuilder.append("','");
                                insertQueryBuilder.append(StringUtils.strip(line[16], "\"").toUpperCase().replaceAll("'", "''"));
                                insertQueryBuilder.append("','");
                                insertQueryBuilder.append(file.getParentFile().getName());
                                insertQueryBuilder.append("','");
                                insertQueryBuilder.append(StringUtils.strip(line[5], "\"").replaceAll("'", "''"));
                                insertQueryBuilder.append("','");
                                insertQueryBuilder.append(StringUtils.strip(line[6], "\"").replaceAll("'", "''"));
                                insertQueryBuilder.append("')");

                                if ((++totalCount % 100) == 0) {
                                    doInsert(insertQueryBuilder.toString(), connection);
                                    insertQueryBuilder.clear();
                                    insertQueryBuilder.append(insertQueryPrefix);
                                    pendingCount = 0;
                                }
                            }
                        }
                    }
                }
            }
            if (pendingCount > 0) {
                doInsert(insertQueryBuilder.toString(), connection);
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (SQLException e) {
        	logger.error(e);
            e.printStackTrace();
            
        } finally {
            if (br != null) {
                try {
                    fileReader.close();
                    br.close();
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }
    }
}
