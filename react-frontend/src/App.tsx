import React, { useState, useEffect } from 'react';
import './App.css';
import LoginForm from "./LoginForm";
import RegisterForm from "./RegisterForm";
import QrCodePopup from "./QrCodePopup";
import ForgotPassword from './ForgotPassword';
import ResetPassword from './ResetPassword';
import MainPage from './MainPage';
import { Routes, Route, useNavigate } from 'react-router-dom';
import axiosInstance from './axiosInstance';

const App: React.FC = () => {
    const [loggedInUser, setLoggedInUser] = useState<string | null>(localStorage.getItem('username'));
    const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
    const navigate = useNavigate();

    const handleLogin = async (username: string, password: string, totpSecret: string) => {
        try {
            const response = await axiosInstance.post('/users/login', { username, password, totpSecret });
            if (response.status === 200) {
                setLoggedInUser(username);
                localStorage.setItem('username', username);
                localStorage.setItem('session', response.data.session);
                localStorage.setItem('csrfToken', response.data.csrfToken);

                axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${response.data.session}`;
                navigate('/main');
                window.location.reload();
            } else {
                throw new Error('Login failed');
            }
        } catch (error) {
            console.error('Login failed:', error);
            alert('Login failed. Please try again.');
        }
    };

    const handleLogout = async () => {
        try {
            await axiosInstance.post('/users/logout');
        } catch (error) {
            console.error('Logout failed:', error);
        } finally {
            setLoggedInUser(null);
            localStorage.removeItem('username');
            localStorage.removeItem('session');
            delete axiosInstance.defaults.headers.common['Authorization'];
            window.location.reload();
        }
    };

const handleRegister = async (username: string, password: string) => {
    try {
        const response = await axiosInstance.post('/users/register', { username, password });
        return {
            qrCode: response.data.qrCode,
            totpSecret: response.data.totpSecret,
        };
    } catch (error) {
        console.error('Registration failed:', error);
        alert('Registration failed. Please try again.');
        throw error;
    }
};

    const handleForgotPassword = () => {
        navigate('/forgot-password');
    };


    const handleDebugRegisterLoginAdmin = async () => {
            try {
                const response = await axiosInstance.post('/users/debug/register-login-admin');
                console.log(response.data.message);
                setLoggedInUser('admin-debug');
                localStorage.setItem('username', 'admin-debug');
                localStorage.setItem('session', response.data.session);
                localStorage.setItem('csrfToken', response.data.csrfToken);
                axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${response.data.session}`;
                navigate('/main');
                window.location.reload();
            } catch (error) {
                console.error('Failed to register-login admin:', error);
                alert('Failed to register-login admin. Please try again.');
            }
        };

    useEffect(() => {
        const checkAuthentication = async () => {
            const session = localStorage.getItem('session');
            if (session) {
                try {
                    axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${session}`;
                    const response = await axiosInstance.get('/users/check-authentication');
                    if (response.status === 200) {
                        setLoggedInUser(localStorage.getItem('username'));
                        setIsAuthenticated(true);
                    } else {
                        throw new Error('Not authenticated');
                    }
                } catch (error) {
                    console.error('Failed to check authentication:', error);
                    setLoggedInUser(null);
                    setIsAuthenticated(false);
                    localStorage.removeItem('username');
                    localStorage.removeItem('session');
                    delete axiosInstance.defaults.headers.common['Authorization'];
                }
            }
        };

        checkAuthentication();
    }, []);

    axiosInstance.interceptors.request.use((config) => {
        const csrfToken = localStorage.getItem('csrfToken');
        if (csrfToken && config.method && ['post', 'put', 'delete', 'patch'].includes(config.method.toLowerCase())) {
        config.headers['X-CSRF-TOKEN'] = csrfToken;
    }
        return config;
    }, (error) => {
        return Promise.reject(error);
    });

 return (
        <div className="App">
            <Routes>
                <Route path="/login" element={<LoginForm onLogin={handleLogin} onForgotPassword={handleForgotPassword} />} />
                <Route path="/register" element={<RegisterForm onRegister={handleRegister} />} />
                <Route path="/forgot-password" element={<ForgotPassword />} />
                <Route path="/reset-password" element={<ResetPassword />} />
                <Route path="/main" element={<MainPage isAuthenticated={isAuthenticated} username={loggedInUser} onLogout={handleLogout} onDebugRegisterLoginAdmin={handleDebugRegisterLoginAdmin} />} />
                <Route path="/" element={<MainPage isAuthenticated={isAuthenticated} username={loggedInUser} onLogout={handleLogout} onDebugRegisterLoginAdmin={handleDebugRegisterLoginAdmin} />} />
            </Routes>
        </div>
    );
};

export default App;