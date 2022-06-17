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

const failureStatus =
    <React.Fragment>
        <Tooltip title="Both Manifest and digital asset might be corrupted.">
            <ErrorRoundedIcon
                sx={{
                    color: 'orange'
                }} />
        </Tooltip>
        <Typography>
            Corrupted Provenance
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

    var statusElement = Boolean(info) && info.length > 0 ? successStatus : nonExistentStatus;

    return (
        <Status direction="row" spacing={2}>
            {statusElement}
        </Status>
    )
}

export default ProvenanceStatus