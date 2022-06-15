import React from 'react'
import { Box } from '@mui/material'
import { styled } from '@mui/material/styles';
import { Button } from '@mui/material';

const StyledBox = styled(Box)(({ theme }) => ({
    padding: theme.spacing(1),
}));

const Input = styled('input')({
    display: 'none',
});

const UploadAsset = (props) => {

    const { loading, onFileUploadChange, onFileUploadClick, fullPage } = props;

    return (
        <StyledBox
            sx={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                height: (fullPage) ? '100%' : 'auto'
            }}

        >
            <label htmlFor="contained-button-file">
                <Input accept="image/jpeg" id="contained-button-file" multiple type="file" onChange={onFileUploadChange} />
                <Button disabled={loading} variant="contained" component="span" onClick={onFileUploadClick}>
                    Upload Asset
                </Button>
            </label>
        </StyledBox>
    )
}

export default UploadAsset