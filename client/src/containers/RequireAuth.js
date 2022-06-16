import React from "react";
import { useAuth } from "../helpers/useAuth";
import { Navigate } from "react-router-dom";

export const RequireAuth = ({ children }) => {
    const auth = useAuth();

    function isAuthorized() {

        if (!auth) {
            return false;
        }

        if (!auth.user) {
            return false;
        }

        if (!auth.user.roles) {
            return false;
        }

        return auth.user.roles.includes("PRODUCER");
    }

    return isAuthorized() ? (
        children
    ) : (
        <Navigate to="/" />
    );
};