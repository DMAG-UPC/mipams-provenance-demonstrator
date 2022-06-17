import { Box, CircularProgress, Grid } from '@mui/material'
import React from 'react'
import ImageHolder from './ImageHolder';
import ProvenanceAnalysis from './ProvenanceAnalysis';

const Inspection = (props) => {

    const { assetName, provenanceInfo, loading, fullInspectionAllowed, onFullInspectionClick } = props;

    const imageUrl = (assetName) && "http://localhost:8080/metadata/asset/" + assetName;

    const provenanceElement = loading ?
        <Box sx={{
            height: '100%',
            display: 'grid',
            justifyContent: 'center',
            alignItems: 'center'
        }}>
            <CircularProgress />
        </Box> :
        <ProvenanceAnalysis
            info={provenanceInfo}
            loading={loading}
            fullInspectionAllowed={fullInspectionAllowed}
            onFullInspectionClick={onFullInspectionClick}
        />

    return (
        <Grid container spacing={2}>
            <Grid item xs={8}>
                <ImageHolder imageUrl={imageUrl} />
            </Grid>
            <Grid item xs={4}>
                {provenanceElement}
            </Grid>
        </Grid>
    )
}

export default Inspection