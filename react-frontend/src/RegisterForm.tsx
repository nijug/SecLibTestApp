import React, { useState, FC } from 'react';
import { TextField, Button, Container, Typography } from '@material-ui/core';
import './RegisterForm.css';

export interface RegisterFormProps {
    onRegister: (username: string, password: string) => Promise<void>;
}

const RegisterForm: FC<RegisterFormProps> = ({ onRegister }) => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    const register = async () => {
        try {
            await onRegister(username, password);
        } catch (error) {
            console.error(error);
            alert('Failed to register. Please try again.');
        }
    };

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
