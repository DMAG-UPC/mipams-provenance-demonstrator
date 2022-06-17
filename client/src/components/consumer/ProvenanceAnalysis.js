import { Button, Stack } from '@mui/material'
import React from 'react'
import ProvenanceStatus from './ProvenanceStatus';

import ManifestView from './ManifestView';

const ProvenanceAnalysis = (props) => {

    const { info, loading, fullInspectionAllowed, onFullInspectionClick } = props;

    var i = -1;
    const manifestElementList = info && info.map(manifest => {
        i++;
        return (
            <ManifestView key={manifest.manifestId} info={manifest} isActive={i === 0} />
        )
    });

    const fullInspectionElement = (info && info.length > 0) &&
        <Button disabled={loading || !fullInspectionAllowed} sx={{ marginTop: '8px' }} variant="outlined" component="span" onClick={onFullInspectionClick}>
            Full Inspection
        </Button>

    return (
        <Stack
            direction="column"
            spacing={0}
            sx={{
                height: '70vh',
                overflowX: 'auto',
                overflowY: 'auto'
            }}
        >
            <ProvenanceStatus info={info} />

            {manifestElementList}

            {fullInspectionElement}
        </Stack>
    )
}

export default ProvenanceAnalysis