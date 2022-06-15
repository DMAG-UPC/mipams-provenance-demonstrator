import { CardMedia } from '@mui/material'
import React from 'react'

const ImageHolder = (props) => {

    const { imageUrl } = props;

    return (
        <CardMedia
            sx={{
                height: '70vh',
                wifth: '100%',
                margin: 'auto'
            }}
            image={imageUrl}
            component="img"
        />
    )
}

export default ImageHolder