import React from 'react'
import { Button, List, ListItem, ListItemText, Modal, Stack, Tooltip, Typography, Box } from '@mui/material';

import FilterAltIcon from '@mui/icons-material/FilterAlt';
import CropIcon from '@mui/icons-material/Crop';
import AspectRatioIcon from '@mui/icons-material/AspectRatio';
import ColorLensIcon from '@mui/icons-material/ColorLens';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import DescriptionOutlinedIcon from '@mui/icons-material/DescriptionOutlined';
import TimelineOutlinedIcon from '@mui/icons-material/TimelineOutlined';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';

import { getAssertionDescription, getMetadataList, getRandomInt, getManifestIdFromUri, isAssertionLabel } from '../helpers/tools';
import ExifMetadataTable from './ExifMetadataTable';


const AssertionView = (props) => {

    const [openExifModal, setOpenExifModal] = React.useState(false);
    const handleExifModalOpen = () => { setOpenExifModal(true) };
    const handleExifModalClose = () => setOpenExifModal(false);

    function getAssertionListItem(assertion) {

        if (Boolean(assertion.action)) {
            return getActionListItem(assertion);
        } else if (Boolean(assertion.exifMetadata)) {
            return getExifMetadataListItem(assertion);
        } else if (Boolean(assertion.algorithm)) {
            return <Typography> The checksum of the digital asset is computed using {assertion.algorithm} </Typography>
        } else if (Boolean(assertion.relationship) && assertion.relationship === "parentOf") {
            return getParentRelationshipListItem(assertion);
        } else if (isAssertionLabel(assertion)) {
            return getProtectedListItem(assertion);
        } else {
            console.log(assertion);
            return <ListItemText primary={assertion} />;
        }
    }

    function getActionListItem(assertion) {
        return <Stack direction="row" spacing={1}>
            {getIconBasedOnAction(assertion.action)}
            <Typography> {getAssertionDescription(assertion.action)} </Typography>
            {assertion.metadata && <Tooltip title={assertion.metadata}>
                <InfoOutlinedIcon />
            </Tooltip>}
        </Stack>
    }

    function getIconBasedOnAction(action) {
        if (action === "mpms.prov.filtered") {
            return <FilterAltIcon />
        } else if (action === "mpms.prov.cropped") {
            return <CropIcon />
        } else if (action === "mpms.prov.resized") {
            return <AspectRatioIcon />
        } else {
            return <ColorLensIcon />
        }
    }

    function getExifMetadataListItem(assertion) {

        return <Stack direction="row" spacing={1}>
            <DescriptionOutlinedIcon />
            <Box sx={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
            }}>
                <Typography> Exif Metadata </Typography>
            </Box>
            <Tooltip title={"Inspect exif metadata"}>
                <Button onClick={handleExifModalOpen}>
                    Learn more
                </Button>
            </Tooltip>

            <Modal open={openExifModal}
                onClose={handleExifModalClose}
                aria-labelledby="modal-modal-title"
                aria-describedby="modal-modal-description"
                sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center'
                }}>
                <Box sx={{ width: '60%', height: '70vh', overflowY: 'auto', overflowX: 'auto' }}>
                    <ExifMetadataTable metadata={getMetadataList(assertion.exifMetadata)} />
                </Box>
            </Modal>


        </Stack>
    }

    function getParentRelationshipListItem(assertion) {
        return <Stack direction="row" spacing={1}>
            <TimelineOutlinedIcon />
            <Typography> Parent Manifest is: {getManifestIdFromUri(assertion.manifestReference.uri)} </Typography>
        </Stack>
    }

    function getProtectedListItem(assertion) {
        return <Stack direction="row" spacing={1}>
            <DescriptionOutlinedIcon />
            <Typography> Exif Metadata </Typography>
            <Tooltip title="You don't have access to view this piece of metadata">
                <LockOutlinedIcon sx={{ color: 'blue' }} />
            </Tooltip>
        </Stack>
    }

    const { assertionList } = props;

    const assertionListElement = assertionList && assertionList.map(assertion => (
        <ListItem key={getRandomInt(1000)}>
            {getAssertionListItem(assertion)}
        </ListItem>
    ))

    return (
        <List dense={true}>
            {assertionListElement}
        </List>
    )
}

export default AssertionView;