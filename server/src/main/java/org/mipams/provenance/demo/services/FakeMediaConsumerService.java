package org.mipams.provenance.demo.services;

import java.io.ByteArrayInputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.mipams.jumbf.entities.BinaryDataBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.services.JpegCodestreamGenerator;
import org.mipams.jumbf.services.JpegCodestreamParser;
import org.mipams.jumbf.util.CoreUtils;
import org.mipams.jumbf.util.MipamsException;
import org.mipams.provenance.crypto.CryptoException;
import org.mipams.provenance.crypto.CredentialsReaderService;
import org.mipams.privsec.entities.ProtectionDescriptionBox;
import org.mipams.privsec.services.content_types.ProtectionContentType;
import org.mipams.provenance.demo.entities.responses.ClaimResponse;
import org.mipams.provenance.demo.entities.responses.FakeMediaResponse;
import org.mipams.provenance.entities.assertions.Assertion;
import org.mipams.provenance.entities.responses.DefaultManifestResponse;
import org.mipams.provenance.entities.responses.ManifestResponse;
import org.mipams.provenance.entities.responses.ManifestStoreResponse;
import org.mipams.provenance.entities.responses.ProtectedManifestResponse;
import org.mipams.provenance.services.AssertionFactory;
import org.mipams.provenance.services.consumer.ClaimConsumer;
import org.mipams.provenance.services.consumer.ManifestStoreConsumer;
import org.mipams.provenance.services.content_types.ManifestStoreContentType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class FakeMediaConsumerService {
    private static final Logger logger = Logger.getLogger(FakeMediaConsumerService.class.getName());

    @Autowired
    JpegCodestreamParser jpegCodestreamParser;

    @Autowired
    ManifestStoreConsumer manifestStoreConsumer;

    @Autowired
    ClaimConsumer claimConsumer;

    @Autowired
    CredentialsReaderService credentialsReaderService;

    @Autowired
    AssertionFactory assertionFactory;

    @Autowired
    JpegCodestreamGenerator jpegCodestreamGenerator;

    @Autowired
    EncryptAssertionService encryptAssertionService;

    public ManifestStoreResponse inspect(String digitalAssetUrl, boolean fullInspection, UserDetails userDetails)
            throws MipamsException {

        List<JumbfBox> boxList = jpegCodestreamParser.parseMetadataFromFile(digitalAssetUrl);
        logger.log(Level.FINE, boxList.toString());

        JumbfBox manifestStoreJumbfBox = locateManifestStoreJumbfBox(boxList);

        ManifestStoreResponse response = new ManifestStoreResponse();
        if (manifestStoreJumbfBox == null) {
            return response;
        }

        logger.info("---------------Manifest Store-------------------");
        logger.info(manifestStoreJumbfBox.toString());
        logger.info("-----------------------------------------------");

        String strippedDigitalAssetUrl = stripDigitalAssetFromProvenanceMetadata(digitalAssetUrl);

        if (fullInspection) {
            response = manifestStoreConsumer.consumeFullManifestStore(manifestStoreJumbfBox, strippedDigitalAssetUrl);

            if(hasProtectedManifests(response) && userDetails != null) {
                List<JumbfBox> manifests = manifestStoreJumbfBox.getContentBoxList().stream().map(box -> (JumbfBox) box).collect(Collectors.toList());
                enforceAccessRulePolicyOnJumbfList(manifests, userDetails);
                manifestStoreJumbfBox.getContentBoxList().clear();
                manifestStoreJumbfBox.getContentBoxList().addAll(manifests);
                response = manifestStoreConsumer.consumeFullManifestStore(manifestStoreJumbfBox, strippedDigitalAssetUrl);
            }

        } else {
            response = manifestStoreConsumer.consumeActiveManifest(manifestStoreJumbfBox, strippedDigitalAssetUrl);

            if(hasProtectedManifests(response) && userDetails != null) {
                List<JumbfBox> manifests = manifestStoreJumbfBox.getContentBoxList().stream().map(box -> (JumbfBox) box).collect(Collectors.toList());
                enforceAccessRulePolicyOnJumbfList(manifests, userDetails);
                manifestStoreJumbfBox.getContentBoxList().clear();
                manifestStoreJumbfBox.getContentBoxList().addAll(manifests);
                response = manifestStoreConsumer.consumeActiveManifest(manifestStoreJumbfBox, strippedDigitalAssetUrl);    
            }
        }

        if (userDetails != null) {
            response = enforceAccessRulePolicy(response, userDetails);
        }

        if (!CoreUtils.deleteFile(strippedDigitalAssetUrl)) {
            logger.log(Level.WARNING, "Failed to delete file " + strippedDigitalAssetUrl);
        }

        return response;
    }

    private boolean hasProtectedManifests(ManifestStoreResponse mstore) {
        return mstore.getManifestResponseMap().values().stream().filter(manifest -> manifest.getClass().getName().equals(ProtectedManifestResponse.class.getName())).count() > 0;
    }

    public JumbfBox locateManifestStoreJumbfBox(List<JumbfBox> boxList) {

        ManifestStoreContentType contentType = new ManifestStoreContentType();
        JumbfBox result = null;

        for (JumbfBox jumbfBox : boxList) {

            if (contentType.getContentTypeUuid().equalsIgnoreCase(jumbfBox.getDescriptionBox().getUuid())) {
                result = jumbfBox;
            }
        }

        return result;
    }

    public String stripDigitalAssetFromProvenanceMetadata(String digitalAssetUrl) throws MipamsException {
        String strippedDigitalAssetUrl = digitalAssetUrl + "-tmp";

        String targetUuid = new ManifestStoreContentType().getContentTypeUuid();

        jpegCodestreamGenerator.stripJumbfMetadataWithUuidEqualTo(digitalAssetUrl, strippedDigitalAssetUrl, targetUuid);
        return strippedDigitalAssetUrl;
    }

    private ManifestStoreResponse enforceAccessRulePolicy(ManifestStoreResponse protectedResponse,
            UserDetails userDetails) throws MipamsException {

        ManifestStoreResponse finalResponse = new ManifestStoreResponse();     

        for (String manifestId : protectedResponse.getManifestResponseMap().keySet()) {

            ManifestResponse manifestResponse = protectedResponse.getManifestResponseMap().get(manifestId);

            if(manifestResponse.getClass().getName().equals(ProtectedManifestResponse.class.getName())) {
                continue;
            }
            
            DefaultManifestResponse newManifestResponse = new DefaultManifestResponse((DefaultManifestResponse) manifestResponse);

            enforceAccessRulePolicyOnJumbfList(newManifestResponse.getAssertionJumbfBoxList(), userDetails);

            finalResponse.getManifestResponseMap().put(manifestId, newManifestResponse);
        }

        return finalResponse;
    }

    private void enforceAccessRulePolicyOnJumbfList(List<JumbfBox> jumbfBoxList, UserDetails userDetails)
            throws MipamsException {
        
        for (JumbfBox jumbfBox : new ArrayList<>(jumbfBoxList)) {
            if (isProtectionBox(jumbfBox)) {
                logger.info("Found a Protection Box");

                ProtectionDescriptionBox dBox = (ProtectionDescriptionBox) jumbfBox.getContentBoxList().get(0);
                BinaryDataBox bDataBox = (BinaryDataBox) jumbfBox.getContentBoxList().get(1);

                if (dBox.accessRulesExist()) {

                    logger.info("Found Access Rules as well");

                    JumbfBox arJumbfBox = CoreUtils.locateJumbfBoxFromLabel(jumbfBoxList, dBox.getArLabel());

                    try {
                        logger.info(encryptAssertionService.userHasAccessToResource(userDetails, arJumbfBox));
                    } catch (CryptoException e) {
                        logger.info(e.getMessage());
                        continue;
                    } finally {
                        logger.info("Removing assertion Jumbf Box");
                        jumbfBoxList.remove(arJumbfBox);
                    }
                }

                logger.info("Access Granted. Decrypting assertion");

                List<JumbfBox> decryptedAssertionJumbfBoxes = encryptAssertionService.decrypt(dBox, bDataBox);
                
                jumbfBoxList.addAll(0, decryptedAssertionJumbfBoxes);
                jumbfBoxList.remove(jumbfBox);
            }
        }
    }

    public FakeMediaResponse getFakeMediaResponseForManifest(String manifestId, ManifestResponse manifestResponse)
            throws MipamsException {
        FakeMediaResponse response = new FakeMediaResponse();

        response.setManifestId(manifestId);

        if(manifestResponse.getClass().getName().equals(ProtectedManifestResponse.class.getName())) {
            response.setManifestProtected(true);
        } else {
            DefaultManifestResponse defaultManifestResponse = (DefaultManifestResponse) manifestResponse;
            List<Assertion> assertionList = getAssertionListFromJumbfBoxList(defaultManifestResponse.getAssertionJumbfBoxList());
            response.setAssertionList(assertionList);

            List<String> inaccessibleJumbfBoxLabelList = getProtectionBoxLabelList(
                defaultManifestResponse.getAssertionJumbfBoxList());

            ClaimResponse claimResponse = generateClaimResponse(defaultManifestResponse);
            response.setClaimResponse(claimResponse);
            response.setInaccessibleJumbfBoxLabelList(inaccessibleJumbfBoxLabelList);
        }

        return response;
    }

    private List<Assertion> getAssertionListFromJumbfBoxList(List<JumbfBox> assertionJumbfBoxList)
            throws MipamsException {

        List<Assertion> result = new ArrayList<>();
        Assertion assertion;
        for (JumbfBox assertionJumbfBox : assertionJumbfBoxList) {
            if (assertionFactory.isJumbfBoxAnAssertion(assertionJumbfBox) && !isProtectionBox(assertionJumbfBox)) {
                assertion = assertionFactory.convertJumbfBoxToAssertion(assertionJumbfBox);
                result.add(assertion);
            }
        }

        return result;
    }

    private boolean isProtectionBox(JumbfBox assertionJumbfBox) {
        return (new ProtectionContentType()).getContentTypeUuid().equalsIgnoreCase(assertionJumbfBox.getDescriptionBox().getUuid());
    }

    private List<String> getProtectionBoxLabelList(List<JumbfBox> assertionJumbfBoxList) throws MipamsException {
        List<String> result = new ArrayList<>();

        for (JumbfBox assertionJumbfBox : assertionJumbfBoxList) {
            if (assertionFactory.isJumbfBoxAnAssertion(assertionJumbfBox) && isProtectionBox(assertionJumbfBox)) {
                result.add(assertionJumbfBox.getDescriptionBox().getLabel());
            }
        }

        return result;
    }

    private ClaimResponse generateClaimResponse(DefaultManifestResponse manifestResponse)
            throws MipamsException {

        ClaimResponse claimResponse = new ClaimResponse();

        claimResponse.setSignedGeneratorDescription(manifestResponse.getClaim().getClaimGeneratorDescription());
        claimResponse.setSignedOn(manifestResponse.getClaimSignature().getDate());

        String dname = getDistinguishedNameFromCertificate(manifestResponse.getClaimSignature().getCertificate());
        claimResponse.setSignedBy(dname);

        return claimResponse;
    }

    private String getDistinguishedNameFromCertificate(byte[] claimCertificate) throws MipamsException {

        try {
            X509Certificate cert = credentialsReaderService.getCertificate(new ByteArrayInputStream(claimCertificate));

            if (cert.getIssuerX500Principal() == null) {
                throw new MipamsException("Certificate Issuer Not found");
            }

            return cert.getIssuerX500Principal().getName();
        } catch (CryptoException e) {
            throw new MipamsException(e);
        }
    }
}
