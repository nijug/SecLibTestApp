import React, { useState } from 'react';
import axiosInstance from './axiosInstance';  // Import your axiosInstance

function ForgotPassword() {
    const [username, setUsername] = useState('');
    const [message, setMessage] = useState('');

    const forgotPassword = async () => {
        try {
            await axiosInstance.post('/users/forgot-password', { username });
            setMessage('Password reset email has been sent');
        } catch (error) {
            console.error(error);
            setMessage('Failed to send password reset email');
        }
    };

    return (
        <div>
            <input type="text" value={username} onChange={e => setUsername(e.target.value)} placeholder="Username" />
            <button onClick={forgotPassword}>Send Password Reset Email</button>
            {message && <p>{message}</p>}
        </div>
    );
}

export default ForgotPassword;
