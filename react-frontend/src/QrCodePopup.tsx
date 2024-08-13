// QrCodePopup.tsx
import React, { FC } from 'react';
import { Button, Container, Typography } from '@material-ui/core';
import './QrCodePopup.css';
import { useNavigate } from 'react-router-dom';


interface QrCodePopupProps {
    qrCode: string;
    totpSecret: string;
    onClose: () => void;
}


const QrCodePopup: FC<QrCodePopupProps> = ({ qrCode, totpSecret, onClose }) => {
    const navigate = useNavigate();

    const handleBackToLoginPage = () => {
        onClose();
        navigate('/login');
    };

    return (
        <Container className="qrCodePopupContainer whiteBackground" maxWidth="xs">
            <Typography variant="h5" align="center">Registration Successful</Typography>
            <img src={qrCode} alt="QR Code" style={{ display: 'block', marginLeft: 'auto', marginRight: 'auto' }} />
            <Typography variant="body1" align="center">TOTP Secret: {totpSecret}</Typography>
            <Button id="backToLoginPageButton" fullWidth variant="contained" color="primary" onClick={handleBackToLoginPage}>Back to Login page</Button>
        </Container>
    );
};

export default QrCodePopup;