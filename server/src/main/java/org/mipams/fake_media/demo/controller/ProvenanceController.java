package org.mipams.fake_media.demo.controller;

import org.mipams.fake_media.demo.entities.requests.UploadRequest;
import org.mipams.fake_media.demo.services.ExifToolService;
import org.mipams.fake_media.demo.services.FileHandler;
import org.mipams.fake_media.demo.services.ProvenanceConsumerService;
import org.mipams.fake_media.demo.services.ProvenanceProducerService;
import org.mipams.jumbf.core.util.MipamsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mipams-provenance")
public class ProvenanceController {

    private static final Logger logger = LoggerFactory.getLogger(ProvenanceController.class);

    @Autowired
    FileHandler fileHandler;

    @Autowired
    ProvenanceConsumerService provenanceConsumerService;

    @Autowired
    ProvenanceProducerService provenanceProducerService;

    @Autowired
    ExifToolService exifToolService;

    @RequestMapping(path = "/inspectDigitalAsset", method = RequestMethod.POST, consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE }, produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<?> inspectProvenance(@RequestParam(required = false) String digitalAssetFileId,
            @ModelAttribute UploadRequest request) throws MipamsException {

        try {
            String fileUrl;
            if (digitalAssetFileId == null) {
                fileUrl = fileHandler.saveFileToDiskAndGetFileUrl(request, true);
                logger.debug("Upload file " + fileUrl);
            } else {
                fileUrl = fileHandler.checkFileNameExistanceAndGetFileUrl(digitalAssetFileId);
                logger.debug("File is already uploaded: " + fileUrl);
            }

            String result = provenanceConsumerService.inspect(fileUrl);
            logger.debug(result);
            return ResponseEntity.ok().body(result);
        } catch (MipamsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequestMapping(path = "/getExifMetadata", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<?> getExifMetadata(@RequestParam String digitalAssetFileId) throws MipamsException {

        try {
            String digitalAssetUrl = fileHandler.checkFileNameExistanceAndGetFileUrl(digitalAssetFileId);

            String result = exifToolService.parseExifMetadata(digitalAssetUrl);
            logger.debug(result);

            return ResponseEntity.ok().body(result);
        } catch (MipamsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequestMapping(path = "/getIptcMetadata", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('CONSUMER')")
    public ResponseEntity<?> getIptcMetadata(@RequestParam String digitalAssetFileId) throws MipamsException {
        try {
            String digitalAssetUrl = fileHandler.checkFileNameExistanceAndGetFileUrl(digitalAssetFileId);

            String result = exifToolService.parseIptcMetadata(digitalAssetUrl);
            logger.debug(result);

            return ResponseEntity.ok().body(result);
        } catch (MipamsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("produce")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<?> getProduce(@RequestParam String digitalAssetFileId) throws MipamsException {
        try {
            String digitalAssetUrl = fileHandler.checkFileNameExistanceAndGetFileUrl(digitalAssetFileId);

            String result = exifToolService.parseIptcMetadata(digitalAssetUrl);
            logger.debug(result);

            return ResponseEntity.ok().body("OK");
        } catch (MipamsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
