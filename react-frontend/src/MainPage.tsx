import React, { useEffect, useState } from 'react';
import axiosInstance from './axiosInstance';
import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, TextField } from '@material-ui/core';
import { useNavigate } from 'react-router-dom';
import './MainPage.css';
import ReactMarkdown from 'react-markdown';


interface MainPageProps {
    username: string | null;
    onLogout: () => void;
    isAuthenticated: boolean;
    onDebugRegisterLoginAdmin?: () => void;
}

interface Post {
    id: string;
    title: string;
    content: string;
    author: string;
}
 /*todo: fix it that when being guest, you have buttons for post taht dont have author, but there shouldnt be post without author
 so fix validation maybe probably wtf */

const MainPage: React.FC<MainPageProps> = ({ username, isAuthenticated, onLogout, onDebugRegisterLoginAdmin }) => {
    const [posts, setPosts] = useState<Post[]>([]);
    const [open, setOpen] = useState(false);
    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [reloadPosts, setReloadPosts] = useState(false);
    const navigate = useNavigate();
    const [openPostId, setOpenPostId] = useState<string | null>(null);


    useEffect(() => {
        const fetchPosts = async () => {
            try {
                const response = await axiosInstance.get('/posts');
                if (Array.isArray(response.data)) {
                    setPosts(response.data);
                } else {
                    setPosts([]);
                }
            } catch (error) {
                console.error('Failed to fetch posts:', error);
                setPosts([]);
            }
        };

        fetchPosts();
    }, [reloadPosts]);

    const handleDeletePost = async (postId: string) => {
        try {
            await axiosInstance.delete(`/posts/${postId}`, { params: { author: username } });
            setReloadPosts(prev => !prev);
        } catch (error) {
            console.error('Failed to delete post:', error);
            alert('Failed to delete post. Please try again.');
        }
    };

    const handleClickOpen = () => {
        setTitle('');
        setContent('');
        setOpen(true);
    };

    const handleEditClick = (postId: string) => {
        const post = posts.find(post => post.id === postId);
        if (post) {
            setTitle(post.title);
            setContent(post.content);
        }
        setOpenPostId(postId);
        setOpen(true);
    };

    const handleSave = async () => {
        try {
            if (openPostId) {
                await axiosInstance.put(`/posts/${openPostId}`, { title, content, author: username });
            } else {
                await axiosInstance.post('/posts', { title, content, author: username });
            }
            setReloadPosts(prev => !prev);
            setOpen(false);
            setOpenPostId(null);
        } catch (error) {
            console.error('Failed to save post:', error);
            alert('Failed to save post. Please try again.');
        }
    };

    const handleClose = () => {
        setOpen(false);
    };


    return (
        <div className="mainPageWrapper">
            <nav className="navbar">
                <h1>Welcome, {username || 'Guest'}!</h1>
                <div className="navbar-buttons">
                    {isAuthenticated ? (
                        <>
                            <Button className="logoutButton" variant="contained" color="secondary" onClick={onLogout}>Logout</Button>
                            <Button className="createPostButton" variant="contained" color="primary" onClick={handleClickOpen}>Create Post</Button>
                        </>
                    ) : (
                        <>
                            <Button className="loginButton" variant="contained" color="primary" onClick={() => navigate('/login')}>Login</Button>
                            <Button className="registerButton" variant="contained" color="secondary" onClick={() => navigate('/register')}>Register</Button>
                            <Button className="debugLoginButton" variant="contained" color="default" onClick={onDebugRegisterLoginAdmin}>Debug</Button>
                        </>
                    )}
                </div>
            </nav>
            <div className="mainPageContainer">
                <div>
                   {posts.map(post => (
                       <div key={post.id} className="post">
                           <p className="postAuthor">{post.author}</p>
                           <h2 className="postTitle">{post.title}</h2>
                           <ReactMarkdown className="postContent">{post.content}</ReactMarkdown>
                           {post.author === username && (
                               <div className="postButtons">
                                   <Button className="editButton" variant="outlined" color="primary" onClick={() => handleEditClick(post.id)}>Edit</Button>
                                   <Button className="deleteButton" variant="outlined" color="secondary" onClick={() => handleDeletePost(post.id)}>Delete</Button>
                               </div>
                           )}
                       </div>
                   ))}
                </div>
            </div>
            <Dialog open={open} onClose={handleClose}>
                <DialogTitle>{openPostId ? 'Edit Post' : 'Create Post'}</DialogTitle>
                <DialogContent>
                    <DialogContentText>Enter the title and content of your post.</DialogContentText>
                    <TextField
                        autoFocus
                        margin="dense"
                        id="title"
                        label="Title"
                        fullWidth
                        value={title}
                        onChange={e => setTitle(e.target.value)}
                    />
                    <TextField
                        margin="dense"
                        id="content"
                        label="Content"
                        fullWidth
                        multiline
                        rows={4}
                        value={content}
                        onChange={e => setContent(e.target.value)}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose} color="primary">Cancel</Button>
                    <Button onClick={handleSave} color="primary">Save</Button>
                </DialogActions>
            </Dialog>
        </div>
    );
};

export default MainPage;
