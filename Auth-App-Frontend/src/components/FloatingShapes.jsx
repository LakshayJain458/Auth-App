import './FloatingShapes.css';

export default function FloatingShapes() {
    return (
        <div className="floating-shapes" aria-hidden="true">
            {/* Sphere 1 — top-left */}
            <div className="shape shape--sphere shape--1" />

            {/* Cube 2 — top-right */}
            <div className="shape shape--cube shape--2">
                <div className="cube-face cube-face--front" />
                <div className="cube-face cube-face--back" />
                <div className="cube-face cube-face--left" />
                <div className="cube-face cube-face--right" />
                <div className="cube-face cube-face--top" />
                <div className="cube-face cube-face--bottom" />
            </div>

            {/* Sphere 3 — bottom-right */}
            <div className="shape shape--sphere shape--3" />

            {/* Ring 4 — center-left */}
            <div className="shape shape--ring shape--4" />

            {/* Sphere 5 — bottom-left */}
            <div className="shape shape--sphere shape--5" />
        </div>
    );
}
