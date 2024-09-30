// ResetPassword.tsx
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useLocation } from 'react-router-dom';

function ResetPassword() {
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [token, setToken] = useState('');
    const location = useLocation();

    useEffect(() => {
        const query = new URLSearchParams(location.search);
        setToken(query.get('token') || '');
    }, [location]);

    const resetPassword = async () => {
        if (newPassword !== confirmPassword) {
            alert('Passwords do not match');
            return;
        }

        try {
            await axios.post('https://localhost/api/users/reset-password', { token, newPassword, confirmPassword });
            alert('Password has been reset');
        } catch (error) {
            console.error(error);
            alert('Failed to reset password');
        }
    };

    return (
        <div>
            <input type="password" value={newPassword} onChange={e => setNewPassword(e.target.value)} placeholder="New Password" />
            <input type="password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} placeholder="Confirm Password" />
            <button onClick={resetPassword}>Reset Password</button>
        </div>
    );
}

export default ResetPassword;