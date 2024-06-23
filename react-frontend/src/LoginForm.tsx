import React, { useState } from 'react';
import { TextField, Button, Container, Typography } from '@material-ui/core';
import { useNavigate } from 'react-router-dom';
import './LoginForm.css';

interface LoginFormProps {
    onLogin: (username: string, password: string, totp: string) => void;
    onForgotPassword: () => void;
}

const LoginForm: React.FC<LoginFormProps> = ({ onLogin, onForgotPassword }) => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [totp, setTotp] = useState('');

    const navigate = useNavigate();

    const login = () => {
        onLogin(username, password, totp);
    };

    return (
        <Container className="loginFormContainer" maxWidth="xs">
            <Typography variant="h5" align="center">User Login</Typography>
            <TextField fullWidth margin="normal" value={username} onChange={e => setUsername(e.target.value)} label="Username" />
            <TextField fullWidth margin="normal" value={password} onChange={e => setPassword(e.target.value)} label="Password" type="password" />
            <TextField fullWidth margin="normal" value={totp} onChange={e => setTotp(e.target.value)} label="TOTP Token" />
            <Button fullWidth variant="contained" color="primary" onClick={login}>Login</Button>
            <Button fullWidth variant="contained" color="secondary" onClick={onForgotPassword}>Forgot Password?</Button>
        </Container>
    );
};

export default LoginForm;
