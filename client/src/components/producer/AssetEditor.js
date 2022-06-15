import React from 'react';
import './tui-image-editor.css'
import ImageEditor from '@toast-ui/react-image-editor'

const AssetEditor = (props) => {
    const { editorRef } = props;

    return (
        <ImageEditor
            ref={editorRef}
            includeUI={{
                menu: ['filter', 'crop', 'resize'],
                initMenu: 'filter',
                uiSize: {
                    width: '100%',
                    height: '70vh'
                },
                menuBarPosition: 'bottom'
            }}
            cssMaxHeight={500}
            cssMaxWidth={700}
            selectionStyle={{
                cornerSize: 20,
                rotatingPointOffset: 70
            }}
            usageStatistics={true}
        />
    )
}

export default AssetEditor