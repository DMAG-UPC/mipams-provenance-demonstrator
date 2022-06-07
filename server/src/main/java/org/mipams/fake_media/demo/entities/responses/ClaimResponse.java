package org.mipams.fake_media.demo.entities.responses;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class ClaimResponse {
    private @Getter @Setter String signedBy;
    private @Getter @Setter String signedGeneratorDescription;
}
