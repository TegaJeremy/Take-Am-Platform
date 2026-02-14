require('dotenv').config();

module.exports = {
    secret: process.env.JWT_SECRET || 'your-secret-key-min-32-chars-long',
    expiresIn: '24h'
};