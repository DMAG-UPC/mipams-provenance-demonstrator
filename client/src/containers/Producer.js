import React, { useState, createRef } from 'react'
import ProducerLayout from '../components/producer/ProducerLayout';
import api from '../helpers/api';
import { getActionAssertions, getHistoryList } from '../helpers/tools';

const Producer = () => {

    const editorRef = createRef();

    const [activeStep, setActiveStep] = useState(0);
    const [assertionList, setAssertionList] = useState([]);
    const [loading, setLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState();
    const [assetName, setAssetName] = useState();

    function alertCloseHandler() {
        setErrorMessage();
    }

    const nextHandler = () => {

        if (activeStep === 0) {

            let actionList = getHistoryList();

            if (actionList.filter(x => (x.indexOf("(Remove)") !== -1)).length > 0) {
                setErrorMessage("Cannot proceed if there are actions that were removed during processing. It has been fixed now. Try again carefully.");
                resetHandler();
                return;
            }

            let assertionList = getActionAssertions(actionList);
            setAssertionList(assertionList);

            let editorInstance = editorRef.current.getInstance();

            var contentBase64 = editorInstance.toDataURL({ format: "jpeg" });

            contentBase64 = contentBase64.replace("data:image/jpeg;base64,", "");

            const requestBody = { "base64Content": contentBase64 };

            setLoading(true);
            // Upload file and get asset name
            api.post("/metadata/uploadContent", requestBody).then(response => {
                setLoading(false)
                setAssetName(response.data);
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
        let editorInstance = editorRef.current.getInstance();
        editorInstance._clearHistory();

        setActiveStep(0);
    };

    const backHandler = () => {
        setActiveStep((prevActiveStep) => prevActiveStep - 1);
    };

    return (
        <ProducerLayout
            activeStep={activeStep}
            editorRef={editorRef}
            assetName={assetName}
            loading={loading}
            errorMessage={errorMessage}
            onAlertClose={alertCloseHandler}
            onNextClick={nextHandler}
            onBackClick={backHandler}
            onResetClick={resetHandler}
        />
    )
}

export default Producer