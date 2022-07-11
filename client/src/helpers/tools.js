export const typeToAssertionDict = {
    'Filter': 'mpms.prov.filtered',
    'Crop': 'mpms.prov.cropped',
    'Resize': 'mpms.prov.resized',
}

export function getRandomInt(max) {
    return Math.floor(Math.random() * Math.floor(max));
}

export function parseJwt(token) {
    var base64Url = token.split('.')[1];
    var base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    var jsonPayload = decodeURIComponent(atob(base64).split('').map(function (c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));

    return JSON.parse(jsonPayload);
};

export function getHistoryList() {

    const nodeItems = document.getElementsByClassName("history-item");

    var historyList = [];

    for (var i = 0; i < nodeItems.length; i++) {
        const innerNode = nodeItems[i].getElementsByTagName("span");
        var text = innerNode[0].textContent;
        text = text.replace(/[\n\r\s]/g, '');

        if (text === "Load") {
            continue;
        }

        historyList.push(text);
    }

    return historyList;
}

export function getActionAssertions(editorHistoryList) {

    var tempDict = {}

    for (var element in editorHistoryList) {
        var parts = editorHistoryList[element].split("(")

        if (parts.length === 1) {
            tempDict[parts[0]] = ""
            continue;
        }

        if (!(parts[0] in tempDict)) {
            tempDict[parts[0]] = parts[1].replace(")", "")
        } else {
            tempDict[parts[0]] = tempDict[parts[0]] + "," + parts[1].replace(")", "")
        }
    }

    var assertionList = [];

    Object.keys(tempDict).forEach((key) => {

        var template = {
            "date": String((new Date()).toString()),
            "action": typeToAssertionDict[key],
            "metadata": tempDict[key],
            "softwareAgent": "Mipams for Producers v1.0"
        }

        assertionList.push(template);
    });

    return assertionList;
}

export function getMetadataList(data) {

    var ignoredAttributes = ["ExifMetadata", "DataDump", "ThumbnailImage", "ExifToolVersion", "SourceFile", "FileName", "Directory", "FileSize", "FileModifyDate", "FileAccessDate", "FileInodeChangeDate", "FilePermissions", "FileType"]

    var tempDict = (Array.isArray(data)) ? data[0] : JSON.parse(data);

    var resultList = [];

    var i = 1;

    Object.keys(tempDict).forEach((key) => {

        if (ignoredAttributes.includes(key)) {
            return;
        }

        if (!tempDict[key] || tempDict[key] === 'n/a') {
            return
        }

        var template = {
            "id": i,
            "key": key,
            "val": tempDict[key],
        }

        resultList.push(template);
        i++;
    });

    return resultList;
}

export function getMetadataAssertions(metadataList) {
    var exifContent = {};

    metadataList.forEach(entry => (exifContent[entry.key] = entry.val));

    return [{ "exifMetadata": JSON.stringify(exifContent) }];
}

export function getAssertionDescription(assertion) {

    var result = assertion.replace("mpms.prov.", '');

    const firstLetter = result.slice(0, 1);

    return firstLetter.toUpperCase() + result.substring(1);
}

export function getExifMetadataFromJSON(exifMetadata) {

    var exifObject = JSON.parse(exifMetadata);

    var result = "";

    Object.keys(exifObject).forEach((key) => {
        result = result + "[" + key + "]: " + exifObject[key]

        if (result.length === 0) {
            return;
        }

        result = result + ", ";
    });

    return result;
}

export function getManifestIdFromUri(uri) {
    return uri.replace('self#jumbf=mipams/', '');
}

export function displayManifestId(manifestId) {

    var res = String(manifestId);

    res = res.replace("urn:uuid:", '');

    return "urn:uuid:" + res.toUpperCase();
}

export function isAssertionLabel(assertion) {
    return (typeof assertion === 'string' && (assertion.startsWith("mpms.") || assertion.startsWith("stds.")));
}