package org.mipams.fake_media.demo.services;

import org.mipams.fake_media.demo.entities.CredentialsCommand;
import org.mipams.jumbf.core.util.CoreUtils;
import org.mipams.jumbf.core.util.MipamsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProducerInitializer {

    private static final Logger logger = LoggerFactory.getLogger(ProducerInitializer.class);

    @Value("${org.mipams.fake_media.demo.credentials_path}")
    String CREDENTIALS_PATH;

    @Value("${org.mipams.fake_media.demo.provenance_path}")
    String PROVENANCE_PATH;

    public String initializeUserContext(String username, String organization) {

        try {
            CoreUtils.createSubdirectory(PROVENANCE_PATH, username);
        } catch (MipamsException e) {
            logger.error("Failed to create subdirectory for user " + username);
        }

        CredentialsCommand command = new CredentialsCommand(CREDENTIALS_PATH, username, organization);

        String result = command.createCredentials();
        logger.info(result);
        return result;
    }

    public String getCredentialsUserDirectory(String username) throws MipamsException {
        return CoreUtils.getFullPath(CREDENTIALS_PATH, username);
    }

    public String getUserAssetDirectory(String username) throws MipamsException {
        return CoreUtils.getFullPath(PROVENANCE_PATH, username);
    }

}
