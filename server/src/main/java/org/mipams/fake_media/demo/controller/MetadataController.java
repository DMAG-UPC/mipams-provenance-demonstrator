package org.mipams.fake_media.demo.controller;

import org.mipams.fake_media.demo.services.ExifToolService;
import org.mipams.fake_media.demo.services.FileHandler;
import org.mipams.jumbf.core.util.MipamsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metadata")
public class MetadataController {

    private static final Logger logger = LoggerFactory.getLogger(MetadataController.class);

    @Autowired
    ExifToolService exifToolService;

    @Autowired
    FileHandler fileHandler;

    @RequestMapping(path = "/getExifMetadata", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
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
}
