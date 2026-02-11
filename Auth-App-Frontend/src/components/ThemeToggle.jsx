import { useEffect, useState } from 'react';
import { Sun, Moon } from 'lucide-react';
import './ThemeToggle.css';

export default function ThemeToggle() {
    const [theme, setTheme] = useState(() => {
        return localStorage.getItem('theme') || 'light';
    });

    useEffect(() => {
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem('theme', theme);
    }, [theme]);

    const toggle = () => {
        setTheme((prev) => (prev === 'light' ? 'dark' : 'light'));
    };

    return (
        <button
            className="theme-toggle"
            onClick={toggle}
            aria-label={`Switch to ${theme === 'light' ? 'dark' : 'light'} mode`}
            title={`Switch to ${theme === 'light' ? 'dark' : 'light'} mode`}
        >
            <span className={`theme-toggle__icon ${theme === 'light' ? 'theme-toggle__icon--active' : ''}`}>
                <Sun size={18} />
            </span>
            <span className={`theme-toggle__icon ${theme === 'dark' ? 'theme-toggle__icon--active' : ''}`}>
                <Moon size={18} />
            </span>
        </button>
    );
}
