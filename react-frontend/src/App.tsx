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
    const [totpSecret, setTotpSecret] = useState('');
    const [qrCode, setQrCode] = useState('');
    const [showQrCodePopup, setShowQrCodePopup] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async (username: string, password: string, totp: string) => {
        try {
            const response = await axiosInstance.post('/users/login', { username, password, totp });
            if (response.status === 200) {
                setLoggedInUser(username);
                localStorage.setItem('username', username);
                localStorage.setItem('session', response.data.session);
                axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${response.data.session}`;
                navigate('/main');
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
            const qrCodeBlob = new Blob([new Uint8Array(atob(response.data.qrCode).split('').map(char => char.charCodeAt(0)))], { type: 'image/png' });
            const qrCodeUrl = URL.createObjectURL(qrCodeBlob);
            setTotpSecret(response.data.totpSecret);
            setQrCode(qrCodeUrl);
            setShowQrCodePopup(true);
        } catch (error) {
            console.error('Registration failed:', error);
            alert('Registration failed. Please try again.');
        }
    };

    const handleForgotPassword = () => {
        navigate('/forgot-password');
    };


    const handleDebugRegisterLoginAdmin = async () => {
            try {
                const response = await axiosInstance.post('/users/debug/register-login-admin');
                console.log(response.data.message);
                localStorage.setItem('session', response.data.session);
                axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${response.data.session}`;
                setLoggedInUser('admin-debug');
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
                    } else {
                        throw new Error('Not authenticated');
                    }
                } catch (error) {
                    console.error('Failed to check authentication:', error);
                    setLoggedInUser(null);
                    localStorage.removeItem('username');
                    localStorage.removeItem('session');
                    delete axiosInstance.defaults.headers.common['Authorization'];
                }
            }
        };

        checkAuthentication();
    }, []);


    return (
        <div className="App">
            <Routes>
                <Route path="/login" element={<LoginForm onLogin={handleLogin} onForgotPassword={handleForgotPassword} />} />
                <Route path="/register" element={<RegisterForm onRegister={handleRegister} />} />
                <Route path="/forgot-password" element={<ForgotPassword />} />
                <Route path="/reset-password" element={<ResetPassword />} />
                <Route path="/main" element={<MainPage username={loggedInUser} onLogout={handleLogout} onDebugRegisterLoginAdmin={handleDebugRegisterLoginAdmin} />} />
                <Route path="/" element={<MainPage username={loggedInUser} onLogout={handleLogout} onDebugRegisterLoginAdmin={handleDebugRegisterLoginAdmin} />} />
            </Routes>
            {showQrCodePopup && <QrCodePopup qrCode={qrCode} totpSecret={totpSecret} onClose={() => setShowQrCodePopup(false)} />}
        </div>
    );
};

export default App;
