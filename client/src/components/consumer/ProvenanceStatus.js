import React from 'react'
import CheckCircleRoundedIcon from '@mui/icons-material/CheckCircleRounded';
import DoNotDisturbAltRoundedIcon from '@mui/icons-material/DoNotDisturbAltRounded';
import ErrorRoundedIcon from '@mui/icons-material/ErrorRounded';
import { styled } from '@mui/material/styles';
import { Stack, Tooltip, Typography } from '@mui/material';

const Status = styled(Stack)(({ theme }) => ({
    margin: theme.spacing(2),
    display: 'flex',
    flexDirection: 'row',
}));

const successStatus =
    <React.Fragment>
        <Tooltip title="Manifest is successfully validated">
            <CheckCircleRoundedIcon
                sx={{
                    color: 'green'
                }} />
        </Tooltip>
        <Typography>
            Validated
        </Typography>
    </React.Fragment>;

const partialSuccessStatus =
    <React.Fragment>
        <Tooltip title="Provenance is partially validated. There are protected assertions.">
            <ErrorRoundedIcon
                sx={{
                    color: 'orange'
                }} />
        </Tooltip>
        <Typography>
            Partially Validated
        </Typography>
    </React.Fragment>;

const nonExistentStatus =
    <React.Fragment>
        <Tooltip title="Provenance history does not exist for this digital asset.">
            <DoNotDisturbAltRoundedIcon
                sx={{
                    color: 'red'
                }} />
        </Tooltip>
        <Typography>
            Manifest does not exist
        </Typography>
    </React.Fragment>;


const ProvenanceStatus = (props) => {

    const { info } = props;

    var statusElement = null;

    if (Boolean(info) && info.length > 0) {
        if (info[0].inaccessibleJumbfBoxLabelList && info[0].inaccessibleJumbfBoxLabelList.length > 0) {
            statusElement = partialSuccessStatus;
        } else {
            statusElement = successStatus;
        }
    } else {
        statusElement = nonExistentStatus;
    }

    return (
        <Status direction="row" spacing={2}>
            {statusElement}
        </Status>
    )
}

export default ProvenanceStatus