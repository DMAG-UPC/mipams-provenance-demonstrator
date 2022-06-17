package org.mipams.fake_media.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.mipams.fake_media.demo.entities.requests.FakeMediaRequest;
import org.mipams.fake_media.demo.entities.responses.FakeMediaResponse;
import org.mipams.fake_media.demo.entities.utils.FakeMediaUtils;
import org.mipams.fake_media.demo.services.FakeMediaConsumerService;
import org.mipams.fake_media.demo.services.FakeMediaProducerService;
import org.mipams.fake_media.demo.services.FileHandler;
import org.mipams.fake_media.demo.services.ProducerInitializer;
import org.mipams.fake_media.entities.responses.ManifestResponse;
import org.mipams.fake_media.entities.responses.ManifestStoreResponse;
import org.mipams.jumbf.core.util.CoreUtils;
import org.mipams.jumbf.core.util.MipamsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/mipams-provenance")
public class FakeMediaController {

    private static final Logger logger = LoggerFactory.getLogger(FakeMediaController.class);

    @Autowired
    FileHandler fileHandler;

    @Autowired
    FakeMediaConsumerService fakeMediaConsumerService;

    @Autowired
    FakeMediaProducerService fakeMediaProducerService;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    ProducerInitializer producerInitializer;

    @RequestMapping(path = "/inspection/{digitalAssetFileId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<?> inspection(Authentication authentication,
            @PathVariable(value = "digitalAssetFileId") final String digitalAssetFileId) throws MipamsException {

        UserDetails userDetails = userDetailsService.loadUserByUsername(authentication.getName());

        try {
            String digitalAssetFileUrl = fileHandler.checkFileNameExistanceAndGetFileUrl(digitalAssetFileId);

            String result = prepareResponse(
                    fakeMediaConsumerService.inspect(digitalAssetFileUrl, false, userDetails));

            logger.debug(result);
            return ResponseEntity.ok().body(result);
        } catch (MipamsException e) {
            return ResponseEntity.badRequest().body(FakeMediaUtils.generateJsonResponseFromString(e.getMessage()));
        }
    }

    @RequestMapping(path = "/fullInspection/{digitalAssetFileId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<?> fullInspection(Authentication authentication,
            @PathVariable(value = "digitalAssetFileId") final String digitalAssetFileId) throws MipamsException {

        UserDetails userDetails = userDetailsService.loadUserByUsername(authentication.getName());

        try {
            String digitalAssetFileUrl = fileHandler.checkFileNameExistanceAndGetFileUrl(digitalAssetFileId);

            String result = prepareResponse(
                    fakeMediaConsumerService.inspect(digitalAssetFileUrl, true, userDetails));

            logger.debug(result);
            return ResponseEntity.ok().body(result);
        } catch (MipamsException e) {
            return ResponseEntity.badRequest().body(FakeMediaUtils.generateJsonResponseFromString(e.getMessage()));
        }
    }

    @RequestMapping(path = "/produce", method = RequestMethod.POST, consumes = {
            MediaType.APPLICATION_JSON_VALUE }, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<?> produce(Authentication authentication, @RequestBody JsonNode requestNode)
            throws MipamsException {
        try {

            FakeMediaRequest request = FakeMediaUtils.populateRequest(requestNode);
            logger.debug(request.getAssetUrl());

            UserDetails userDetails = userDetailsService.loadUserByUsername(authentication.getName());

            String outputDigitalAssetUrl = fakeMediaProducerService.produce(userDetails, request);

            // Rename asset
            String outputDigitalAssetId = outputDigitalAssetUrl.substring(outputDigitalAssetUrl.lastIndexOf("/") + 1);
            logger.info("Initial: " + outputDigitalAssetId);

            String requestDigitalAssetUrl = outputDigitalAssetUrl.replace(outputDigitalAssetId,
                    request.getOutputAssetName());

            logger.info("Copy from: " + outputDigitalAssetId + " to " + requestDigitalAssetUrl);
            requestDigitalAssetUrl = FakeMediaUtils.movefileToDirectory(outputDigitalAssetUrl, requestDigitalAssetUrl);
            logger.info("Result URL: " + requestDigitalAssetUrl);

            // Copy asset to producer's dir
            String userDir = producerInitializer.getUserAssetDirectory(userDetails.getUsername());
            String assetTargetUrl = CoreUtils.getFullPath(userDir, request.getOutputAssetName());
            assetTargetUrl = FakeMediaUtils.copyfileToDirectory(requestDigitalAssetUrl, assetTargetUrl);

            return ResponseEntity.ok().body(FakeMediaUtils.generateJsonResponseFromString(outputDigitalAssetId));
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

    @GetMapping(path = "/listfiles")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<?> listFiles(Authentication authentication) {

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(authentication.getName());

            String producerDir = producerInitializer.getUserAssetDirectory(userDetails.getUsername());
            List<String> files = FakeMediaUtils.getFileList(producerDir);

            return ResponseEntity.ok().body(FakeMediaUtils.generateJsonResponseFromString(files));
        } catch (MipamsException e) {
            return ResponseEntity.badRequest().body(FakeMediaUtils.generateJsonResponseFromString(e.getMessage()));
        }
    }
}
