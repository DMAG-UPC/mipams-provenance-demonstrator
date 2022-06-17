import React from "react";
import { Step, Stepper, StepLabel, Button, Box, Container, Snackbar, Alert, Typography } from '@mui/material'
import { styled } from '@mui/material/styles';
import NavBar from '../NavBar';
import AssetEditor from "./AssetEditor";
import MetadataViewer from "../../containers/MetadataViewer";
import UploadAsset from "./UploadAsset";
import Recap from "./Recap";

const StyledBox = styled(Box)(({ theme }) => ({
    padding: theme.spacing(1),
    marginTop: theme.spacing(2),
}));


const steps = ['Upload an asset', 'Modify asset', 'Additional metadata', 'Provenance History'];

function ProducerLayout(props) {

    const { activeStep, editorRef, loading, errorMessage, setErrorMessage, onNextClick, onAlertClose, onResetClick } = props;

    const { onFileUploadChange } = props;

    const { assetName, modifiedAssetName, assertionList, protectedAssertionList } = props;

    const { metadata, setMetadata, metadataStatus, setMetadataStatus } = props;

    const { outputAssetName, setOutputAssetName } = props;

    let stepElement = null;
    if (activeStep === 0) {
        stepElement = <UploadAsset
            fullPage
            loading={loading}
            onFileUploadChange={onFileUploadChange}
            onFileUploadClick={() => { }}
        />
    } else if (activeStep === 1) {
        stepElement = <AssetEditor editorRef={editorRef} />;
    } else if (activeStep === 2) {
        stepElement = <MetadataViewer
            assetName={assetName}
            setErrorMessage={setErrorMessage}
            metadata={metadata}
            setMetadata={setMetadata}
            metadataStatus={metadataStatus}
            setMetadataStatus={setMetadataStatus}
        />
    } else if (activeStep === 3) {
        stepElement = <Recap
            assetName={assetName}
            modifiedAssetName={modifiedAssetName}
            assertionList={assertionList}
            protectedAssertionList={protectedAssertionList}
            outputAssetName={outputAssetName}
            setOutputAssetName={setOutputAssetName}
        />
    }

    return (
        <React.Fragment>
            <NavBar />
            <StyledBox
                sx={{
                    flexGrow: 1,
                    display: 'flex',
                    flexDirection: 'column',
                    alignContent: 'center',
                    justifyContent: 'center'
                }}
            >
                <Stepper
                    activeStep={activeStep}
                    sx={{
                        marginBottom: '16px'
                    }}>
                    {steps.map((label) => {
                        const stepProps = {};
                        const labelProps = {};

                        return (
                            <Step key={label} {...stepProps}>
                                <StepLabel {...labelProps}>{label}</StepLabel>
                            </Step>
                        );
                    })}
                </Stepper>
                {activeStep === steps.length ? (
                    <React.Fragment>
                        <Typography sx={{ mt: 2, mb: 1 }}>
                            All steps completed - you&apos;re finished
                        </Typography>
                        <Box sx={{ display: 'flex', flexDirection: 'row', pt: 2 }}>
                            <Box sx={{ flex: '1 1 auto' }} />
                            <Button onClick={onResetClick}>Reset</Button>
                        </Box>
                    </React.Fragment>) :
                    <Container sx={{
                        height: '70vh'
                    }}>
                        {stepElement}
                        <Box sx={{ display: 'flex', flexDirection: 'row', pt: 2 }}>
                            <Box sx={{ flex: '1 1 auto' }} />

                            <Button onClick={onNextClick} disabled={loading}>
                                {activeStep === steps.length - 1 ? 'Generate Provenance' : 'Next'}
                            </Button>
                        </Box>
                    </Container>}
                <Snackbar open={!!errorMessage} autoHideDuration={6000} onClose={onAlertClose}>
                    <Alert severity="error" onClose={onAlertClose} sx={{ flex: '1' }}>
                        Error parsing the file: {errorMessage}
                    </Alert >
                </Snackbar>
            </StyledBox >
        </React.Fragment>
    );
}

export default ProducerLayout;