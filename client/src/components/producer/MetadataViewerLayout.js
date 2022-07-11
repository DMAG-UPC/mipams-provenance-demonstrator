import React from 'react'
import { styled } from '@mui/material/styles';
import { Box } from '@mui/system';
import { CircularProgress, Typography, FormControl, FormLabel, RadioGroup, FormControlLabel, Radio } from '@mui/material';

import ExifMetadataTable from '../ExifMetadataTable';

const StyledBox = styled(Box)(({ theme }) => ({
    padding: theme.spacing(1),
    paddingLeft: theme.spacing(5),
    marginTop: theme.spacing(2),
    display: 'flex'
}));

const MetadataViewerLayout = (props) => {

    const { metadata, metadataStatus, setMetadataStatus, loading } = props;

    return (
        <StyledBox sx={{ height: '70vh' }}>
            {loading && <CircularProgress />}
            <Box sx={{ width: '60%', height: '70vh', overflowY: 'auto', overflowX: 'auto' }}>
                <ExifMetadataTable metadata={metadata} />
            </Box>
            <StyledBox sx={{ flexDirection: 'column' }}>
                {(metadata && metadata.length > 0) ?
                    <React.Fragment>
                        <Typography>
                            The metadata listed in the table are going to be included in the Provenance Hisory of the asset. Do you want to protect the access on this information?
                        </Typography>
                        <FormControl>
                            <FormLabel id="demo-controlled-radio-buttons-group">Protection level</FormLabel>
                            <RadioGroup
                                row
                                aria-labelledby="demo-controlled-radio-buttons-group"
                                name="controlled-radio-buttons-group"
                                value={metadataStatus}
                                onChange={(e) => { setMetadataStatus(e.target.value) }}
                            >
                                <FormControlLabel value="unprotected" control={<Radio />} label="Unprotected" />
                                <FormControlLabel value="protected-level1" control={<Radio />} label="Access to Users" />
                                <FormControlLabel value="protected-level2" control={<Radio />} label="Access to Producers ONLY" />
                            </RadioGroup>
                        </FormControl>
                    </React.Fragment> : <Typography> No additional metadata were found in the digital asset</Typography>}
            </StyledBox>
        </StyledBox>
    )
}

export default MetadataViewerLayout