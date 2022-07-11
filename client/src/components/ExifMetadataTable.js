import React from 'react'
import { Paper, TableContainer, Table, TableHead, TableRow, TableBody, TableCell } from '@mui/material';


const ExifMetadataTable = React.forwardRef((props, ref) => {

    const { metadata } = props;

    var tableBody = null;

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
        <TableContainer component={Paper}>
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
    )
});

export default ExifMetadataTable