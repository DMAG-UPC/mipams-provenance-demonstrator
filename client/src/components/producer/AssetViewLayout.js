import React from 'react'
import DownloadIcon from '@mui/icons-material/Download';
import styled from '@emotion/styled';
import { Box } from '@mui/system';
import { CircularProgress, IconButton, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tooltip, Typography } from '@mui/material';

const StyledBox = styled(Box)(({ theme }) => ({
    padding: theme.spacing(1),
    marginTop: theme.spacing(2),
}));

const AssetViewLayout = (props) => {

    function getDownloadButton(assetName) {

        return (<a
            href={"http://localhost:8080/metadata/download/" + assetName}
            download={assetName}
            style={{ textDecoration: 'none' }}
        >
            <Tooltip title="Download">
                <IconButton>
                    <DownloadIcon />
                </IconButton>
            </Tooltip>
        </a>);
    }

    const { assetList, loading } = props;

    var tableElement =
        <StyledBox sx={{ backgroundColor: 'white' }}>
            <Typography >
                No asset has been found.
            </Typography>
        </StyledBox>

    if (assetList && assetList.length > 0) {
        tableElement = <TableContainer component={Paper}>
            <Table sx={{ minWidth: 650 }} size="small" aria-label="a dense table">
                <TableHead>
                    <TableRow>
                        <TableCell sx={{ fontWeight: "bold" }}>Asset Name</TableCell>
                        <TableCell></TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {assetList.map((row) => (
                        <TableRow
                            key={row.id}
                            sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                        >
                            <TableCell component="th" scope="row">
                                {row.name}
                            </TableCell>
                            <TableCell align="right" component="th" scope="row">
                                {getDownloadButton(row.name)}
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    }



    return (
        <StyledBox sx={{
            maxHeight: '50vh',
            overflowY: 'auto'
        }}>
            {loading && <CircularProgress />}
            {tableElement}
        </StyledBox>
    )
}

export default AssetViewLayout