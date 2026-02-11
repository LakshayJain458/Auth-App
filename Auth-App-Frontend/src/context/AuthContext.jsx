import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authAPI, userAPI } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(() => {
        const saved = localStorage.getItem('user');
        return saved ? JSON.parse(saved) : null;
    });
    const [token, setToken] = useState(() => localStorage.getItem('accessToken'));
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const isAuthenticated = !!token && !!user;

    // Persist user to localStorage
    useEffect(() => {
        if (user) {
            localStorage.setItem('user', JSON.stringify(user));
        } else {
            localStorage.removeItem('user');
        }
    }, [user]);

    // Try refreshing token on mount (only if we have an existing token)
    useEffect(() => {
        const initAuth = async () => {
            const existingToken = localStorage.getItem('accessToken');
            if (!existingToken) {
                setLoading(false);
                return;
            }

            try {
                const { data } = await authAPI.refreshToken();
                setToken(data.accessToken);
                localStorage.setItem('accessToken', data.accessToken);
                if (data.user) {
                    setUser(data.user);
                }
            } catch {
                // Refresh failed — clear state silently
                setToken(null);
                setUser(null);
                localStorage.removeItem('accessToken');
                localStorage.removeItem('user');
            }
            setLoading(false);
        };
        initAuth();
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    const login = useCallback(async (email, password) => {
        setError(null);
        try {
            const { data } = await authAPI.login({ email, password });
            setToken(data.accessToken);
            localStorage.setItem('accessToken', data.accessToken);
            setUser(data.user);
            return data;
        } catch (err) {
            const msg = err.response?.data?.message || 'Login failed. Please try again.';
            setError(msg);
            throw err;
        }
    }, []);

    const register = useCallback(async (email, username, password) => {
        setError(null);
        try {
            const { data } = await authAPI.register({ email, username, password });
            return data;
        } catch (err) {
            const msg = err.response?.data?.message || 'Registration failed. Please try again.';
            setError(msg);
            throw err;
        }
    }, []);

    const logout = useCallback(async () => {
        try {
            await authAPI.logout();
        } catch {
            // Ignore logout errors
        } finally {
            setToken(null);
            setUser(null);
            localStorage.removeItem('accessToken');
            localStorage.removeItem('user');
        }
    }, []);

    // Handle OAuth callback — token comes from URL params, need to fetch user info
    const handleOAuthCallback = useCallback(async (accessToken) => {
        setError(null);
        try {
            // Store the access token
            setToken(accessToken);
            localStorage.setItem('accessToken', accessToken);

            // Try to refresh to get user data (the refresh token is already in the cookie)
            try {
                const { data } = await authAPI.refreshToken();
                setToken(data.accessToken);
                localStorage.setItem('accessToken', data.accessToken);
                if (data.user) {
                    setUser(data.user);
                    return data;
                }
            } catch {
                // If refresh fails, try to decode basic info from the token itself
                // The token has claims with email, so we just set what we have
            }

            // If we couldn't get user from refresh, set a minimal user object
            // The user can still access the dashboard and we refresh on next load
            const tokenPayload = JSON.parse(atob(accessToken.split('.')[1]));
            const minimalUser = {
                id: tokenPayload.sub,
                email: tokenPayload.email || '',
                username: tokenPayload.email?.split('@')[0] || 'User',
                provider: 'OAUTH2',
                enabled: true,
            };
            setUser(minimalUser);
            return minimalUser;
        } catch (err) {
            const msg = 'OAuth login failed. Please try again.';
            setError(msg);
            throw err;
        }
    }, []);

    const updateProfile = useCallback(async (updates) => {
        setError(null);
        try {
            const { data } = await userAPI.update(user.id, updates);
            setUser(data);
            return data;
        } catch (err) {
            const msg = err.response?.data?.message || 'Update failed.';
            setError(msg);
            throw err;
        }
    }, [user]);

    const clearError = useCallback(() => setError(null), []);

    const value = {
        user,
        token,
        isAuthenticated,
        loading,
        error,
        login,
        register,
        logout,
        handleOAuthCallback,
        updateProfile,
        clearError,
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
}

export default AuthContext;
