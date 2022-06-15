import React from "react";

import { useAuth } from "../helpers/useAuth";

export const PageNotFound = () => {

    const auth = useAuth();

    console.log(auth);

    return (
        <h>404 Page not found</h>
    );
}

export default PageNotFound;