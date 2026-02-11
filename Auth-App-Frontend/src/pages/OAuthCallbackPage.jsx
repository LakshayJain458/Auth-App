import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import FloatingShapes from '../components/FloatingShapes';
import GlassCard from '../components/GlassCard';
import { Loader2, CheckCircle2, AlertCircle } from 'lucide-react';
import './AuthPages.css';

export default function OAuthCallbackPage() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { handleOAuthCallback } = useAuth();
    const [status, setStatus] = useState('processing');
    const [errorMsg, setErrorMsg] = useState('');

    useEffect(() => {
        const token = searchParams.get('token');
        if (!token) {
            setStatus('error');
            setErrorMsg('No authentication token received.');
            setTimeout(() => navigate('/login'), 3000);
            return;
        }

        const processCallback = async () => {
            try {
                await handleOAuthCallback(token);
                setStatus('success');
                setTimeout(() => navigate('/dashboard'), 1500);
            } catch (err) {
                setStatus('error');
                setErrorMsg('Failed to complete authentication. Please try again.');
                setTimeout(() => navigate('/login'), 3000);
            }
        };

        processCallback();
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    return (
        <div className="auth-page">
            <FloatingShapes />
            <div className="auth-page__bg" />

            <div className="auth-page__container animate-fadeInUp">
                <GlassCard className="auth-card" style={{ textAlign: 'center', padding: '60px 40px' }}>
                    {status === 'processing' && (
                        <>
                            <Loader2 size={48} className="btn-spinner" style={{ color: 'var(--primary)', marginBottom: 20 }} />
                            <h2 style={{ marginBottom: 8 }}>Completing Sign In...</h2>
                            <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>Please wait while we set up your session.</p>
                        </>
                    )}
                    {status === 'success' && (
                        <>
                            <CheckCircle2 size={48} style={{ color: 'var(--success)', marginBottom: 20 }} />
                            <h2 style={{ marginBottom: 8 }}>Welcome!</h2>
                            <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>Redirecting to your dashboard...</p>
                        </>
                    )}
                    {status === 'error' && (
                        <>
                            <AlertCircle size={48} style={{ color: 'var(--error)', marginBottom: 20 }} />
                            <h2 style={{ marginBottom: 8 }}>Authentication Failed</h2>
                            <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>{errorMsg}</p>
                        </>
                    )}
                </GlassCard>
            </div>
        </div>
    );
}
