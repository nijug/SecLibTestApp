import React, { useState, FC } from 'react';
import { TextField, Button, Container, Typography } from '@material-ui/core';
import './RegisterForm.css';

export interface RegisterFormProps {
    onRegister: (username: string, password: string) => Promise<{ qrCode: string; totpSecret: string; }>;
}

const RegisterForm: FC<RegisterFormProps> = ({ onRegister }) => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [qrCode, setQrCode] = useState('');
    const [totpSecret, setTotpSecret] = useState('');
    const [isRegistered, setIsRegistered] = useState(false);

    const register = async () => {
        try {
            const response = await onRegister(username, password);
            const qrCodeUrl = `data:image/png;base64,${response.qrCode}`;
            setQrCode(qrCodeUrl);
            setTotpSecret(response.totpSecret);
            setIsRegistered(true);
        } catch (error) {
            console.error('Registration failed:', error);
            alert('Registration failed. Please try again.');
        }
    };


    // Conditional rendering based on registration status
    if (isRegistered) {
        return (
            <Container className="registerFormContainer" maxWidth="xs">
                <Typography variant="h5" align="center">Registration Successful</Typography>
                <img src={qrCode} alt="QR Code" style={{ display: 'block', marginLeft: 'auto', marginRight: 'auto' }} />
                <Typography variant="body1" align="center">TOTP Secret: {totpSecret}</Typography>
                <Button fullWidth variant="contained" color="primary" onClick={() => window.location.href = '/login'}>Back to Login page</Button>
            </Container>
        );
    }

    return (
        <Container className="registerFormContainer" maxWidth="xs">
            <Typography variant="h5" align="center">User Registration</Typography>
            <TextField fullWidth margin="normal" value={username} onChange={e => setUsername(e.target.value)} label="Username" />
            <TextField fullWidth margin="normal" value={password} onChange={e => setPassword(e.target.value)} label="Password" type="password" />
            <Button fullWidth variant="contained" color="secondary" onClick={register}>Register</Button>
        </Container>
    );
};

export default RegisterForm;
