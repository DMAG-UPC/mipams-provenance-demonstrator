import React from "react";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import Login from "./components/Login";
import { RequireAuth } from "./containers/RequireAuth";
import PageNotFound from "./components/PageNotFound";
import { AuthProvider } from "./helpers/useAuth";
import Producer from "./containers/Producer";
import Consumer from "./containers/Consumer";
import { createTheme, ThemeProvider, responsiveFontSizes } from '@mui/material/styles';

let theme = createTheme();
theme = responsiveFontSizes(theme);

function App() {
  return (
    <ThemeProvider theme={theme}>
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route
              path="/producer"
              element={
                <RequireAuth>
                  <Producer />
                </RequireAuth>
              }
            ></Route>
            <Route path="/" element={<Consumer />} />
            <Route path="/*" element={<PageNotFound />} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;