import React, { useState, useEffect } from 'react'
import AssetViewLayout from '../components/producer/AssetViewLayout';
import api from '../helpers/api';

const AssetView = React.forwardRef((props, ref) => {

    const [assetList, setAssetList] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {

        setLoading(true);

        api.get("/mipams-provenance/listfiles").then(response => {
            console.log(response);
            setLoading(false);

            var resultList = [];
            for (var i in response.data) {
                resultList.push({ "id": i, "name": response.data[i] });
            }

            setAssetList(resultList);
        }).catch(error => {
            setLoading(false);
            console.log(error);
            setAssetList([]);
        });


    }, [])


    return (
        <AssetViewLayout assetList={assetList} loading={loading} />
    )
})

export default AssetView