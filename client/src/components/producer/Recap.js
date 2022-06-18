import { Grid, Stack, TextField, Typography } from '@mui/material';
import React, { useEffect, useState } from 'react'
import AssertionView from '../AssertionView';



const Recap = (props) => {

    const [assertionListEl, setAssertionListEl] = useState();
    const [protectedAssertionListEl, setProtectedAssertionListEl] = useState();

    const { assertionList, protectedAssertionList, outputAssetName, setOutputAssetName } = props;

    useEffect(() => {
        console.log(1);
        if (assertionList) {
            setAssertionListEl(<Grid item xs={12} md={6}>
                <Typography sx={{ mt: 4, mb: 2 }} variant="h6" component="div">
                    Assertions to be embedded
                </Typography>
                <AssertionView assertionList={assertionList} />
            </Grid>);
        }
    }, [assertionList]);

    useEffect(() => {
        console.log(2);
        if (protectedAssertionList && protectedAssertionList.length > 0) {
            setProtectedAssertionListEl(<Grid item xs={12} md={6}>
                <Typography sx={{ mt: 4, mb: 2 }} variant="h6" component="div">
                    Protected Assertions to be embedded
                </Typography>
                <AssertionView assertionList={protectedAssertionList} />
            </Grid>);
        }
    }, [protectedAssertionList]);

    return (
        <Stack
            direction="column"
            spacing={4}
            sx={{
                height: '70vh',
                overflowX: 'auto',
                overflowY: 'auto'
            }}
        >
            <Typography sx={{ mt: 4, mb: 2 }} variant="h5" component="div">
                Recap
            </Typography>

            <TextField
                label="Save digital asset as"
                value={outputAssetName}
                onChange={(e) => setOutputAssetName(e.target.value)}
                size="small"
            />

            {assertionListEl}

            {protectedAssertionListEl}
        </Stack>
    )
}

export default Recap