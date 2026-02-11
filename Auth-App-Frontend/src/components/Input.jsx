import { useState } from 'react';
import './Input.css';

export default function Input({
    label,
    type = 'text',
    value,
    onChange,
    error,
    icon: Icon,
    id,
    ...props
}) {
    const [focused, setFocused] = useState(false);
    const isActive = focused || (value && value.length > 0);

    return (
        <div className={`input-group ${error ? 'input-group--error' : ''}`}>
            {Icon && (
                <span className="input-icon">
                    <Icon size={18} />
                </span>
            )}
            <input
                id={id}
                type={type}
                value={value}
                onChange={onChange}
                className={`input-field ${Icon ? 'input-field--has-icon' : ''}`}
                onFocus={() => setFocused(true)}
                onBlur={() => setFocused(false)}
                placeholder=" "
                {...props}
            />
            <label
                htmlFor={id}
                className={`input-label ${isActive ? 'input-label--active' : ''} ${Icon ? 'input-label--has-icon' : ''}`}
            >
                {label}
            </label>
            <div className={`input-focus-line ${focused ? 'input-focus-line--active' : ''}`} />
            {error && <span className="input-error">{error}</span>}
        </div>
    );
}
