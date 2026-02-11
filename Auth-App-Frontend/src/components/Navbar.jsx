import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import ThemeToggle from './ThemeToggle';
import { LogOut, Shield, User, LogIn, UserPlus } from 'lucide-react';
import './Navbar.css';

export default function Navbar() {
    const { isAuthenticated, user, logout } = useAuth();
    const location = useLocation();
    const navigate = useNavigate();

    const handleLogout = async () => {
        await logout();
        navigate('/');
    };

    // Don't show navbar on auth pages or OAuth callback
    const hiddenPages = ['/login', '/register', '/oauth/callback'];
    if (hiddenPages.includes(location.pathname)) return null;

    return (
        <nav className="navbar">
            <div className="navbar__inner">
                {/* Logo */}
                <Link to="/" className="navbar__logo">
                    <div className="navbar__logo-icon">
                        <Shield size={22} />
                    </div>
                    <span className="navbar__logo-text">
                        Auth<span className="gradient-text">App</span>
                    </span>
                </Link>

                {/* Right section */}
                <div className="navbar__actions">
                    <ThemeToggle />

                    {isAuthenticated ? (
                        <>
                            <Link to="/dashboard" className="navbar__user-chip">
                                <div className="navbar__avatar">
                                    {user?.image ? (
                                        <img src={user.image} alt={user.username} />
                                    ) : (
                                        <User size={16} />
                                    )}
                                </div>
                                <span className="navbar__username">{user?.username}</span>
                            </Link>

                            <button className="navbar__logout" onClick={handleLogout} title="Logout">
                                <LogOut size={18} />
                            </button>
                        </>
                    ) : (
                        <>
                            <Link to="/login" className="navbar__auth-link">
                                <LogIn size={16} />
                                <span>Sign In</span>
                            </Link>
                            <Link to="/register" className="navbar__auth-link navbar__auth-link--primary">
                                <UserPlus size={16} />
                                <span>Sign Up</span>
                            </Link>
                        </>
                    )}
                </div>
            </div>
        </nav>
    );
}
