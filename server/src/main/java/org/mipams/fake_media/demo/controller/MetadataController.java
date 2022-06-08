package org.mipams.fake_media.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.mipams.fake_media.demo.entities.requests.UploadRequest;
import org.mipams.fake_media.demo.entities.responses.FakeMediaResponse;
import org.mipams.fake_media.demo.entities.utils.FakeMediaUtils;
import org.mipams.fake_media.demo.services.ExifToolService;
import org.mipams.fake_media.demo.services.FakeMediaConsumerService;
import org.mipams.fake_media.demo.services.FileHandler;
import org.mipams.fake_media.entities.responses.ManifestResponse;
import org.mipams.fake_media.entities.responses.ManifestStoreResponse;
import org.mipams.jumbf.core.util.MipamsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metadata")
public class MetadataController {

    private static final Logger logger = LoggerFactory.getLogger(MetadataController.class);

    @Autowired
    ExifToolService exifToolService;

    @Autowired
    FileHandler fileHandler;

    @Autowired
    FakeMediaConsumerService fakeMediaConsumerService;

    @RequestMapping(path = "/upload", method = RequestMethod.POST, consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE }, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> uploadDigitalAsset(@ModelAttribute UploadRequest request) throws MipamsException {

        try {
            String digitalAssetFileUrl = fileHandler.saveFileToDiskAndGetFileUrl(request, true);
            logger.debug(digitalAssetFileUrl);

            String digitalAssetFileId = digitalAssetFileUrl.substring(digitalAssetFileUrl.lastIndexOf("/") + 1);
            return ResponseEntity.ok(FakeMediaUtils.generateJsonResponseFromString(digitalAssetFileId));
        } catch (MipamsException e) {
            return ResponseEntity.badRequest().body(FakeMediaUtils.generateJsonResponseFromString(e.getMessage()));
        }
    }

    @RequestMapping(path = "/getExifMetadata/{digitalAssetFileId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getExifMetadata(
            @PathVariable(value = "digitalAssetFileId") final String digitalAssetFileId) throws MipamsException {

        try {
            String digitalAssetUrl = fileHandler.checkFileNameExistanceAndGetFileUrl(digitalAssetFileId);
            String result = exifToolService.parseExifMetadata(digitalAssetUrl);

            logger.debug(result);
            return ResponseEntity.ok().body(result);
        } catch (MipamsException e) {
            return ResponseEntity.badRequest().body(FakeMediaUtils.generateJsonResponseFromString(e.getMessage()));
        }
    }

    @RequestMapping(path = "/inspect/{digitalAssetFileId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> publicInspection(
            @PathVariable(value = "digitalAssetFileId") final String digitalAssetFileId) throws MipamsException {

        try {
            String digitalAssetFileUrl = fileHandler.checkFileNameExistanceAndGetFileUrl(digitalAssetFileId);

            String result = prepareResponse(
                    fakeMediaConsumerService.inspect(digitalAssetFileUrl, false, null));

            logger.debug(result);
            return ResponseEntity.ok().body(FakeMediaUtils.generateJsonResponseFromString(result));
        } catch (MipamsException e) {
            return ResponseEntity.badRequest().body(FakeMediaUtils.generateJsonResponseFromString(e.getMessage()));
        }
    }

    @RequestMapping(path = "/fullInspect/{digitalAssetFileId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> publicFullInspection(
            @PathVariable(value = "digitalAssetFileId") final String digitalAssetFileId) throws MipamsException {

        try {
            String digitalAssetFileUrl = fileHandler.checkFileNameExistanceAndGetFileUrl(digitalAssetFileId);

            String result = prepareResponse(
                    fakeMediaConsumerService.inspect(digitalAssetFileUrl, true, null));

            logger.debug(result);
            return ResponseEntity.ok().body(result);
        } catch (MipamsException e) {
            return ResponseEntity.badRequest().body(FakeMediaUtils.generateJsonResponseFromString(e.getMessage()));
        }
    }

    private String prepareResponse(ManifestStoreResponse manifestStoreResponse)
            throws MipamsException {

        List<FakeMediaResponse> responseList = new ArrayList<>();

        ManifestResponse manifestResponse;
        for (String manifestId : manifestStoreResponse.getManifestResponseMap().keySet()) {

            manifestResponse = manifestStoreResponse.getManifestResponseMap().get(manifestId);

            FakeMediaResponse fakeMediaResponse = fakeMediaConsumerService.getFakeMediaResponseForManifest(manifestId,
                    manifestResponse);

            responseList.add(fakeMediaResponse);
        }

        return FakeMediaUtils.prepareResponse(responseList);
    }
}