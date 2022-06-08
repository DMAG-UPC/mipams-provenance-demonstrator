package org.mipams.fake_media.demo.entities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.mipams.jumbf.core.util.MipamsException;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class ExifToolCommand {

    private @NonNull String exifToolPath;

    public static final String PERL_PATH = "/usr/bin/perl";

    public String execute(String... params) throws MipamsException {

        String[] cmd = new String[params.length + 2];

        cmd[0] = PERL_PATH;
        cmd[1] = exifToolPath;

        System.arraycopy(params, 0, cmd, 2, params.length);

        try {
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;

            StringBuilder result = new StringBuilder();
            StringBuilder errorBuffer = new StringBuilder();

            while ((line = errorReader.readLine()) != null) {
                errorBuffer.append(line);
            }

            if (errorBuffer.length() > 0) {
                throw new MipamsException(errorBuffer.toString());
            }

            while ((line = outputReader.readLine()) != null) {
                result.append(line);
            }

            return result.toString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
