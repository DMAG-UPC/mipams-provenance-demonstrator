package org.mipams.fake_media.demo.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mipams.fake_media.demo.entities.responses.ProvenanceResponse;
import org.mipams.fake_media.entities.assertions.Assertion;
import org.mipams.fake_media.entities.requests.ConsumerRequest;
import org.mipams.fake_media.services.ProvenanceConsumer;
import org.mipams.jumbf.core.entities.JumbfBox;
import org.mipams.jumbf.core.services.JpegCodestreamParser;
import org.mipams.jumbf.core.util.MipamsException;
import org.mipams.jumbf.crypto.services.KeyReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProvenanceConsumerService {

    @Autowired
    JpegCodestreamParser jpegCodestreamParser;

    @Autowired
    ProvenanceConsumer provenanceConsumer;

    @Autowired
    KeyReaderService keyReaderService;

    public String inspect(String digitalAssetUrl) throws MipamsException {
        // 1. Extract Jumbf Boxes from digital asset
        List<JumbfBox> boxList = jpegCodestreamParser.parseMetadataFromFile(digitalAssetUrl);

        // 2. Locate Active Manifest
        JumbfBox activeManifestBox = boxList.remove(0);

        if (activeManifestBox == null) {
            return prepareResponse(digitalAssetUrl, new ArrayList<>());
        } else {
            ConsumerRequest consumerRequest = new ConsumerRequest();
            consumerRequest.setAssetUrl(digitalAssetUrl);
            consumerRequest.setManifestContentTypeJumbfBox(activeManifestBox);

            // 3. Verify and Extract
            List<Assertion> extractedAssertionList = provenanceConsumer
                    .verifyAndConsumeManifestJumbfBox(consumerRequest);
            return prepareResponse(digitalAssetUrl, extractedAssertionList);
        }
    }

    private String prepareResponse(String digitalAssetUrl, List<Assertion> assertionList) throws MipamsException {

        File f = new File(digitalAssetUrl);
        final String digitalAssetId = f.getName();

        ProvenanceResponse provenanceResponse = new ProvenanceResponse(digitalAssetId, assertionList);

        ObjectMapper mapper = new ObjectMapper();
        String result;
        try {
            result = mapper.writeValueAsString(provenanceResponse);
        } catch (JsonProcessingException e) {
            throw new MipamsException(e.getMessage());
        }

        return result;
    }
}
