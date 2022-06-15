import { Button, Stack } from '@mui/material'
import React from 'react'
import ProvenanceStatus from './ProvenanceStatus';
import Accordion from '@mui/material/Accordion';
import AccordionSummary from '@mui/material/AccordionSummary';
import AccordionDetails from '@mui/material/AccordionDetails';
import Typography from '@mui/material/Typography';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';

const ProvenanceAnalysis = (props) => {

    const { info, loading, fullInspectionAllowed, fullInspectionHandler } = props;

    const manifestElementList = info && info.map(manifest => {

        return (
            <Accordion key={manifest.manifestId}>
                <AccordionSummary
                    expandIcon={<ExpandMoreIcon />}
                    aria-controls="panel1a-content"
                    id="panel1a-header"
                >
                    <Typography>Accordion 1</Typography>
                </AccordionSummary>
                <AccordionDetails>
                    <Typography>
                        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse
                        malesuada lacus ex, sit amet blandit leo lobortis eget.
                    </Typography>
                </AccordionDetails>
            </Accordion>
        )
    });

    const fullInspectionElement = (info && info.length > 0) &&
        <Button disabled={loading || fullInspectionAllowed} variant="outlined" component="span" onClick={fullInspectionHandler}>
            Full Inspection
        </Button>

    return (
        <Stack
            direction="column"
            spacing={0}
            sx={{
                height: '70vh',
                overflowX: 'auto',
                overflowY: 'auto'
            }}
        >
            <ProvenanceStatus info={info} />

            {manifestElementList}

            {fullInspectionElement}
        </Stack>
    )
}

export default ProvenanceAnalysis