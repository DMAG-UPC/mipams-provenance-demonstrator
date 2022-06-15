import React from "react";
import { Step, Stepper, StepLabel, Button, Box, Container, Snackbar, Alert } from '@mui/material'
import { styled } from '@mui/material/styles';
import NavBar from '../NavBar';
import AssetEditor from "./AssetEditor";

const StyledBox = styled(Box)(({ theme }) => ({
    padding: theme.spacing(1),
    marginTop: theme.spacing(2),
}));


const steps = ['Upload an asset', 'Additional metadata', 'Provenance History'];

function ProducerLayout(props) {

    const {
        activeStep,
        editorRef,
        loading,
        assetName,
        assertions,
        setAssertions,
        errorMessage,
        onAlertClose,
        uploadImageHandler,
        onBackClick,
        onResetClick,
        onNextClick } = props;

    let stepElement = null;

    if (activeStep === 0) {
        // Apply Modifications
        stepElement = <AssetEditor
            activeStep={activeStep}
            editorRef={editorRef}
            setAssertions={setAssertions}
        />;
    } else if (activeStep === 1) {
        // Review and encrypt metadata; add access rules
    } else if (activeStep === 2) {
        // print whole history and check redactions
        stepElement = null;
    }

    // Output name and generate element

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
                <Container sx={{
                    height: '70vh'
                }}>
                    {stepElement}
                    <Box sx={{ display: 'flex', flexDirection: 'row', pt: 2 }}>
                        <Button
                            color="inherit"
                            disabled={activeStep === 0}
                            onClick={onBackClick}
                            sx={{ mr: 1 }}
                        >
                            Back
                        </Button>
                        <Box sx={{ flex: '1 1 auto' }} />

                        <Button onClick={onNextClick} disabled={loading}>
                            {activeStep === steps.length - 1 ? 'Generate Provenance' : 'Next'}
                        </Button>
                    </Box>
                </Container>
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