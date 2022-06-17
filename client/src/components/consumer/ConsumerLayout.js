import { Box, Paper, Snackbar } from '@mui/material'
import React from 'react'

import { styled } from '@mui/material/styles';
import { Alert } from '@mui/material';
import NavBar from '../NavBar';
import Inspection from './Inspection';
import UploadAsset from '../producer/UploadAsset';

const StyledBox = styled(Box)(({ theme }) => ({
    padding: theme.spacing(1),
}));

export default function ConsumerLayout(props) {

    const { assetName,
        provenanceInfo,
        fullInspectionAllowed,
        onFullInspectionClick,
        errorMessage,
        onAlertClose,
        loading,
        onFileUploadChange,
        onFileUploadClick } = props;

    const inspectionElement = assetName && <Paper
        elevation={3}
        sx={{
            flexGrow: 1,
            flex: 'auto',
            overflowY: 'auto',
            overflowX: 'auto',
        }}
    >
        <Inspection
            assetName={assetName}
            provenanceInfo={provenanceInfo}
            loading={loading}
            fullInspectionAllowed={fullInspectionAllowed}
            onFullInspectionClick={onFullInspectionClick}
        />
    </Paper>


    return (
        <React.Fragment>
            <NavBar />
            <StyledBox
                sx={{
                    flexGrow: 1,
                    display: 'flex',
                    flexDirection: 'column',
                    height: '80vh',
                    alignContent: 'center',
                    justifyContent: 'center'
                }}
            >
                {inspectionElement}
                <UploadAsset
                    loading={loading}
                    onFileUploadChange={onFileUploadChange}
                    onFileUploadClick={onFileUploadClick}
                />
                <Snackbar open={errorMessage} autoHideDuration={6000} onClose={onAlertClose}>
                    <Alert severity="error" onClose={onAlertClose} sx={{ flex: '1' }}>
                        Error parsing the file: {errorMessage}
                    </Alert >
                </Snackbar>
            </StyledBox >
        </React.Fragment>
    );

}
