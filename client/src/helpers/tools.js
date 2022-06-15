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

    let historyList = new Array();

    for (let i = 0; i < nodeItems.length; i++) {
        const innerNode = nodeItems[i].getElementsByTagName("span");
        let text = innerNode[0].textContent;
        text = text.replace(/[\n\r\s]/g, '');
        console.log(text);

        historyList.push(text);
    }

    return historyList;
}

export function getActionAssertions(editorHistoryList) {

    return editorHistoryList;
}