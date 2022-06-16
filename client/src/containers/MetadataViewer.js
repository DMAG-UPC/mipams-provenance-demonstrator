import React, { useEffect, useState } from 'react'
import MetadataViewerLayout from '../components/producer/MetadataViewerLayout';
import api from '../helpers/api';
import { getMetadataList } from '../helpers/tools';

const MetadataViewer = (props) => {

    const { assetName, setErrorMessage } = props;
    const { metadata, setMetadata, metadataStatus, setMetadataStatus } = props;

    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (assetName) {
            setLoading(true);
            api.get("/metadata/getExifMetadata/" + assetName).then(response => {
                setLoading(false)
                setMetadata(getMetadataList(response.data));
            }).catch(error => {
                setLoading(false);
                if (typeof (error.response.data) === 'string') {
                    setErrorMessage(error.response.data);
                } else {
                    setErrorMessage(error.message);
                }
            });
        }
    }, [assetName]);

    return (
        <MetadataViewerLayout
            metadata={metadata}
            loading={loading}
            metadataStatus={metadataStatus}
            setMetadataStatus={setMetadataStatus}
        />
    )
}

export default MetadataViewer