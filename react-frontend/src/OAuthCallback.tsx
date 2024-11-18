import React, { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import axiosInstance from './axiosInstance';

interface OAuthCallbackProps {
    setLoggedInUser: (username: string | null) => void;
}

const OAuthCallback: React.FC<OAuthCallbackProps> = ({ setLoggedInUser }) => {
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        const handleOAuthCallback = async () => {
            const params = new URLSearchParams(location.search);
            const username = params.get('username');

            if (username) {
                try {
                    const response = await axiosInstance.get(`oauth/oauth-callback?username=${username}`);
                    if (response.status === 200) {
                        const { username, sessionId, csrfToken } = response.data;
                        localStorage.setItem('username', username);
                        localStorage.setItem('session', sessionId);
                        localStorage.setItem('csrfToken', csrfToken);
                        axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${sessionId}`;
                        setLoggedInUser(username);
                        navigate('/main');
                        window.location.reload();
                    } else {
                        throw new Error('OAuth login failed');
                    }
                } catch (error) {
                    console.error('OAuth login failed:', error);
                    alert('OAuth login failed. Please try again.');
                }
            }
        };

        handleOAuthCallback();
    }, [navigate, setLoggedInUser, location]);

    return <div>Loading...</div>;
};

export default OAuthCallback;