const jwt = require('jsonwebtoken');
const jwtConfig = require('../config/jwt');

const authenticate = (req, res, next) => {
    try {
        const authHeader = req.headers.authorization;

        if (!authHeader || !authHeader.startsWith('Bearer ')) {
            return res.status(401).json({
                success: false,
                message: 'No token provided'
            });
        }

        const token = authHeader.substring(7);
        const decoded = jwt.verify(token, jwtConfig.secret);

        console.log('Decoded JWT:', decoded); // Debug log

        req.user = {
            id: decoded.sub || decoded.userId || decoded.id,
            role: decoded.role || decoded.roles?.[0],
            phoneNumber: decoded.phoneNumber || decoded.phone,
            fullName: decoded.fullName || decoded.name || 'Agent User' // Default fallback
        };

        console.log('✅ Authenticated user:', req.user);

        next();
    } catch (error) {
        console.error('JWT Error:', error.message);
        return res.status(401).json({
            success: false,
            message: 'Invalid or expired token',
            error: error.message
        });
    }
};

const authorizeRoles = (...roles) => {
    return (req, res, next) => {
        if (!roles.includes(req.user.role)) {
            return res.status(403).json({
                success: false,
                message: `Access denied. Required roles: ${roles.join(', ')}`
            });
        }
        next();
    };
};


module.exports = { authenticate, authorizeRoles };