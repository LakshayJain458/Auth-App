import { Loader2 } from 'lucide-react';
import './Button.css';

export default function Button({
    children,
    variant = 'primary',
    size = 'md',
    loading = false,
    icon: Icon,
    fullWidth = false,
    className = '',
    ...props
}) {
    return (
        <button
            className={`btn btn--${variant} btn--${size} ${fullWidth ? 'btn--full' : ''} ${loading ? 'btn--loading' : ''} ${className}`}
            disabled={loading || props.disabled}
            {...props}
        >
            {loading ? (
                <Loader2 className="btn-spinner" size={18} />
            ) : Icon ? (
                <Icon size={18} className="btn-icon" />
            ) : null}
            <span className="btn-text">{children}</span>
        </button>
    );
}
