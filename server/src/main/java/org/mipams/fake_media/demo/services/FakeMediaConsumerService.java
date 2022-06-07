package org.mipams.fake_media.demo.services;

import java.io.ByteArrayInputStream;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.mipams.fake_media.demo.entities.responses.ClaimResponse;
import org.mipams.fake_media.demo.entities.responses.FakeMediaResponse;
import org.mipams.fake_media.entities.Claim;
import org.mipams.fake_media.entities.ProvenanceErrorMessages;
import org.mipams.fake_media.entities.assertions.Assertion;
import org.mipams.fake_media.entities.responses.ManifestResponse;
import org.mipams.fake_media.entities.responses.ManifestStoreResponse;
import org.mipams.fake_media.services.AssertionFactory;
import org.mipams.fake_media.services.AssertionFactory.MipamsAssertion;
import org.mipams.fake_media.services.consumer.ClaimConsumer;
import org.mipams.fake_media.services.consumer.ManifestStoreConsumer;
import org.mipams.fake_media.services.content_types.ManifestStoreContentType;
import org.mipams.jumbf.core.entities.BinaryDataBox;
import org.mipams.jumbf.core.entities.JumbfBox;
import org.mipams.jumbf.core.services.CoreParserService;
import org.mipams.jumbf.core.services.JpegCodestreamGenerator;
import org.mipams.jumbf.core.services.JpegCodestreamParser;
import org.mipams.jumbf.core.util.CoreUtils;
import org.mipams.jumbf.core.util.MipamsException;
import org.mipams.jumbf.core.util.Properties;
import org.mipams.jumbf.crypto.entities.CryptoException;
import org.mipams.jumbf.crypto.entities.request.CryptoRequest;
import org.mipams.jumbf.crypto.services.CredentialsReaderService;
import org.mipams.jumbf.crypto.services.CryptoService;
import org.mipams.jumbf.privacy_security.entities.ProtectionDescriptionBox;
import org.mipams.jumbf.privacy_security.services.content_types.ProtectionContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FakeMediaConsumerService {

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
    CryptoService cryptoService;

    @Autowired
    CoreParserService coreParserService;

    @Autowired
    Properties properties;

    @Value("${org.mipams.fake_media.demo.encScheme}")
    String ENCRYPTION_SCHEME;

    @Value("${org.mipams.fake_media.demo.encSecret}")
    String ENCRYPTION_SECRET;

    public String inspect(String digitalAssetUrl, boolean fullInspection, UserDetails userDetails)
            throws MipamsException {

        List<JumbfBox> boxList = jpegCodestreamParser.parseMetadataFromFile(digitalAssetUrl);

        JumbfBox manifestStoreJumbfBox = locateManifestStoreJumbfBox(boxList);

        String strippedDigitalAssetUrl = stripDigitalAssetFromProvenanceMetadata(digitalAssetUrl);

        ManifestStoreResponse response;
        if (fullInspection) {
            response = manifestStoreConsumer.consumeFullManifestStore(manifestStoreJumbfBox, strippedDigitalAssetUrl);
        } else {
            response = manifestStoreConsumer.consumeActiveManifest(manifestStoreJumbfBox, strippedDigitalAssetUrl);
        }

        if (userDetails != null) {
            response = enforceAccessRulePolicy(response, userDetails);
        }

        return prepareResponse(response);
    }

    private JumbfBox locateManifestStoreJumbfBox(List<JumbfBox> boxList) {

        ManifestStoreContentType contentType = new ManifestStoreContentType();
        JumbfBox result = null;

        for (JumbfBox jumbfBox : boxList) {

            if (contentType.getContentTypeUuid().equalsIgnoreCase(jumbfBox.getDescriptionBox().getUuid())) {
                result = jumbfBox;
            }
        }

        return result;
    }

    private String stripDigitalAssetFromProvenanceMetadata(String digitalAssetUrl) throws MipamsException {
        String strippedDigitalAssetUrl = CoreUtils.getFullPath(properties.getFileDirectory(), digitalAssetUrl + "-tmp");

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
            if (isAssertion(assertionJumbfBox) && isProtectionBox(assertionJumbfBox)) {

                ProtectionDescriptionBox dBox = (ProtectionDescriptionBox) assertionJumbfBox.getContentBoxList().get(0);
                BinaryDataBox bDataBox = (BinaryDataBox) assertionJumbfBox.getContentBoxList().get(1);

                if (dBox.accessRulesExist()) {
                    JumbfBox accessRulesJumbfBox = locateJumbfBoxFromLabel(assertionJumbfBoxList,
                            dBox.getArLabel());
                    if (!cryptoService.accessRulesVerifiedSuccessfully(userDetails.getUsername(),
                            authoritiesToList(userDetails.getAuthorities()), accessRulesJumbfBox)) {
                        continue;
                    }
                    assertionJumbfBoxList.remove(accessRulesJumbfBox);
                }

                JumbfBox decryptedAssertionJumbfBox = decryptProtectionBox(dBox, bDataBox);
                assertionJumbfBoxList.add(decryptedAssertionJumbfBox);
                assertionJumbfBoxList.remove(assertionJumbfBox);

            }
        }

    }

    private String authoritiesToList(Collection<? extends GrantedAuthority> authorities) {
        StringBuilder result = new StringBuilder();

        for (GrantedAuthority auth : authorities) {
            result.append(auth.getAuthority());
            if (result.length() > 0) {
                result.append(",");
            }

        }

        return result.toString();
    }

    private JumbfBox decryptProtectionBox(ProtectionDescriptionBox pdBox, BinaryDataBox bdBox) throws MipamsException {

        try {
            if (!pdBox.isAes256CbcWithIvProtection()) {
                throw new MipamsException(ProvenanceErrorMessages.UNSUPPORTED_ENCRYPTION);
            }

            CryptoRequest decryptionRequest = new CryptoRequest();
            decryptionRequest.setContentFileUrl(bdBox.getFileUrl());
            decryptionRequest.setIv(DatatypeConverter.printHexBinary(pdBox.getIv()));
            decryptionRequest.setCryptoMethod(ENCRYPTION_SCHEME);

            byte[] aesKeyData = DatatypeConverter.parseHexBinary(ENCRYPTION_SECRET);

            SecretKeySpec secretKeySpec = new SecretKeySpec(aesKeyData, "AES");
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("AES");
            SecretKey secretKey = keyFactory.generateSecret(secretKeySpec);

            String outputFileUrl = cryptoService.decryptDocument(secretKey, decryptionRequest);

            JumbfBox decryptedJumbfBox = coreParserService.parseMetadataFromFile(outputFileUrl).get(0);
            return decryptedJumbfBox;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | CryptoException e) {
            throw new MipamsException(e);
        }
    }

    private JumbfBox locateJumbfBoxFromLabel(List<JumbfBox> assertionJumbfBoxList, String label)
            throws MipamsException {
        JumbfBox result = null;

        if (label == null) {
            return result;
        }

        for (JumbfBox jumbfBox : assertionJumbfBoxList) {
            if (label.equals(jumbfBox.getDescriptionBox().getLabel())) {
                result = jumbfBox;
                break;
            }
        }

        return result;
    }

    private String prepareResponse(ManifestStoreResponse manifestStoreResponse)
            throws MipamsException {

        List<FakeMediaResponse> responseList = new ArrayList<>();

        ManifestResponse manifestResponse;
        for (String manifestId : manifestStoreResponse.getManifestResponseMap().keySet()) {

            manifestResponse = manifestStoreResponse.getManifestResponseMap().get(manifestId);

            FakeMediaResponse fakeMediaResponse = getFakeMediaResponseForManifest(manifestId, manifestResponse);

            responseList.add(fakeMediaResponse);
        }

        ObjectMapper mapper = new ObjectMapper();
        String response;
        try {
            response = mapper.writeValueAsString(responseList);
        } catch (JsonProcessingException e) {
            throw new MipamsException(e.getMessage());
        }

        return response;
    }

    private FakeMediaResponse getFakeMediaResponseForManifest(String manifestId, ManifestResponse manifestResponse)
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
            if (isAssertion(assertionJumbfBox) && !isProtectionBox(assertionJumbfBox)) {
                assertion = assertionFactory.convertJumbfBoxToAssertion(assertionJumbfBox);
                result.add(assertion);
            }
        }

        return result;
    }

    private boolean isAssertion(JumbfBox assertionJumbfBox) {
        MipamsAssertion type = MipamsAssertion.getTypeFromLabel(assertionJumbfBox.getDescriptionBox().getLabel());
        return type != null;
    }

    private boolean isProtectionBox(JumbfBox assertionJumbfBox) {
        ProtectionContentType protectionContentType = new ProtectionContentType();
        return assertionJumbfBox.getDescriptionBox().getUuid()
                .equalsIgnoreCase(protectionContentType.getContentTypeUuid());
    }

    private List<String> getProtectionBoxLabelList(List<JumbfBox> assertionJumbfBoxList) {
        List<String> result = new ArrayList<>();

        for (JumbfBox assertionJumbfBox : assertionJumbfBoxList) {
            if (isAssertion(assertionJumbfBox) && isProtectionBox(assertionJumbfBox)) {
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
