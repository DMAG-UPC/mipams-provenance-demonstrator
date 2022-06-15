import { Box, Paper, Snackbar } from '@mui/material'
import React from 'react'

import { styled } from '@mui/material/styles';
import { Button, Alert } from '@mui/material';
import NavBar from '../NavBar';
import Inspection from './Inspection';

const StyledBox = styled(Box)(({ theme }) => ({
    padding: theme.spacing(1),
}));

const Input = styled('input')({
    display: 'none',
});

export default function ConsumerLayout(props) {

    const { assetName,
        provenanceInfo,
        fullInspectionAllowed,
        fullInspectionHandler,
        errorMessage,
        onAlertClose,
        loading,
        onFileUploadChange,
        onFileUploadClick } = props;

    const inspectionElement = assetName && <Paper
        elevation={3}
        sx={{
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
            fullInspectionHandler={fullInspectionHandler}
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
                <StyledBox
                    sx={{
                        display: 'flex',
                        justifyContent: 'center',
                        height: '5vh'
                    }}

                >
                    <label htmlFor="contained-button-file">
                        <Input accept="image/jpeg" id="contained-button-file" multiple type="file" onChange={onFileUploadChange} />
                        <Button disabled={loading} variant="contained" component="span" onClick={onFileUploadClick}>
                            Upload Asset
                        </Button>
                    </label>
                </StyledBox>
                <Snackbar open={errorMessage} autoHideDuration={6000} onClose={onAlertClose}>
                    <Alert severity="error" onClose={onAlertClose} sx={{ flex: '1' }}>
                        Error parsing the file: {errorMessage}
                    </Alert >
                </Snackbar>
            </StyledBox >
        </React.Fragment>
    );

}
