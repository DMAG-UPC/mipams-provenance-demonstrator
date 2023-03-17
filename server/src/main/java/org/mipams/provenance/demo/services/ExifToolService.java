package org.mipams.provenance.demo.services;

import org.mipams.jumbf.util.MipamsException;
import org.mipams.provenance.demo.entities.ExifToolCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExifToolService {

    @Value("${org.mipams.provenance.demo.exiftool_path}")
    public String EXIF_TOOL_PATH;

    public String parseExifMetadata(String digitalAssetUrl) throws MipamsException {
        ExifToolCommand command = new ExifToolCommand(EXIF_TOOL_PATH);
        return command.execute("-j", digitalAssetUrl);
    }
}
