import React from 'react'
import { List, ListItem, ListItemText, Stack, Tooltip, Typography } from '@mui/material';

import FilterAltIcon from '@mui/icons-material/FilterAlt';
import CropIcon from '@mui/icons-material/Crop';
import AspectRatioIcon from '@mui/icons-material/AspectRatio';
import ColorLensIcon from '@mui/icons-material/ColorLens';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import DescriptionOutlinedIcon from '@mui/icons-material/DescriptionOutlined';
import TimelineOutlinedIcon from '@mui/icons-material/TimelineOutlined';

import { getAssertionDescription, getExifMetadataFromJSON, getRandomInt, getManifestIdFromUri } from '../helpers/tools';


const AssertionView = (props) => {

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

function getAssertionListItem(assertion) {

    if (Boolean(assertion.action)) {
        return getActionListItem(assertion);
    } else if (Boolean(assertion.exifMetadata)) {
        return getExifMetadataListItem(assertion);
    } else if (Boolean(assertion.algorithm)) {
        return <Typography> The checksum of the digital asset is copmuted using {assertion.algorithm} </Typography>
    } else if (Boolean(assertion.relationship) && assertion.relationship === "parentOf") {
        return getParentRelationshipListItem(assertion);
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
        <Typography> Exif Metadata </Typography>
        <Tooltip title={getExifMetadataFromJSON(assertion.exifMetadata)}>
            <InfoOutlinedIcon />
        </Tooltip>
    </Stack>
}

function getParentRelationshipListItem(assertion) {
    return <Stack direction="row" spacing={1}>
        <TimelineOutlinedIcon />
        <Typography> Parent Manifest is: {getManifestIdFromUri(assertion.manifestReference.uri)} </Typography>
    </Stack>
}