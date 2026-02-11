import { useState, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import FloatingShapes from '../components/FloatingShapes';
import GlassCard from '../components/GlassCard';
import Input from '../components/Input';
import Button from '../components/Button';
import ThemeToggle from '../components/ThemeToggle';
import { Mail, Lock, Shield, AlertCircle, ArrowLeft } from 'lucide-react';
import './AuthPages.css';

const BACKEND_URL = 'http://localhost:8083';

export default function LoginPage() {
    const { login, error, clearError } = useAuth();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [fieldErrors, setFieldErrors] = useState({});
    const [oauthError, setOauthError] = useState('');

    // Show error if redirected from failed OAuth login
    useEffect(() => {
        const errorParam = searchParams.get('error');
        if (errorParam === 'oauth_failed') {
            setOauthError('OAuth login failed. Please try again or use email/password.');
        }
    }, [searchParams]);

    const validate = () => {
        const errors = {};
        if (!email.trim()) errors.email = 'Email is required';
        else if (!/\S+@\S+\.\S+/.test(email)) errors.email = 'Invalid email format';
        if (!password) errors.password = 'Password is required';
        setFieldErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        clearError();
        if (!validate()) return;

        setLoading(true);
        try {
            await login(email, password);
            navigate('/dashboard');
        } catch {
            // error is set by context
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-page">
            <FloatingShapes />

            {/* Mesh gradient background */}
            <div className="auth-page__bg" />

            {/* Back button — top left */}
            <Link to="/" className="auth-page__back">
                <ArrowLeft size={18} />
                <span>Home</span>
            </Link>

            {/* Theme toggle — top right */}
            <div className="auth-page__theme-toggle">
                <ThemeToggle />
            </div>

            <div className="auth-page__container animate-fadeInUp">
                <GlassCard className="auth-card" glow>
                    {/* Header */}
                    <div className="auth-card__header">
                        <div className="auth-card__logo">
                            <Shield size={28} />
                        </div>
                        <h1 className="auth-card__title">
                            Welcome <span className="gradient-text">Back</span>
                        </h1>
                        <p className="auth-card__subtitle">Sign in to continue to your account</p>
                    </div>

                    {/* Error */}
                    {(error || oauthError) && (
                        <div className="auth-alert auth-alert--error animate-fadeIn">
                            <AlertCircle size={16} />
                            <span>{error || oauthError}</span>
                        </div>
                    )}

                    {/* Form */}
                    <form onSubmit={handleSubmit} className="auth-form">
                        <Input
                            id="login-email"
                            label="Email Address"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            icon={Mail}
                            error={fieldErrors.email}
                            autoComplete="email"
                        />

                        <Input
                            id="login-password"
                            label="Password"
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            icon={Lock}
                            error={fieldErrors.password}
                            autoComplete="current-password"
                        />

                        <Button type="submit" fullWidth loading={loading} size="lg">
                            Sign In
                        </Button>
                    </form>

                    {/* Divider */}
                    <div className="auth-divider">
                        <span>or continue with</span>
                    </div>

                    {/* OAuth */}
                    <div className="auth-oauth">
                        <a href={`${BACKEND_URL}/oauth2/authorization/google`} className="btn btn--oauth btn--md btn--full">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4" />
                                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
                                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05" />
                                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
                            </svg>
                            <span className="btn-text">Google</span>
                        </a>

                        <a href={`${BACKEND_URL}/oauth2/authorization/github`} className="btn btn--oauth btn--md btn--full">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                                <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z" />
                            </svg>
                            <span className="btn-text">GitHub</span>
                        </a>
                    </div>

                    {/* Footer */}
                    <p className="auth-footer">
                        Don't have an account? <Link to="/register">Create one</Link>
                    </p>
                </GlassCard>
            </div>
        </div>
    );
}
