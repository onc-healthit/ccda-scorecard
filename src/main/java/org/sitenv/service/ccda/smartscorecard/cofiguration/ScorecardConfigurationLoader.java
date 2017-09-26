package org.sitenv.service.ccda.smartscorecard.cofiguration;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.oxm.Unmarshaller;

import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ScorecardConfigurationLoader implements InitializingBean {
    private Unmarshaller unmarshaller;
    private String scorecardConfigurationFilePath;
    private Configurations configurations;

	public void setScorecardConfigurationFilePath(String scorecardConfigurationFilePath) {
		this.scorecardConfigurationFilePath = scorecardConfigurationFilePath;
	}

	public void setUnmarshaller(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    public Configurations getConfigurations(){
        return configurations;
    }

    //Converts XML to Java Object
    public void xmlToObject(String fileName) throws IOException {
        FileInputStream fis = null;
        try {
        	if(fileName!=null){
        		fis = new FileInputStream(fileName);
        		configurations = (Configurations) unmarshaller.unmarshal(new StreamSource(fis));
        	}
        }catch(FileNotFoundException foe){
        	
        }
        finally {
        	if(fis!=null){
        		fis.close();
        	}
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        xmlToObject(scorecardConfigurationFilePath);
    }
}