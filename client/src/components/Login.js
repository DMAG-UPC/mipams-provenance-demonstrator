import React, { useState, useEffect } from "react";
import { useAuth } from "../helpers/useAuth";
import { Button, Container, Typography, TextField, CssBaseline } from '@mui/material';
import Alert from '@mui/material/Alert';
import { styled } from '@mui/material/styles';

const LoginWrapper = styled(Container)(({ theme }) => ({
    marginTop: theme.spacing(8),
}));

const StyledButton = styled(Button)(({ theme }) => ({
    margin: theme.spacing(3, 0, 2),
}));

function Login() {
    const auth = useAuth();
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [errorMessage, setErrorMessage] = useState("");

    const handleLogin = () => {
        auth.login({ username: username, password: password });
    };

    useEffect(() => {
        if (auth.user && auth.user.errorMessage) {
            setErrorMessage("Wrong username/password.");
        }
    }, [auth]);

    let loginError = null;
    if (errorMessage) loginError = <Alert severity="error">{errorMessage}</Alert>

    return (
        <LoginWrapper component="main" maxWidth="xs">
            <CssBaseline />
            <Typography component="h1" variant="h5">
                MIPAMS Provenance app - Sign in
            </Typography>
            {loginError}
            <TextField
                variant="outlined"
                margin="normal"
                required
                fullWidth
                id="username"
                label="Username"
                name="username"
                autoFocus
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoComplete="off"
            />
            <TextField
                variant="outlined"
                margin="normal"
                required
                fullWidth
                name="password"
                label="Password"
                type="password"
                id="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="off"
            />

            <StyledButton
                fullWidth
                variant="contained"
                color="primary"
                onClick={handleLogin}
            >
                Sign In
            </StyledButton>
        </LoginWrapper>
    );
}

export default Login;