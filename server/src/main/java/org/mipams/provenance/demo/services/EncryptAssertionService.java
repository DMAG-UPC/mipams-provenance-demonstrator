package org.mipams.provenance.demo.services;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.mipams.jumbf.entities.BinaryDataBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.entities.JumbfBoxBuilder;
import org.mipams.jumbf.entities.XmlBox;
import org.mipams.jumbf.services.CoreParserService;
import org.mipams.jumbf.services.boxes.JumbfBoxService;
import org.mipams.jumbf.services.content_types.XmlContentType;
import org.mipams.jumbf.util.CoreUtils;
import org.mipams.jumbf.util.MipamsException;
import org.mipams.provenance.crypto.CryptoException;
import org.mipams.provenance.crypto.CryptoRequest;
import org.mipams.provenance.crypto.CryptoService;
import org.mipams.privsec.entities.ProtectionDescriptionBox;
import org.mipams.privsec.services.content_types.ProtectionContentType;
import org.mipams.provenance.entities.ProvenanceErrorMessages;
import org.mipams.provenance.entities.ProvenanceMetadata;
import org.mipams.provenance.entities.assertions.RedactableAssertion;
import org.mipams.provenance.services.AssertionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class EncryptAssertionService {

    @Autowired
    CryptoService cryptoService;

    @Autowired
    CoreParserService coreParserService;

    @Autowired
    AssertionFactory assertionFactory;

    @Autowired
    JumbfBoxService jumbfBoxService;

    @Value("${org.mipams.provenance.demo.encScheme}")
    String ENCRYPTION_SCHEME;

    @Value("${org.mipams.provenance.demo.encSecret}")
    String ENCRYPTION_SECRET;

    public JumbfBox encrypt(RedactableAssertion assertion, String arJumbfBoxLabel,
            ProvenanceMetadata provenanceMetadata) throws MipamsException {

        final String assertionLabel = assertionFactory.getBaseLabel(assertion);
        final int ivSizeInBytes = 16;

        String outputFileUrl = null;
        try {
            byte[] iv = cryptoService.getRandomNumber(ivSizeInBytes);
            String ivAsHex = CoreUtils.convertByteArrayToHex(iv);

            JumbfBox assertionJumbfBox = assertionFactory.convertAssertionToJumbfBox(assertion, provenanceMetadata);

            outputFileUrl = writeJumbfBoxToAFile(assertionJumbfBox, provenanceMetadata);

            String encryptedContentUrl = encryptFileContent(ivAsHex, outputFileUrl);

            JumbfBox protectionJumbfBox = buildProtectionBox(iv, assertionLabel, arJumbfBoxLabel, encryptedContentUrl);

            return protectionJumbfBox;
        } catch (CryptoException e) {
            throw new MipamsException(e);
        } finally {
            if (outputFileUrl != null) {
                CoreUtils.deleteFile(outputFileUrl);
            }
        }
    }

    private String writeJumbfBoxToAFile(JumbfBox assertionJumbfBox, ProvenanceMetadata provenanceMetadata)
            throws MipamsException {

        String jumbfBoxFileName = CoreUtils.randomStringGenerator();
        String jumbfBoxFilePath = CoreUtils.getFullPath(provenanceMetadata.getParentDirectory(),
                jumbfBoxFileName);

        try (OutputStream fos = new FileOutputStream(jumbfBoxFilePath)) {
            jumbfBoxService.writeToJumbfFile(assertionJumbfBox, fos);
        } catch (IOException e) {
            throw new MipamsException(ProvenanceErrorMessages.JUMBF_BOX_CREATION_ERROR, e);
        }

        return jumbfBoxFilePath;
    }

    private String encryptFileContent(String ivAsHex, String jumbfFilePath) throws MipamsException {

        CryptoRequest encryptionRequest = new CryptoRequest();

        encryptionRequest.setCryptoMethod(ENCRYPTION_SCHEME);
        encryptionRequest.setIv(ivAsHex);
        encryptionRequest.setContentFileUrl(jumbfFilePath);

        try {
            return cryptoService.encryptDocument(getSecretKey(), encryptionRequest);
        } catch (CryptoException e) {
            throw new MipamsException(ProvenanceErrorMessages.ENCRYPTION_ERROR, e);
        }
    }

    private JumbfBox buildProtectionBox(byte[] iv, String assertionLabel,
            String accessRulesLabel, String encryptedContentFilePath) throws MipamsException {

        JumbfBoxBuilder builder = new JumbfBoxBuilder(new ProtectionContentType());
        builder.setJumbfBoxAsRequestable();
        builder.setLabel(assertionLabel);

        ProtectionDescriptionBox pdBox = new ProtectionDescriptionBox();
        pdBox.setAes256CbcWithIvProtection();
        pdBox.setIv(iv);

        if (accessRulesLabel != null) {
            pdBox.setArLabel(accessRulesLabel);
            pdBox.includeAccessRulesInToggle();
        }

        BinaryDataBox bdBox = new BinaryDataBox();
        bdBox.setFileUrl(encryptedContentFilePath);

        builder.appendContentBox(pdBox);
        builder.appendContentBox(bdBox);

        return builder.getResult();
    }

    public JumbfBox decrypt(ProtectionDescriptionBox pdBox, BinaryDataBox bdBox) throws MipamsException {
        try {
            if (!pdBox.isAes256CbcWithIvProtection()) {
                throw new MipamsException(ProvenanceErrorMessages.UNSUPPORTED_ENCRYPTION);
            }

            CryptoRequest decryptionRequest = new CryptoRequest();
            decryptionRequest.setContentFileUrl(bdBox.getFileUrl());
            decryptionRequest.setIv(CoreUtils.convertByteArrayToHex(pdBox.getIv()));
            decryptionRequest.setCryptoMethod(ENCRYPTION_SCHEME);

            String outputFileUrl = cryptoService.decryptDocument(getSecretKey(), decryptionRequest);

            JumbfBox decryptedJumbfBox = coreParserService.parseMetadataFromFile(outputFileUrl).get(0);
            return decryptedJumbfBox;
        } catch (CryptoException e) {
            throw new MipamsException(e);
        }
    }

    private SecretKey getSecretKey() throws MipamsException {
        byte[] aesKeyData = CoreUtils.convertHexToByteArray(ENCRYPTION_SECRET);

        try {
            return new SecretKeySpec(aesKeyData, "AES");
        } catch (IllegalArgumentException e) {
            throw new MipamsException(e);
        }
    }

    public String userHasAccessToResource(UserDetails userDetails, JumbfBox accessRulesJumbfBox)
            throws CryptoException {

        // String username = userDetails.getUsername();
        // String rolesAsList = authoritiesToList(userDetails.getAuthorities());
        // JumbfBox accessRules = accessRulesJumbfBox;

        // return cryptoService.authorizeAccessToResource(accessRules, username,
        // rolesAsList);
        return "Example policy that allowed user to access to metadata";
    }

    // private String authoritiesToList(Collection<? extends GrantedAuthority>
    // authorities) {
    // StringBuilder result = new StringBuilder();

    // for (GrantedAuthority auth : authorities) {
    // result.append(auth.getAuthority());
    // if (result.length() > 0) {
    // result.append(",");
    // }

    // }

    // return result.toString();
    // }

    public JumbfBox defineAccessRulesForAssertion(String accessRulesLabel, String rule, ProvenanceMetadata metadata)
            throws MipamsException {
        JumbfBoxBuilder builder = new JumbfBoxBuilder(new XmlContentType());

        builder.setJumbfBoxAsRequestable();
        builder.setLabel(accessRulesLabel);

        XmlBox xmlBox = new XmlBox();

        if (rule == null) {
            try {
                rule = cryptoService.generatePolicy();
            } catch (CryptoException e) {
                throw new MipamsException(e);
            }
        }

        xmlBox.setContent(rule.getBytes());

        builder.appendContentBox(xmlBox);

        return builder.getResult();
    }

}
