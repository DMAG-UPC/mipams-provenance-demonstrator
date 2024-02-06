import React from 'react'

import { Accordion, AccordionDetails, AccordionSummary, Divider, Stack, Tooltip, Typography } from '@mui/material'
import StarRateIcon from '@mui/icons-material/StarRate';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import BlockIcon from '@mui/icons-material/Block';
import styled from '@emotion/styled';
import { Box } from '@mui/system';

import AssertionView from '../AssertionView';

import { displayManifestId } from '../../helpers/tools';

const StyledBox = styled(Box)(({ theme }) => ({
    padding: theme.spacing(1),
}));

function getOrganizationUnit(text) {

    if (!text) {
        return text;
    }

    const regex = /OU=(\w+)/g;
    return regex.exec(text)[1];
}

const ManifestView = (props) => {

    const { info, isActive } = props

    var assertionListElement = info.assertionList && <AssertionView assertionList={[...info.assertionList, ...info.inaccessibleJumbfBoxLabelList]} />;

    var claimElement = !info.manifestProtected && <StyledBox>
        <Typography >
            <b>Signed by:</b> {getOrganizationUnit(info.claimResponse.signedBy)}
        </Typography>
        <Typography >
            <b>Signed on:</b> {info.claimResponse.signedOn} UTC
        </Typography>
    </StyledBox>


    return (
        <Accordion disabled={info.manifestProtected}>
            <AccordionSummary
                expandIcon={info.manifestProtected ? <BlockIcon />: <ExpandMoreIcon />}
                aria-controls="panel1a-content"
                id="panel1a-header"
            >
                {isActive && <Tooltip title="Active Manifest">
                    <StarRateIcon sx={{ color: 'gold', marginRight: '5px' }} />
                </Tooltip>}
                <Typography>Manifest: {displayManifestId(info.manifestId)} </Typography>
            </AccordionSummary>
            <AccordionDetails>
                <Stack spacing={2} direction="column" divider={<Divider />}>
                    {assertionListElement}
                    {claimElement}
                </Stack>
            </AccordionDetails>
        </Accordion>
    )
}

export default ManifestView