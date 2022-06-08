package org.mipams.fake_media.demo.services;

import org.mipams.fake_media.demo.entities.ExifToolCommand;
import org.mipams.jumbf.core.util.MipamsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExifToolService {

    @Value("${org.mipams.fake_media.demo.exiftool_path}")
    public String EXIF_TOOL_PATH;

    public String parseExifMetadata(String digitalAssetUrl) throws MipamsException {
        ExifToolCommand command = new ExifToolCommand(EXIF_TOOL_PATH);
        return command.execute("-j", "-exif*", digitalAssetUrl);
    }
}
