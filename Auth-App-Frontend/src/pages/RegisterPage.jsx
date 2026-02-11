import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import FloatingShapes from '../components/FloatingShapes';
import GlassCard from '../components/GlassCard';
import Input from '../components/Input';
import Button from '../components/Button';
import ThemeToggle from '../components/ThemeToggle';
import { Mail, Lock, User, Shield, AlertCircle, CheckCircle2, ArrowLeft } from 'lucide-react';
import './AuthPages.css';

export default function RegisterPage() {
    const { register, error, clearError } = useAuth();
    const navigate = useNavigate();

    const [form, setForm] = useState({
        email: '',
        username: '',
        password: '',
        confirmPassword: '',
    });
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);
    const [fieldErrors, setFieldErrors] = useState({});

    const handleChange = (field) => (e) => {
        setForm((prev) => ({ ...prev, [field]: e.target.value }));
        if (fieldErrors[field]) {
            setFieldErrors((prev) => ({ ...prev, [field]: '' }));
        }
    };

    const validate = () => {
        const errors = {};
        if (!form.email.trim()) errors.email = 'Email is required';
        else if (!/\S+@\S+\.\S+/.test(form.email)) errors.email = 'Invalid email format';
        if (!form.username.trim()) errors.username = 'Username is required';
        else if (form.username.length < 3) errors.username = 'At least 3 characters';
        if (!form.password) errors.password = 'Password is required';
        else if (form.password.length < 6) errors.password = 'At least 6 characters';
        if (form.password !== form.confirmPassword) errors.confirmPassword = 'Passwords do not match';
        setFieldErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        clearError();
        if (!validate()) return;

        setLoading(true);
        try {
            await register(form.email, form.username, form.password);
            setSuccess(true);
            setTimeout(() => navigate('/login'), 2000);
        } catch {
            // error set by context
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-page">
            <FloatingShapes />
            <div className="auth-page__bg" />

            {/* Back button — top left */}
            <Link to="/" className="auth-page__back">
                <ArrowLeft size={18} />
                <span>Home</span>
            </Link>

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
                            Create <span className="gradient-text">Account</span>
                        </h1>
                        <p className="auth-card__subtitle">Start your journey with us today</p>
                    </div>

                    {/* Success */}
                    {success && (
                        <div className="auth-alert auth-alert--success animate-fadeIn">
                            <CheckCircle2 size={16} />
                            <span>Account created! Redirecting to login...</span>
                        </div>
                    )}

                    {/* Error */}
                    {error && !success && (
                        <div className="auth-alert auth-alert--error animate-fadeIn">
                            <AlertCircle size={16} />
                            <span>{error}</span>
                        </div>
                    )}

                    {/* Form */}
                    <form onSubmit={handleSubmit} className="auth-form">
                        <Input
                            id="register-email"
                            label="Email Address"
                            type="email"
                            value={form.email}
                            onChange={handleChange('email')}
                            icon={Mail}
                            error={fieldErrors.email}
                            autoComplete="email"
                        />

                        <Input
                            id="register-username"
                            label="Username"
                            type="text"
                            value={form.username}
                            onChange={handleChange('username')}
                            icon={User}
                            error={fieldErrors.username}
                            autoComplete="username"
                        />

                        <Input
                            id="register-password"
                            label="Password"
                            type="password"
                            value={form.password}
                            onChange={handleChange('password')}
                            icon={Lock}
                            error={fieldErrors.password}
                            autoComplete="new-password"
                        />

                        <Input
                            id="register-confirm"
                            label="Confirm Password"
                            type="password"
                            value={form.confirmPassword}
                            onChange={handleChange('confirmPassword')}
                            icon={Lock}
                            error={fieldErrors.confirmPassword}
                            autoComplete="new-password"
                        />

                        <Button type="submit" fullWidth loading={loading} size="lg">
                            Create Account
                        </Button>
                    </form>

                    {/* Footer */}
                    <p className="auth-footer" style={{ marginTop: 24 }}>
                        Already have an account? <Link to="/login">Sign in</Link>
                    </p>
                </GlassCard>
            </div>
        </div>
    );
}
