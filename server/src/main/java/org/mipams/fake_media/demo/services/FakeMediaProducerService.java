package org.mipams.fake_media.demo.services;

import java.io.File;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.mipams.fake_media.demo.entities.requests.FakeMediaRequest;
import org.mipams.fake_media.entities.ClaimGenerator;
import org.mipams.fake_media.entities.ProvenanceMetadata;
import org.mipams.fake_media.entities.ProvenanceSigner;
import org.mipams.fake_media.entities.UriReference;
import org.mipams.fake_media.entities.assertions.Assertion;
import org.mipams.fake_media.entities.assertions.IngredientAssertion;
import org.mipams.fake_media.entities.assertions.RedactableAssertion;
import org.mipams.fake_media.entities.requests.ProducerRequestBuilder;
import org.mipams.fake_media.services.AssertionFactory;
import org.mipams.fake_media.services.RedactionService;
import org.mipams.fake_media.services.UriReferenceService;
import org.mipams.fake_media.services.AssertionFactory.MipamsAssertion;
import org.mipams.fake_media.services.content_types.AssertionStoreContentType;
import org.mipams.fake_media.services.producer.ManifestStoreProducer;
import org.mipams.fake_media.utils.ProvenanceUtils;
import org.mipams.jumbf.core.entities.BmffBox;
import org.mipams.jumbf.core.entities.JumbfBox;
import org.mipams.jumbf.core.services.CoreGeneratorService;
import org.mipams.jumbf.core.services.JpegCodestreamGenerator;
import org.mipams.jumbf.core.services.JpegCodestreamParser;
import org.mipams.jumbf.core.util.CoreUtils;
import org.mipams.jumbf.core.util.MipamsException;
import org.mipams.jumbf.core.util.Properties;
import org.mipams.jumbf.crypto.entities.CryptoException;
import org.mipams.jumbf.crypto.services.CredentialsReaderService;
import org.mipams.jumbf.crypto.services.CryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class FakeMediaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(FakeMediaProducerService.class);

    final static String CERTIFICATE_FILENAME_FORMAT = "%s.crt";
    final static String KEY_FILENAME_FORMAT = "%s.priv.key";

    @Autowired
    CredentialsReaderService credentialsReaderService;

    @Autowired
    ManifestStoreProducer manifestStoreProducer;

    @Autowired
    UriReferenceService uriReferenceService;

    @Autowired
    CoreGeneratorService coreGeneratorService;

    @Autowired
    JpegCodestreamGenerator jpegCodestreamGenerator;

    @Autowired
    JpegCodestreamParser jpegCodestreamParser;

    @Autowired
    FakeMediaConsumerService fakeMediaConsumerService;

    @Autowired
    EncryptAssertionService encryptAssertionService;

    @Autowired
    AssertionFactory assertionFactory;

    @Value("${org.mipams.fake_media.demo.credentials_path}")
    String CREDENTIALS_PATH;

    @Value("${org.mipams.fake_media.demo.claim_generator}")
    String CLAIM_GENERATOR_DESCRIPTION;

    @Autowired
    CryptoService cryptoService;

    @Autowired
    Properties properties;

    @Autowired
    ExifToolService exifToolService;

    @Autowired
    RedactionService redactionService;

    public String produce(UserDetails userDetails, FakeMediaRequest fakeMediaRequest)
            throws MipamsException {

        fakeMediaConsumerService.inspect(fakeMediaRequest.getAssetUrl(), false, userDetails);

        List<JumbfBox> boxList = jpegCodestreamParser.parseMetadataFromFile(fakeMediaRequest.getAssetUrl());
        JumbfBox manifestStoreJumbfBox = fakeMediaConsumerService.locateManifestStoreJumbfBox(boxList);

        if (manifestStoreJumbfBox != null) {
            logger.info("---------------Manifest Store-------------------");
            logger.info(manifestStoreJumbfBox.toString());
            logger.info("-----------------------------------------------");
        }

        ProvenanceMetadata metadata = new ProvenanceMetadata();
        String tempWorkingDir = CoreUtils.createSubdirectory(properties.getFileDirectory(),
                CoreUtils.randomStringGenerator());
        metadata.setParentDirectory(tempWorkingDir);

        List<JumbfBox> assertionJumbfBoxList = calculateAssertionJumbfBoxList(userDetails, manifestStoreJumbfBox,
                fakeMediaRequest, metadata);

        ProvenanceSigner signer = getProvenanceSigner(userDetails);
        ProducerRequestBuilder requestBuilder = new ProducerRequestBuilder(fakeMediaRequest.getModifiedAssetUrl());
        requestBuilder.setSigner(signer);
        requestBuilder.setManifestStore(manifestStoreJumbfBox);

        ClaimGenerator claimGenerator = new ClaimGenerator();
        claimGenerator.setDescription(CLAIM_GENERATOR_DESCRIPTION);
        requestBuilder.setClaimGenerator(claimGenerator);

        requestBuilder.setRedactedAssertionList(fakeMediaRequest.getRedactedAssertionUriList());
        requestBuilder.setAssertionList(assertionJumbfBoxList);

        JumbfBox newManifestStoreJumbfBox = manifestStoreProducer.createManifestStore(requestBuilder.getResult());

        if (fakeMediaRequest.getRedactedAssertionUriList() != null
                && !fakeMediaRequest.getRedactedAssertionUriList().isEmpty()) {
            for (BmffBox contentBox : newManifestStoreJumbfBox.getContentBoxList()) {
                redactionService.redactAssertionsFromJumbfBox((JumbfBox) contentBox,
                        fakeMediaRequest.getRedactedAssertionUriList());
            }
        }

        String outputAssetUrl = jpegCodestreamGenerator.generateJumbfMetadataToFile(List.of(newManifestStoreJumbfBox),
                fakeMediaRequest.getModifiedAssetUrl());

        CoreUtils.deleteDir(metadata.getParentDirectory());

        return outputAssetUrl;
    }

    private List<JumbfBox> calculateAssertionJumbfBoxList(UserDetails userDetails, JumbfBox currentManifestStore,
            FakeMediaRequest fakeMediaRequest, ProvenanceMetadata metadata) throws MipamsException {

        List<JumbfBox> assertionJumbfBoxList = new ArrayList<>();
        String assetUrl = fakeMediaRequest.getAssetUrl();

        assertionJumbfBoxList.addAll(appendJumbfBoxForAssertions(fakeMediaRequest.getAssertionList(), metadata));

        appendParentIngredientAssertion(assetUrl, assertionJumbfBoxList, currentManifestStore, metadata);

        verifyRedactionRequest(currentManifestStore, fakeMediaRequest.getRedactedAssertionUriList());

        appendComponentIngredientManifestIfRequired(assetUrl, assertionJumbfBoxList,
                fakeMediaRequest.getComponentIngredientUriList(), metadata);

        appendEncryptedAssertionJumbfBoxes(assertionJumbfBoxList, fakeMediaRequest.getEncryptionAssertionList(),
                metadata);

        appendEncryptedAssertionJumbfBoxesWithAccessRules(assertionJumbfBoxList,
                fakeMediaRequest.getEncryptionWithAccessRulesAssertionList(), metadata);

        return assertionJumbfBoxList;
    }

    private List<JumbfBox> appendJumbfBoxForAssertions(List<Assertion> assertionList, ProvenanceMetadata metadata)
            throws MipamsException {

        List<JumbfBox> assertionJumbfBoxList = new ArrayList<>();

        for (Assertion assertion : assertionList) {
            assertionJumbfBoxList.add(assertionFactory.convertAssertionToJumbfBox(assertion, metadata));
        }

        return assertionJumbfBoxList;
    }

    private void appendParentIngredientAssertion(String assetUrl, List<JumbfBox> assertionJumbfBoxList,
            JumbfBox currentManifestStore, ProvenanceMetadata metadata) throws MipamsException {

        if (currentManifestStore == null) {
            return;
        }

        JumbfBox activeManifestJumbfBox = ProvenanceUtils.locateActiveManifest(currentManifestStore);

        if (activeManifestJumbfBox != null) {
            String activeManifestId = activeManifestJumbfBox.getDescriptionBox().getLabel();

            IngredientAssertion assertion = new IngredientAssertion();
            assertion.setRelationship(IngredientAssertion.RELATIONSHIP_PARENT_OF);
            assertion.setFormat("application/jpeg");
            assertion.setTitle(assetUrl.substring(assetUrl.lastIndexOf("/") + 1));

            String manifestUri = ProvenanceUtils
                    .getProvenanceJumbfURL(activeManifestId);

            UriReference uriReference = new UriReference();
            uriReference.setUri(manifestUri);
            uriReference.setAlgorithm(UriReference.SUPPORTED_HASH_ALGORITHM);
            uriReference.setDigest(uriReferenceService.getManifestSha256Digest(activeManifestJumbfBox));

            assertion.setManifestReference(uriReference);

            JumbfBox assertionJumbfBox = assertionFactory.convertAssertionToJumbfBox(assertion, metadata);
            assertionJumbfBoxList.add(assertionJumbfBox);
        }

    }

    private void verifyRedactionRequest(JumbfBox currentManifestStore, List<String> redactedAssertionUriList)
            throws MipamsException {

        AssertionStoreContentType assertionStoreContentType = new AssertionStoreContentType();

        for (String redactedAssertionUriRequest : redactedAssertionUriList) {

            JumbfBox manifestJumbfBox = ProvenanceUtils.locateManifestFromUri(currentManifestStore,
                    redactedAssertionUriRequest);

            if (manifestJumbfBox == null) {
                throw new MipamsException(
                        "Manifest not found. Invalid redaction request: " + redactedAssertionUriRequest);
            }

            String manifestId = manifestJumbfBox.getDescriptionBox().getLabel();

            JumbfBox assertionStoreJumbfBox = ProvenanceUtils.getProvenanceJumbfBox(manifestJumbfBox,
                    assertionStoreContentType);

            String assertionStoreLabel = assertionStoreContentType.getLabel();

            String currentAssertionUri, assertionLabel;
            for (BmffBox contentBox : assertionStoreJumbfBox.getContentBoxList()) {
                JumbfBox assertionJumbfBox = (JumbfBox) contentBox;

                assertionLabel = assertionJumbfBox.getDescriptionBox().getLabel();

                currentAssertionUri = ProvenanceUtils.getProvenanceJumbfURL(manifestId, assertionStoreLabel,
                        assertionLabel);

                if (redactedAssertionUriList.contains(currentAssertionUri)) {

                    MipamsAssertion type = MipamsAssertion.getTypeFromLabel(assertionLabel);
                    if (!type.isRedactable()) {
                        throw new MipamsException("Invalid redaction request. Assertion referenced with uri: "
                                + redactedAssertionUriRequest + " can't be redacted");
                    }
                }
            }

        }
    }

    private void appendComponentIngredientManifestIfRequired(String assetUrl, List<JumbfBox> assertionJumbfBoxList,
            List<String> componentIngredientUriList, ProvenanceMetadata metadata) throws MipamsException {

        for (String componentUrl : componentIngredientUriList) {

            File f = new File(componentUrl);

            IngredientAssertion assertion = new IngredientAssertion();
            assertion.setRelationship(IngredientAssertion.RELATIONSHIP_COMPONENT_OF);
            assertion.setFormat("application/jpeg");
            assertion.setTitle(componentUrl.substring(componentUrl.lastIndexOf("/") + 1));

            if (!f.exists()) {
                logger.debug("Component file does not exist: " + componentUrl);
                continue;
            }

            List<JumbfBox> jumbfBoxes = jpegCodestreamParser.parseMetadataFromFile(componentUrl);

            if (!jumbfBoxes.isEmpty()) {
                JumbfBox manifestStore = fakeMediaConsumerService.locateManifestStoreJumbfBox(jumbfBoxes);

                JumbfBox manifestJumbfBox = ProvenanceUtils.locateActiveManifest(manifestStore);

                String manifestUri = ProvenanceUtils
                        .getProvenanceJumbfURL(manifestJumbfBox.getDescriptionBox().getLabel());

                UriReference uriReference = new UriReference();
                uriReference.setUri(manifestUri);
                uriReference.setAlgorithm(UriReference.SUPPORTED_HASH_ALGORITHM);
                uriReference.setDigest(uriReferenceService.getManifestSha256Digest(manifestJumbfBox));

                assertion.setManifestReference(uriReference);
            }

            JumbfBox assertionJumbfBox = assertionFactory.convertAssertionToJumbfBox(assertion, metadata);
            assertionJumbfBoxList.add(assertionJumbfBox);
        }

    }

    private void appendEncryptedAssertionJumbfBoxes(List<JumbfBox> assertionJumbfBoxList,
            List<RedactableAssertion> encryptionAssertionList, ProvenanceMetadata metadata) throws MipamsException {

        for (RedactableAssertion assertion : encryptionAssertionList) {
            assertionJumbfBoxList.add(encryptAssertionService.encrypt(assertion, null, metadata));
        }
    }

    private void appendEncryptedAssertionJumbfBoxesWithAccessRules(List<JumbfBox> assertionJumbfBoxList,
            List<RedactableAssertion> encryptionAssertionList, ProvenanceMetadata metadata) throws MipamsException {

        for (RedactableAssertion assertion : encryptionAssertionList) {
            String arLabel = CoreUtils.randomStringGenerator();
            assertionJumbfBoxList.add(encryptAssertionService.encrypt(assertion, arLabel, metadata));
            assertionJumbfBoxList.add(encryptAssertionService.defineAccessRulesForAssertion(arLabel, null, metadata));
        }

    }

    private ProvenanceSigner getProvenanceSigner(UserDetails userDetails) throws MipamsException {

        final String userCredentialsPath = CoreUtils.getFullPath(CREDENTIALS_PATH, userDetails.getUsername());

        final String userCertificateUrl = CoreUtils.getFullPath(userCredentialsPath,
                String.format(CERTIFICATE_FILENAME_FORMAT, userDetails.getUsername()));

        final String userKeyUrl = CoreUtils.getFullPath(userCredentialsPath,
                String.format(KEY_FILENAME_FORMAT, userDetails.getUsername()));

        try {
            X509Certificate cert = credentialsReaderService.getCertificate(userCertificateUrl);

            PublicKey pubKey = cert.getPublicKey();
            PrivateKey privKey = credentialsReaderService.getPrivateKey(userKeyUrl);

            KeyPair kp = new KeyPair(pubKey, privKey);

            ProvenanceSigner signer = new ProvenanceSigner();
            signer.setSigningScheme("SHA1withRSA");
            signer.setSigningCredentials(kp);
            signer.setSigningCertificate(cert);

            return signer;
        } catch (CryptoException e) {
            throw new MipamsException(e);
        }
    }

}
