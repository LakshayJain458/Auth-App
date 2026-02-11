import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import FloatingShapes from '../components/FloatingShapes';
import GlassCard from '../components/GlassCard';
import Button from '../components/Button';
import {
    Shield, Lock, Globe, Zap, KeyRound, Users,
    ArrowRight, Github, CheckCircle2
} from 'lucide-react';
import './HomePage.css';

const FEATURES = [
    {
        icon: Lock,
        title: 'JWT Authentication',
        desc: 'Stateless access & refresh tokens with automatic rotation and secure HTTP-only cookies.',
    },
    {
        icon: Globe,
        title: 'OAuth2 Social Login',
        desc: 'Sign in seamlessly with Google or GitHub — no extra passwords to remember.',
    },
    {
        icon: KeyRound,
        title: 'Token Rotation',
        desc: 'Refresh tokens are rotated on every use with old tokens automatically revoked.',
    },
    {
        icon: Users,
        title: 'User Management',
        desc: 'Full CRUD user profiles with role-based access control and provider tracking.',
    },
    {
        icon: Shield,
        title: 'Secure by Default',
        desc: 'BCrypt hashing, CORS protection, CSRF-safe stateless API, and input validation.',
    },
    {
        icon: Zap,
        title: 'Production Ready',
        desc: 'Spring Boot 3 backend with Swagger docs, actuator monitoring, and multi-profile configs.',
    },
];

const TECH_STACK = [
    'Spring Boot 3', 'Spring Security', 'PostgreSQL', 'React 19',
    'JWT (JJWT)', 'OAuth2 Client', 'Vite', 'Axios',
];

export default function HomePage() {
    const { isAuthenticated } = useAuth();

    return (
        <div className="home">
            <FloatingShapes />
            <div className="home__bg" />

            {/* ─── Hero ─── */}
            <section className="home__hero">
                <div className="home__hero-content animate-fadeInUp">
                    <div className="home__hero-badge">
                        <CheckCircle2 size={14} />
                        <span>Open-source &amp; self-hosted</span>
                    </div>

                    <h1 className="home__hero-title">
                        Secure Authentication,{' '}
                        <span className="gradient-text">Made Simple</span>
                    </h1>

                    <p className="home__hero-subtitle">
                        A full-stack auth system built with Spring Boot&nbsp;3 &amp; React.
                        JWT tokens, OAuth2 social login, refresh-token rotation — everything
                        you need, out of the box.
                    </p>

                    <div className="home__hero-actions">
                        {isAuthenticated ? (
                            <Link to="/dashboard">
                                <Button size="lg" icon={ArrowRight}>
                                    Go to Dashboard
                                </Button>
                            </Link>
                        ) : (
                            <>
                                <Link to="/register">
                                    <Button size="lg" icon={ArrowRight}>
                                        Get Started
                                    </Button>
                                </Link>
                                <Link to="/login">
                                    <Button variant="secondary" size="lg">
                                        Sign In
                                    </Button>
                                </Link>
                            </>
                        )}
                    </div>

                    {/* Tech pills */}
                    <div className="home__tech-row">
                        {TECH_STACK.map((t) => (
                            <span key={t} className="home__tech-pill">{t}</span>
                        ))}
                    </div>
                </div>

                {/* Decorative hero card */}
                <div className="home__hero-visual animate-fadeInUp delay-2">
                    <GlassCard className="home__hero-card" glow>
                        <div className="home__hero-card-header">
                            <div className="home__hero-card-dot home__hero-card-dot--red" />
                            <div className="home__hero-card-dot home__hero-card-dot--yellow" />
                            <div className="home__hero-card-dot home__hero-card-dot--green" />
                        </div>
                        <pre className="home__hero-code"><code>{`POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "••••••••"
}

→ 200 OK
{
  "accessToken": "eyJhbG...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": { "email": "user@…" }
}`}</code></pre>
                    </GlassCard>
                </div>
            </section>

            {/* ─── Features ─── */}
            <section className="home__features" id="features">
                <h2 className="home__section-title animate-fadeInUp">
                    Everything You Need to{' '}
                    <span className="gradient-text">Ship Auth Fast</span>
                </h2>
                <p className="home__section-subtitle animate-fadeInUp delay-1">
                    Stop reinventing the wheel. This starter comes pre-wired with best-practice
                    patterns used in production apps.
                </p>

                <div className="home__features-grid">
                    {FEATURES.map(({ icon: Icon, title, desc }, i) => (
                        <GlassCard
                            key={title}
                            className={`home__feature-card animate-fadeInUp delay-${Math.min(i + 1, 5)}`}
                            tilt
                        >
                            <div className="home__feature-icon">
                                <Icon size={24} />
                            </div>
                            <h3 className="home__feature-title">{title}</h3>
                            <p className="home__feature-desc">{desc}</p>
                        </GlassCard>
                    ))}
                </div>
            </section>

            {/* ─── CTA ─── */}
            <section className="home__cta animate-fadeInUp">
                <GlassCard className="home__cta-card" glow>
                    <Shield size={40} className="home__cta-icon" />
                    <h2 className="home__cta-title">Ready to Get Started?</h2>
                    <p className="home__cta-subtitle">
                        Create an account in seconds and explore the dashboard.
                    </p>
                    <div className="home__cta-actions">
                        {isAuthenticated ? (
                            <Link to="/dashboard">
                                <Button size="lg" icon={ArrowRight}>Dashboard</Button>
                            </Link>
                        ) : (
                            <>
                                <Link to="/register">
                                    <Button size="lg" icon={ArrowRight}>Create Free Account</Button>
                                </Link>
                                <a
                                    href="https://github.com/LakshayJain458/Auth-App.git"
                                    target="_blank"
                                    rel="noopener noreferrer"
                                >
                                    <Button variant="secondary" size="lg" icon={Github}>
                                        View Source
                                    </Button>
                                </a>
                            </>
                        )}
                    </div>
                </GlassCard>
            </section>

            {/* ─── Footer ─── */}
            <footer className="home__footer">
                <p>
                    Built with ❤️ by <strong>Lakshay</strong> &nbsp;·&nbsp; Spring Boot &amp; React
                </p>
            </footer>
        </div>
    );
}
