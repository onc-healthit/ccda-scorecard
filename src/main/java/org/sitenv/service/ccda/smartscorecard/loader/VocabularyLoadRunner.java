package org.sitenv.service.ccda.smartscorecard.loader;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

public class VocabularyLoadRunner implements InitializingBean, DisposableBean {
    private VocabularyLoaderFactory vocabularyLoaderFactory;
    private static Logger logger = Logger.getLogger(VocabularyLoadRunner.class);
    private String codeDirectory = null;
    private boolean recursive = true;
    private DataSource dataSource;

    public void loadDirectory(String directory, Connection connection) throws IOException {
        File dir = new File(directory);
        if (dir.isFile()) {
            throw new IOException("Directory to Load is a file and not a directory");
        } else {
            File[] list = dir.listFiles();
            for (File file : list) {
                load(file, connection);
            }
        }
    }

    private void load(File directory, Connection connection) throws IOException {
        if (directory.isDirectory() && !directory.isHidden()) {
            File[] filesToLoad = directory.listFiles();
            logger.debug("Building Loader for directory: " + directory.getName() + "...");
            VocabularyLoader loader = vocabularyLoaderFactory.getVocabularyLoader(directory.getName());
            if (loader != null && filesToLoad != null) {
                logger.debug("Loader built...");
                //logger.debug("Loading file: " + loadFile.getAbsolutePath() + "...");
                loader.load(Arrays.asList(filesToLoad), connection);
                logger.debug("File loaded...");
            } else {
                logger.debug("Building of Loader Failed.");
            }
        }
    }

    public String getCodeDirectory() {
        return codeDirectory;
    }

    public void setCodeDirectory(String codeDirectory) {
        this.codeDirectory = codeDirectory;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setVocabularyLoaderFactory(VocabularyLoaderFactory vocabularyLoaderFactory) {
        this.vocabularyLoaderFactory = vocabularyLoaderFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            if (codeDirectory != null && !codeDirectory.trim().equals("")) {
                logger.info("Loading vocabularies at: " + codeDirectory + "...");
                loadDirectory(codeDirectory, connection);
                logger.info("Vocabularies loaded...");
            }
            connection.commit();

            logger.info("!!!!*********** VOCABULARY DATABASE HAS FINISHED LOADING - SERVER WILL CONTINUE AND SHOULD BE DONE SHORTLY. ***********!!!!");
        } catch (Exception e) {
            logger.error("Failed to load configured vocabulary directory.", e);
        }finally {
            try {
                if(connection != null || !(connection.isClosed())) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void destroy() throws Exception {

    }
}
