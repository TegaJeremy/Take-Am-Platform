const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
require('dotenv').config();

const app = express();


const PORT = process.env.PORT || 8084;
const HOST = '0.0.0.0';

console.log('='.repeat(50));
console.log('🔍 INTAKE SERVICE STARTUP');
console.log('='.repeat(50));
console.log('📍 NODE_ENV:', process.env.NODE_ENV || 'production');
console.log('🔌 PORT:', PORT);
console.log('🌐 HOST:', HOST);
console.log('='.repeat(50));

// Load database and routes
const { connectDB } = require('./config/database');
const traderRoutes = require('./routes/traderRoutes');
const agentRoutes = require('./routes/agentRoutes');
const adminRoutes = require('./routes/adminRoutes');
const errorHandler = require('./middlewares/errorHandler');
const gradingRoutes = require('./routes/gradingRoutes');

app.use(helmet());
app.use(cors());
app.use(morgan('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

console.log('✅ Middleware configured');


app.get('/health', (req, res) => {
    res.status(200).json({
        success: true,
        service: 'Intake Service',
        status: 'healthy',
        timestamp: new Date().toISOString(),
        port: PORT,
        env: process.env.NODE_ENV || 'production'
    });
});


app.get('/', (req, res) => {
    res.status(200).json({
        success: true,
        message: 'TakeAm Intake Service API',
        version: '1.0.0',
        endpoints: {
            health: '/health',
            traderRequests: '/api/v1/trader-requests',
            agentRequests: '/api/v1/agent-requests',
            admin: '/api/v1/admin'
        }
    });
});


app.use('/api/v1/trader-requests', traderRoutes);
app.use('/api/v1/agent-requests', agentRoutes);
app.use('/api/v1/admin', adminRoutes);
app.use('/api/v1/gradings', gradingRoutes);


console.log('✅ Routes configured');


app.use((req, res, next) => {
    res.status(404).json({
        success: false,
        message: 'Route not found',
        path: req.originalUrl
    });
});


app.use(errorHandler);


const startServer = async () => {
    try {

        await connectDB();

        // CRITICAL: Bind to 0.0.0.0 so Render can detect the service
        const server = app.listen(PORT, HOST, () => {
            console.log('='.repeat(50));
            console.log(' SERVER STARTED SUCCESSFULLY');
            console.log('='.repeat(50));
            console.log(` Listening on: http://${HOST}:${PORT}`);
            console.log(` Health check: http://${HOST}:${PORT}/health`);
            console.log(` Environment: ${process.env.NODE_ENV || 'production'}`);
            console.log('='.repeat(50));
        });

        // Handle server errors
        server.on('error', (error) => {
            console.error('='.repeat(50));
            console.error(' SERVER ERROR');
            console.error('='.repeat(50));
            if (error.code === 'EADDRINUSE') {
                console.error(` Port ${PORT} is already in use`);
            } else if (error.code === 'EACCES') {
                console.error(` Permission denied to bind to port ${PORT}`);
            } else {
                console.error(' Error:', error.message);
                console.error(' Code:', error.code);
            }
            console.error('='.repeat(50));
            process.exit(1);
        });

        // Graceful shutdown
        process.on('SIGTERM', () => {
            console.log('🔄 SIGTERM received, shutting down gracefully...');
            server.close(() => {
                console.log(' Server closed');
                process.exit(0);
            });
        });

    } catch (error) {
        console.error('='.repeat(50));
        console.error('STARTUP FAILED');
        console.error('='.repeat(50));
        console.error(' Error:', error.message);
        console.error(' Stack:', error.stack);
        console.error('='.repeat(50));
        process.exit(1);
    }
};


console.log('🔄 Initiating server startup...');
startServer().catch((error) => {
    console.error('Unhandled startup error:', error);
    process.exit(1);
});

module.exports = app;