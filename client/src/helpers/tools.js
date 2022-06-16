export const typeToAssertionDict = {
    'Filter': 'mpms.prov.filtered',
    'Crop': 'mpms.prov.cropped',
    'Resize': 'mpms.prov.resized',
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

    console.log(editorHistoryList);

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
    console.log(tempDict);

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

    var tempDict = data[0];

    var resultList = [];

    var i = 1;

    Object.keys(tempDict).forEach((key) => {

        if (key === "SourceFile" || key === "ExifToolVersion") {
            return;
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