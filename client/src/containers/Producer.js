import React, { useState, createRef, useEffect } from 'react'
import ProducerLayout from '../components/producer/ProducerLayout';
import api from '../helpers/api';
import { getActionAssertions, getMetadataAssertions, getHistoryList } from '../helpers/tools';

const Producer = () => {

    const editorRef = createRef();

    const [uploadedFileName, setUploadedFileName] = useState();
    const [activeStep, setActiveStep] = useState(0);
    const [loading, setLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState();

    const [assertionList, setAssertionList] = useState([]);
    const [metadata, setMetadata] = useState();
    const [metadataStatus, setMetadataStatus] = useState("unprotected");
    const [protectedAssertionList, setProtectedAssertionList] = useState([]);
    const [assetName, setAssetName] = useState();
    const [modifiedAssetName, setModifiedAssetName] = useState();
    const [outputAssetName, setOutputAssetName] = useState("test.jpg");

    function alertCloseHandler() {
        setErrorMessage();
    }

    function fileUploadChangeHandler(event) {
        setUploadedFileName(event.target.files[0]);
    }

    useEffect(() => {
        if (uploadedFileName) {
            setLoading(true);

            const formData = new FormData();

            formData.append(
                "file",
                uploadedFileName,
                uploadedFileName.name
            );

            api.post("/metadata/upload", formData).then(response => {
                setAssetName(response.data);
                setErrorMessage(null);
                nextHandler();
                setLoading(false);
            }).catch(error => {
                setAssetName();
                setLoading(false);
                if (typeof (error.response.data) === 'string') {
                    setErrorMessage(error.response.data);
                } else {
                    setErrorMessage(error.message);
                }
            });
        }
    }, [uploadedFileName])

    const nextHandler = () => {

        if (activeStep === 1) {

            var actionList = getHistoryList();

            if (actionList.filter(x => (x.indexOf("(Remove)") !== -1)).length > 0) {
                setErrorMessage("Cannot proceed if there are actions that were removed during processing. It has been fixed now. Try again carefully.");
                resetHandler();
                return;
            }

            var actionAssertionList = getActionAssertions(actionList);
            setAssertionList(actionAssertionList);

            var editorInstance = editorRef.current.getInstance();

            var contentBase64 = editorInstance.toDataURL({ format: "jpeg" });

            contentBase64 = contentBase64.replace("data:image/jpeg;base64,", "");

            const requestBody = { "base64Content": contentBase64 };

            setLoading(true);
            // Upload file and get asset name
            api.post("/metadata/uploadContent", requestBody).then(response => {
                setLoading(false)
                setModifiedAssetName(response.data);
            }).catch(error => {
                setLoading(false);
                if (typeof (error.response.data) === 'string') {
                    setErrorMessage(error.response.data);
                } else {
                    setErrorMessage(error.message);
                }
            });
        }

        if (activeStep === 2) {
            // Create metadata assertions and check where to append them (protected request or not)
            if (metadata && metadata.length > 0) {
                var metadataAssertionList = getMetadataAssertions(metadata);

                if (metadataStatus === "protected") {
                    setProtectedAssertionList(metadataAssertionList);
                } else {
                    setAssertionList((oldList) => [...oldList, ...metadataAssertionList]);
                }
            }
        }

        if (activeStep === 3) {

            if (!outputAssetName) {
                setErrorMessage("Output File Name field cannot be empty");
                return;
            }


            setLoading(true);

            const requestBody = {
                "assetUrl": "/app/assets/" + assetName,
                "modifiedAssetUrl": "/app/assets/" + modifiedAssetName,
                "assertionList": assertionList,
                "encryptionAssertionList": [],
                "encryptionWithAccessRulesAssertionList": (protectedAssertionList) ? protectedAssertionList : [],
                "redactedAssertionUriList": [],
                "componentIngredientUriList": [],
                "outputAssetName": outputAssetName
            }

            console.log(requestBody);

            api.post("/mipams-provenance/produce", requestBody).then(response => {
                setLoading(false)
                setModifiedAssetName(response.data);
            }).catch(error => {
                setLoading(false);
                if (typeof (error.response.data) === 'string') {
                    setErrorMessage(error.response.data);
                } else {
                    setErrorMessage(error.message);
                }
            });

        }

        setActiveStep((prevActiveStep) => prevActiveStep + 1);
    };

    const resetHandler = () => {
        if (editorRef && editorRef.current) {
            var editorInstance = editorRef.current.getInstance();
            editorInstance._clearHistory();
        }

        setActiveStep(0);
    };

    return (
        <ProducerLayout
            activeStep={activeStep}
            editorRef={editorRef}
            assetName={assetName}
            modifiedAssetName={modifiedAssetName}
            metadata={metadata}
            setMetadata={setMetadata}
            metadataStatus={metadataStatus}
            setMetadataStatus={setMetadataStatus}
            loading={loading}
            errorMessage={errorMessage}
            setErrorMessage={setErrorMessage}
            onFileUploadChange={fileUploadChangeHandler}
            assertionList={assertionList}
            protectedAssertionList={protectedAssertionList}
            setProtectedAssertionList={setProtectedAssertionList}
            onAlertClose={alertCloseHandler}
            onNextClick={nextHandler}
            onResetClick={resetHandler}
            outputAssetName={outputAssetName}
            setOutputAssetName={setOutputAssetName}
        />
    )
}

export default Producer