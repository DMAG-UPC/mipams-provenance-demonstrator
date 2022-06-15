import * as React from 'react';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Toolbar from '@mui/material/Toolbar';
import IconButton from '@mui/material/IconButton';
import MenuIcon from '@mui/icons-material/Menu';
import AccountCircle from '@mui/icons-material/AccountCircle';
import MenuItem from '@mui/material/MenuItem';
import Menu from '@mui/material/Menu';
import { useAuth } from '../helpers/useAuth';
import { useNavigate, useLocation } from "react-router-dom";
import { Button } from '@mui/material';

export default function NavBar(props) {
    const auth = useAuth();
    const location = useLocation();
    const navigate = useNavigate();
    const [anchorEl, setAnchorEl] = React.useState(null);

    const handleMenu = (event) => {
        setAnchorEl(event.currentTarget);
    };

    const handleLogin = () => {
        handleClose();
        navigate('/login');
    };

    const handleProducer = () => {
        navigate('/producer');
    };

    const handleConsumer = () => {
        navigate('/');
    };

    const handleLogout = () => {
        handleClose();
        auth.logout();
    };

    const handleClose = () => {
        setAnchorEl(null);
    }

    const menuElement = auth.user && auth.user.username ?
        (<MenuItem onClick={handleLogout}>Logout</MenuItem>) :
        (<MenuItem onClick={handleLogin}>Login</MenuItem>);


    let buttonElement = null;
    if (location.pathname === "/producer") {
        buttonElement =
            <Button
                onClick={handleConsumer}
                sx={{ my: 2, color: 'white', display: 'block' }}
            >
                Mipams For Consumers
            </Button>
    } else {
        buttonElement = (auth.user && auth.user.roles && auth.user.roles.includes("PRODUCER") && location.pathname === "/") &&
            <Button
                onClick={handleProducer}
                sx={{ my: 2, color: 'white', display: 'block' }}
            >
                Mipams For Producers
            </Button>
    }

    const menuPosition = (buttonElement) ? 'space-between' : 'flex-end';


    return (
        <Box sx={{ flexGrow: 1 }}>
            <AppBar position="static">
                <Toolbar sx={{ justifyContent: menuPosition }}>

                    {buttonElement}

                    <div>
                        <IconButton
                            sx={{ flexGrow: 1 }}
                            size="large"
                            aria-label="account of current user"
                            aria-controls="menu-appbar"
                            aria-haspopup="true"
                            onClick={handleMenu}
                            color="inherit"
                        >
                            {(auth.user && auth.user.username) ? <AccountCircle /> : <MenuIcon />}
                        </IconButton>
                        <Menu
                            id="menu-appbar"
                            anchorEl={anchorEl}
                            anchorOrigin={{
                                vertical: 'top',
                                horizontal: 'right',
                            }}
                            keepMounted
                            transformOrigin={{
                                vertical: 'top',
                                horizontal: 'right',
                            }}
                            open={Boolean(anchorEl)}
                            onClose={handleClose}
                        >
                            {menuElement}
                        </Menu>
                    </div>
                </Toolbar>
            </AppBar>
        </Box>
    );
}