import React from 'react'
import { styled } from '@mui/material/styles';
import { Box } from '@mui/system';
import { Paper, TableContainer, Table, TableHead, TableRow, TableBody, TableCell, CircularProgress, Typography, FormControl, FormLabel, RadioGroup, FormControlLabel, Radio } from '@mui/material';


const StyledBox = styled(Box)(({ theme }) => ({
    padding: theme.spacing(1),
    marginTop: theme.spacing(2),
    display: 'flex'
}));

const MetadataViewerLayout = (props) => {

    const { metadata, metadataStatus, setMetadataStatus, loading } = props;

    let tableBody = null;

    if (metadata && metadata.length > 0) {
        tableBody = metadata.map(entry => (
            <TableRow key={entry.id} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                <TableCell component="th" scope="row">
                    {entry.key}
                </TableCell>
                <TableCell component="th" scope="row">
                    {entry.val}
                </TableCell>
            </TableRow>
        ))
    }

    return (
        <StyledBox sx={{ height: '70vh' }}>
            {loading && <CircularProgress />}
            <TableContainer sx={{ width: '60%', overflowY: 'auto', overflowX: 'auto' }} component={Paper}>
                <Table size="small" aria-label="a dense table">
                    <TableHead>
                        <TableRow>
                            <TableCell sx={{ fontWeight: "bold" }}>Metadata Key</TableCell>
                            <TableCell sx={{ fontWeight: "bold" }}>Metadata Value</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {tableBody}
                    </TableBody>
                </Table>
            </TableContainer>
            <StyledBox sx={{ flexDirection: 'column' }}>
                {(metadata && metadata.length > 0) ?
                    <React.Fragment>
                        <Typography>
                            The metadata listed in the table are going to be included in the Provenance Hisory of the asset. Do you want to protect the access on this information?
                        </Typography>
                        <FormControl>
                            <FormLabel id="demo-controlled-radio-buttons-group">Metadata status</FormLabel>
                            <RadioGroup
                                aria-labelledby="demo-controlled-radio-buttons-group"
                                name="controlled-radio-buttons-group"
                                value={metadataStatus}
                                onChange={(e) => { setMetadataStatus(e.target.value) }}
                            >
                                <FormControlLabel value="protected" control={<Radio />} label="Protected" />
                                <FormControlLabel value="unprotected" control={<Radio />} label="Unprotected" />
                            </RadioGroup>
                        </FormControl>
                    </React.Fragment> : <Typography> No additional metadata were found in the digital asset</Typography>}
            </StyledBox>
        </StyledBox>
    )
}

export default MetadataViewerLayout