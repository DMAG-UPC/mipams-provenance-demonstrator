import React, { createContext, useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import { parseJwt } from '../helpers/tools';

import api from './api';

const authContext = createContext();

export function AuthProvider({ children }) {

    const loginAction = async (username, password) => {
        const param = { username: username, password: password };

        const res = await api.post('/login', param);

        if (res.data.errorMessage) {
            setUser({ errorMessage: res.data.errorMessage });
            throw new Error(res.data.errorMessage);
        }

        const expirationDate = new Date(
            new Date().getTime() + res.data.expirationTime * 1000
        );

        const jsonClaims = parseJwt(res.data.token);
        const roleArray = jsonClaims.roles.split(" ")

        const userObject = { username: jsonClaims.sub, roles: roleArray, tokenId: res.data.token, expirationDate: expirationDate };
        setUser(userObject);
    }

    const [user, setUser] = useState({});
    const navigate = useNavigate();

    const login = async (userDetails) => {
        try {
            await loginAction(userDetails.username, userDetails.password);
            navigate("/");
        } catch (error) {
            navigate("/login");
            console.log(error)
        }
    };

    const logout = () => {
        setUser();
        navigate("/");
    };

    return (
        <authContext.Provider value={{ user, login, logout }}>
            {children}
        </authContext.Provider>
    );
}

export const useAuth = () => {
    return useContext(authContext);
};