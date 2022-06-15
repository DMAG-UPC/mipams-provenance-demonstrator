import React from "react";
import { Box, Paper, Snackbar } from '@mui/material'

import { styled } from '@mui/material/styles';
import { Button, Alert } from '@mui/material';
import NavBar from '../NavBar';

const StyledBox = styled(Box)(({ theme }) => ({
    padding: theme.spacing(1),
}));

function ProducerLayout() {
    return (
        <React.Fragment>
            <NavBar />
            <StyledBox
                sx={{
                    flexGrow: 1,
                    display: 'flex',
                    flexDirection: 'column',
                    height: '80vh',
                    alignContent: 'center',
                    justifyContent: 'center'
                }}
            >

            </StyledBox >
        </React.Fragment>
    );
}

export default ProducerLayout;