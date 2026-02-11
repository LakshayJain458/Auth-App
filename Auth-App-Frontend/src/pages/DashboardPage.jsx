import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import GlassCard from '../components/GlassCard';
import Input from '../components/Input';
import Button from '../components/Button';
import {
    User, Mail, Calendar, Shield, Globe, Edit3, Check, X,
    AlertCircle, CheckCircle2, LogOut
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import './DashboardPage.css';

export default function DashboardPage() {
    const { user, logout, updateProfile, error, clearError } = useAuth();
    const navigate = useNavigate();

    const [editing, setEditing] = useState(false);
    const [editForm, setEditForm] = useState({
        username: user?.username || '',
        email: user?.email || '',
    });
    const [saving, setSaving] = useState(false);
    const [successMsg, setSuccessMsg] = useState('');

    const handleEdit = () => {
        setEditing(true);
        setEditForm({ username: user?.username || '', email: user?.email || '' });
        clearError();
        setSuccessMsg('');
    };

    const handleCancel = () => {
        setEditing(false);
        clearError();
    };

    const handleSave = async () => {
        clearError();
        setSaving(true);
        try {
            await updateProfile(editForm);
            setEditing(false);
            setSuccessMsg('Profile updated successfully!');
            setTimeout(() => setSuccessMsg(''), 3000);
        } catch {
            // error handled by context
        } finally {
            setSaving(false);
        }
    };

    const handleLogout = async () => {
        await logout();
        navigate('/');
    };

    const formatDate = (dateStr) => {
        if (!dateStr) return 'N/A';
        return new Date(dateStr).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
        });
    };

    const providerIcon = (provider) => {
        switch (provider?.toUpperCase()) {
            case 'GOOGLE': return '🔵';
            case 'GITHUB': return '⚫';
            default: return '🔒';
        }
    };

    return (
        <div className="dashboard">
            <div className="dashboard__bg" />

            <div className="dashboard__container">
                {/* Profile Header Card */}
                <GlassCard className="dashboard__profile-card animate-fadeInUp" glow>
                    <div className="profile-header">
                        <div className="profile-header__avatar">
                            {user?.image ? (
                                <img src={user.image} alt={user.username} />
                            ) : (
                                <div className="profile-header__avatar-placeholder">
                                    <span>{user?.username?.charAt(0)?.toUpperCase() || 'U'}</span>
                                </div>
                            )}
                            <div className="profile-header__provider-badge" title={user?.provider || 'LOCAL'}>
                                {providerIcon(user?.provider)}
                            </div>
                        </div>
                        <div className="profile-header__info">
                            <h1 className="profile-header__name">{user?.username || 'User'}</h1>
                            <p className="profile-header__email">{user?.email}</p>
                            <div className="profile-header__meta">
                                <span className="profile-meta-item">
                                    <Calendar size={14} />
                                    Joined {formatDate(user?.createdAt)}
                                </span>
                                <span className={`profile-meta-item profile-status ${user?.enabled !== false ? 'profile-status--active' : 'profile-status--disabled'}`}>
                                    <span className="profile-status__dot" />
                                    {user?.enabled !== false ? 'Active' : 'Disabled'}
                                </span>
                            </div>
                        </div>
                        <div className="profile-header__actions">
                            {!editing && (
                                <Button variant="secondary" size="sm" icon={Edit3} onClick={handleEdit}>
                                    Edit Profile
                                </Button>
                            )}
                        </div>
                    </div>
                </GlassCard>

                <div className="dashboard__grid">
                    {/* Account Details Card */}
                    <GlassCard className="dashboard__details-card animate-fadeInUp delay-1">
                        <h2 className="card-title">
                            <Shield size={20} />
                            Account Details
                        </h2>

                        {/* Alerts */}
                        {successMsg && (
                            <div className="auth-alert auth-alert--success animate-fadeIn" style={{ marginBottom: 16 }}>
                                <CheckCircle2 size={16} />
                                <span>{successMsg}</span>
                            </div>
                        )}
                        {error && (
                            <div className="auth-alert auth-alert--error animate-fadeIn" style={{ marginBottom: 16 }}>
                                <AlertCircle size={16} />
                                <span>{error}</span>
                            </div>
                        )}

                        {editing ? (
                            <div className="edit-form">
                                <Input
                                    id="edit-username"
                                    label="Username"
                                    value={editForm.username}
                                    onChange={(e) => setEditForm({ ...editForm, username: e.target.value })}
                                    icon={User}
                                />
                                <Input
                                    id="edit-email"
                                    label="Email"
                                    type="email"
                                    value={editForm.email}
                                    onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
                                    icon={Mail}
                                />
                                <div className="edit-form__actions">
                                    <Button variant="primary" size="sm" icon={Check} loading={saving} onClick={handleSave}>
                                        Save Changes
                                    </Button>
                                    <Button variant="ghost" size="sm" icon={X} onClick={handleCancel}>
                                        Cancel
                                    </Button>
                                </div>
                            </div>
                        ) : (
                            <div className="details-list">
                                <div className="details-item">
                                    <span className="details-item__icon"><User size={16} /></span>
                                    <div>
                                        <span className="details-item__label">Username</span>
                                        <span className="details-item__value">{user?.username}</span>
                                    </div>
                                </div>
                                <div className="details-item">
                                    <span className="details-item__icon"><Mail size={16} /></span>
                                    <div>
                                        <span className="details-item__label">Email</span>
                                        <span className="details-item__value">{user?.email}</span>
                                    </div>
                                </div>
                                <div className="details-item">
                                    <span className="details-item__icon"><Globe size={16} /></span>
                                    <div>
                                        <span className="details-item__label">Auth Provider</span>
                                        <span className="details-item__value">{user?.provider || 'LOCAL'}</span>
                                    </div>
                                </div>
                                <div className="details-item">
                                    <span className="details-item__icon"><Calendar size={16} /></span>
                                    <div>
                                        <span className="details-item__label">Last Updated</span>
                                        <span className="details-item__value">{formatDate(user?.updatedAt)}</span>
                                    </div>
                                </div>
                            </div>
                        )}
                    </GlassCard>

                    {/* Roles & Security Card */}
                    <GlassCard className="dashboard__security-card animate-fadeInUp delay-2">
                        <h2 className="card-title">
                            <Shield size={20} />
                            Roles & Security
                        </h2>

                        <div className="roles-section">
                            <h3 className="roles-section__title">Assigned Roles</h3>
                            <div className="roles-list">
                                {user?.roles && user.roles.length > 0 ? (
                                    user.roles.map((role, i) => (
                                        <span key={i} className="role-badge">
                                            {role.name || role}
                                        </span>
                                    ))
                                ) : (
                                    <span className="roles-empty">No roles assigned</span>
                                )}
                            </div>
                        </div>

                        <div className="security-section">
                            <h3 className="roles-section__title">Security Info</h3>
                            <div className="security-item">
                                <span className="security-item__label">Account Status</span>
                                <span className={`security-item__badge ${user?.enabled !== false ? 'security-item__badge--active' : 'security-item__badge--disabled'}`}>
                                    {user?.enabled !== false ? 'Active' : 'Disabled'}
                                </span>
                            </div>
                            <div className="security-item">
                                <span className="security-item__label">Provider</span>
                                <span className="security-item__badge">{user?.provider || 'LOCAL'}</span>
                            </div>
                        </div>

                        <div className="logout-section">
                            <Button variant="danger" size="md" icon={LogOut} fullWidth onClick={handleLogout}>
                                Sign Out
                            </Button>
                        </div>
                    </GlassCard>
                </div>
            </div>
        </div>
    );
}
