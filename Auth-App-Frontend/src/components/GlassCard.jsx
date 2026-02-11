import { useState, useRef } from 'react';
import './GlassCard.css';

export default function GlassCard({ children, className = '', tilt = true, glow = false, ...props }) {
    const cardRef = useRef(null);
    const [transform, setTransform] = useState('');
    const [glowPos, setGlowPos] = useState({ x: 50, y: 50 });

    const handleMouseMove = (e) => {
        if (!tilt || !cardRef.current) return;
        const rect = cardRef.current.getBoundingClientRect();
        const x = (e.clientX - rect.left) / rect.width;
        const y = (e.clientY - rect.top) / rect.height;
        const rotateX = (y - 0.5) * -10;
        const rotateY = (x - 0.5) * 10;
        setTransform(`perspective(800px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale3d(1.02, 1.02, 1.02)`);
        setGlowPos({ x: x * 100, y: y * 100 });
    };

    const handleMouseLeave = () => {
        setTransform('');
    };

    return (
        <div
            ref={cardRef}
            className={`glass-card ${glow ? 'glass-card--glow' : ''} ${className}`}
            style={{
                transform,
                '--glow-x': `${glowPos.x}%`,
                '--glow-y': `${glowPos.y}%`,
            }}
            onMouseMove={handleMouseMove}
            onMouseLeave={handleMouseLeave}
            {...props}
        >
            {children}
        </div>
    );
}
