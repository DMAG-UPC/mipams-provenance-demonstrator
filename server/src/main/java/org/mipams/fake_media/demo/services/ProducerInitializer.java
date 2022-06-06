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

    public String initializeUserContext(String username) {

        CredentialsCommand command = new CredentialsCommand(CREDENTIALS_PATH, username);

        String result = command.createCredentials();
        logger.info(result);
        return result;
    }

    public String getUserDirectory(String username) throws MipamsException {
        return CoreUtils.getFullPath(CREDENTIALS_PATH, username);
    }

}
