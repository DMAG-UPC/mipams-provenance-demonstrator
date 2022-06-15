import React, { useEffect, useState } from 'react'
import ConsumerLayout from '../components/consumer/ConsumerLayout';
import api from '../helpers/api';

const Consumer = () => {
    const [loading, setLoading] = useState(false);
    const [fullInspectionAllowed, setFullInspectionAllowed] = useState(true);
    const [errorMessage, setErrorMessage] = useState();
    const [uploadedFileName, setUploadedFileName] = useState();
    const [assetName, setAssetName] = useState();
    const [provenanceInfo, setProvenanceInfo] = useState();

    function fileUploadChangeHandler(event) {
        setUploadedFileName(event.target.files[0]);
    }

    function fileUploadClickHandler(event) {
        console.log("Clicked");
        // Possibly we shall need it to reset something
    }

    function fullInspectionClickHandler(event) {
        setFullInspectionAllowed(false);
        // TODO execute fullInspection and update provenanceInfo
    }

    function alertCloseHandler() {
        setErrorMessage();
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

            // Request made to the backend api Send formData object

            api.post("/metadata/upload", formData).then(response => {
                setAssetName(response.data);
                setErrorMessage(null);

                api.get("/metadata/inspect/" + response.data).then(response => {
                    setLoading(false);
                    setProvenanceInfo(JSON.parse(response.data));
                })

            }).catch(error => {
                setProvenanceInfo();
                setLoading(false);
                if (typeof (error.response.data) === 'string') {
                    setErrorMessage(error.response.data);
                } else {
                    setErrorMessage(error.message);
                }
            });
        }
    }, [uploadedFileName])

    return (
        <ConsumerLayout
            assetName={assetName}
            provenanceInfo={provenanceInfo}
            fullInspectionAllowed={fullInspectionAllowed}
            onFullInspectionClick={fullInspectionClickHandler}
            errorMessage={errorMessage}
            onAlertClose={alertCloseHandler}
            loading={loading}
            onFileUploadChange={fileUploadChangeHandler}
            onFileUploadClick={fileUploadClickHandler}
        />
    )
}

export default Consumer