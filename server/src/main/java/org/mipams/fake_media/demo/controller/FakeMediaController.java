package org.mipams.fake_media.demo.controller;

import org.mipams.fake_media.demo.entities.requests.UploadRequest;
import org.mipams.fake_media.demo.services.FakeMediaConsumerService;
import org.mipams.fake_media.demo.services.FileHandler;
import org.mipams.fake_media.demo.services.FakeMediaProducerService;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping(path = "/publicInspection", method = RequestMethod.POST, consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE }, produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> publicInspection(@RequestParam(required = false) String digitalAssetFileId,
            @RequestParam(required = false) Boolean fullMode, @ModelAttribute UploadRequest request)
            throws MipamsException {

        try {
            String fileUrl;
            if (digitalAssetFileId == null) {
                fileUrl = fileHandler.saveFileToDiskAndGetFileUrl(request, true);
                logger.debug("Upload file " + fileUrl);
            } else {
                fileUrl = fileHandler.checkFileNameExistanceAndGetFileUrl(digitalAssetFileId);
                logger.debug("File is already uploaded: " + fileUrl);
            }

            boolean fullInspection = (fullMode != null) && fullMode.booleanValue();
            String result = fakeMediaConsumerService.inspect(fileUrl, fullInspection, null);

            logger.debug(result);
            return ResponseEntity.ok().body(result);
        } catch (MipamsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequestMapping(path = "/inspectDigitalAsset", method = RequestMethod.POST, consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE }, produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<?> inspectProvenance(Authentication authentication,
            @RequestParam(required = false) String digitalAssetFileId,
            @RequestParam(required = false) Boolean fullMode, @ModelAttribute UploadRequest request)
            throws MipamsException {

        boolean fullInspection = (fullMode != null) && fullMode.booleanValue();
        UserDetails userDetails = userDetailsService.loadUserByUsername(authentication.getName());

        try {
            String fileUrl = uploadIfNeedded(digitalAssetFileId, request);
            String result = fakeMediaConsumerService.inspect(fileUrl, fullInspection, userDetails);
            logger.debug(result);
            return ResponseEntity.ok().body(result);
        } catch (MipamsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String uploadIfNeedded(String digitalAssetFileId, UploadRequest request) throws MipamsException {

        String fileUrl;
        if (digitalAssetFileId == null) {
            fileUrl = fileHandler.saveFileToDiskAndGetFileUrl(request, true);
            logger.debug("Upload file " + fileUrl);
        } else {
            fileUrl = fileHandler.checkFileNameExistanceAndGetFileUrl(digitalAssetFileId);
            logger.debug("File is already uploaded: " + fileUrl);
        }

        return fileUrl;
    }

    @GetMapping("produce")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<?> getProduce(@RequestParam String digitalAssetFileId) throws MipamsException {
        try {
            String digitalAssetUrl = fileHandler.checkFileNameExistanceAndGetFileUrl(digitalAssetFileId);

            String result = digitalAssetUrl;
            logger.debug(result);

            return ResponseEntity.ok().body(result);
        } catch (MipamsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
