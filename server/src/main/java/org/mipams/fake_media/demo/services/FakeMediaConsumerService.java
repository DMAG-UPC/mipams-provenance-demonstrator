package org.mipams.fake_media.demo.services;

import java.io.ByteArrayInputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.mipams.fake_media.demo.entities.responses.ClaimResponse;
import org.mipams.fake_media.demo.entities.responses.FakeMediaResponse;
import org.mipams.fake_media.entities.Claim;
import org.mipams.fake_media.entities.assertions.Assertion;
import org.mipams.fake_media.entities.responses.ManifestResponse;
import org.mipams.fake_media.entities.responses.ManifestStoreResponse;
import org.mipams.fake_media.services.AssertionFactory;
import org.mipams.fake_media.services.consumer.ClaimConsumer;
import org.mipams.fake_media.services.consumer.ManifestStoreConsumer;
import org.mipams.fake_media.services.content_types.ManifestStoreContentType;
import org.mipams.jumbf.core.entities.BinaryDataBox;
import org.mipams.jumbf.core.entities.JumbfBox;
import org.mipams.jumbf.core.services.JpegCodestreamGenerator;
import org.mipams.jumbf.core.services.JpegCodestreamParser;
import org.mipams.jumbf.core.util.CoreUtils;
import org.mipams.jumbf.core.util.MipamsException;
import org.mipams.jumbf.core.util.Properties;
import org.mipams.jumbf.crypto.entities.CryptoException;
import org.mipams.jumbf.crypto.services.CredentialsReaderService;
import org.mipams.jumbf.privacy_security.entities.ProtectionDescriptionBox;
import org.mipams.jumbf.privacy_security.services.content_types.ProtectionContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class FakeMediaConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(FakeMediaConsumerService.class);

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

    @Autowired
    Properties properties;

    public ManifestStoreResponse inspect(String digitalAssetUrl, boolean fullInspection, UserDetails userDetails)
            throws MipamsException {

        List<JumbfBox> boxList = jpegCodestreamParser.parseMetadataFromFile(digitalAssetUrl);
        logger.debug(boxList.toString());

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
        } else {
            response = manifestStoreConsumer.consumeActiveManifest(manifestStoreJumbfBox, strippedDigitalAssetUrl);
        }

        if (userDetails != null) {
            response = enforceAccessRulePolicy(response, userDetails);
        }

        if (!CoreUtils.deleteFile(strippedDigitalAssetUrl)) {
            logger.error("Failed to delete file " + strippedDigitalAssetUrl);
        }

        return response;
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

            ManifestResponse manifestResponse = new ManifestResponse(
                    protectedResponse.getManifestResponseMap().get(manifestId));

            enforceAccessRulePolicyOnAssertionList(manifestResponse.getAssertionJumbfBoxList(), userDetails);

            finalResponse.getManifestResponseMap().put(manifestId, manifestResponse);
        }

        return finalResponse;
    }

    private void enforceAccessRulePolicyOnAssertionList(List<JumbfBox> assertionJumbfBoxList, UserDetails userDetails)
            throws MipamsException {

        for (JumbfBox assertionJumbfBox : new ArrayList<>(assertionJumbfBoxList)) {
            if (assertionFactory.isJumbfBoxAnAssertion(assertionJumbfBox) && isProtectionBox(assertionJumbfBox)) {

                logger.info("Found a Protection Box");

                ProtectionDescriptionBox dBox = (ProtectionDescriptionBox) assertionJumbfBox.getContentBoxList().get(0);
                BinaryDataBox bDataBox = (BinaryDataBox) assertionJumbfBox.getContentBoxList().get(1);

                if (dBox.accessRulesExist()) {

                    logger.info("Found Access Rules as well");

                    JumbfBox arJumbfBox = CoreUtils.locateJumbfBoxFromLabel(assertionJumbfBoxList, dBox.getArLabel());

                    if (!encryptAssertionService.userHasAccessToResource(userDetails, arJumbfBox)) {
                        continue;
                    }

                    assertionJumbfBoxList.remove(arJumbfBox);
                }

                logger.info("Access Granted. Decrypting assertion");

                JumbfBox decryptedAssertionJumbfBox = encryptAssertionService.decrypt(dBox, bDataBox);
                assertionJumbfBoxList.add(decryptedAssertionJumbfBox);
                assertionJumbfBoxList.remove(assertionJumbfBox);
            }
        }

    }

    public FakeMediaResponse getFakeMediaResponseForManifest(String manifestId, ManifestResponse manifestResponse)
            throws MipamsException {
        FakeMediaResponse response = new FakeMediaResponse();

        response.setManifestId(manifestId);

        List<Assertion> assertionList = getAssertionListFromJumbfBoxList(manifestResponse.getAssertionJumbfBoxList());
        response.setAssertionList(assertionList);

        List<String> inaccessibleJumbfBoxLabelList = getProtectionBoxLabelList(
                manifestResponse.getAssertionJumbfBoxList());

        ClaimResponse claimResponse = generateClaimResponse(manifestResponse.getClaim(),
                manifestResponse.getClaimCertificate());
        response.setClaimResponse(claimResponse);
        response.setInaccessibleJumbfBoxLabelList(inaccessibleJumbfBoxLabelList);

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
        ProtectionContentType protectionContentType = new ProtectionContentType();
        return assertionJumbfBox.getDescriptionBox().getUuid()
                .equalsIgnoreCase(protectionContentType.getContentTypeUuid());
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

    private ClaimResponse generateClaimResponse(JumbfBox claimJumbfBox, byte[] claimCertificate)
            throws MipamsException {

        ClaimResponse claimResponse = new ClaimResponse();

        Claim claim = claimConsumer.desirializeClaimJumbfBox(claimJumbfBox);
        claimResponse.setSignedGeneratorDescription(claim.getClaimGeneratorDescription());

        String dname = getDistinguishedNameFromCertificate(claimCertificate);
        claimResponse.setSignedBy(dname);

        return claimResponse;
    }

    private String getDistinguishedNameFromCertificate(byte[] claimCertificate) throws MipamsException {

        try {
            X509Certificate cert = credentialsReaderService.getCertificate(new ByteArrayInputStream(claimCertificate));

            if (cert.getIssuerDN() == null) {
                throw new MipamsException("Certificate Issuer Not found");
            }

            return cert.getIssuerDN().getName();
        } catch (CryptoException e) {
            throw new MipamsException(e);
        }
    }
}
