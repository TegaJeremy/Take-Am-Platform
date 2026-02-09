const { Sequelize } = require('sequelize');

let sequelize;

// Check environment variables
console.log('🔍 Environment check:');
console.log('  - NODE_ENV:', process.env.NODE_ENV);
console.log('  - DATABASE_URL:', process.env.DATABASE_URL ? '✅ Set' : '❌ Not set');
console.log('  - PORT:', process.env.PORT);

if (process.env.DATABASE_URL) {
    // ===== Production / Render / Neon =====
    console.log('🟢 Using DATABASE_URL (Neon / Render)');

    sequelize = new Sequelize(process.env.DATABASE_URL, {
        dialect: 'postgres',
        logging: process.env.NODE_ENV === 'development' ? console.log : false,
        dialectOptions: {
            ssl: {
                require: true,
                rejectUnauthorized: false,
            },
        },
        pool: {
            max: 10,
            min: 0,
            acquire: 30000,
            idle: 10000,
        },
    });
} else {
    // ===== Local development =====
    console.log('🟡 Using local Postgres config');
    console.log('  - DB_NAME:', process.env.DB_NAME || process.env.POSTGRES_DB);
    console.log('  - DB_USER:', process.env.DB_USER || process.env.POSTGRES_USER);
    console.log('  - DB_HOST:', process.env.DB_HOST);

    const dbName = process.env.DB_NAME || process.env.POSTGRES_DB || 'takeam_users';
    const dbUser = process.env.DB_USER || process.env.POSTGRES_USER || 'takeam';
    const dbPassword = process.env.DB_PASSWORD || process.env.POSTGRES_PASSWORD || 'takeam123';
    const dbHost = process.env.DB_HOST || 'localhost';
    const dbPort = process.env.DB_PORT || 5432;

    sequelize = new Sequelize(dbName, dbUser, dbPassword, {
        host: dbHost,
        port: dbPort,
        dialect: 'postgres',
        logging: console.log,
        pool: {
            max: 5,
            min: 0,
            acquire: 30000,
            idle: 10000,
        },
    });
}

const connectDB = async () => {
    try {
        await sequelize.authenticate();
        console.log(' Database connected successfully');
        console.log(`Connected to database: ${sequelize.config.database || 'from URL'}`);

        await sequelize.sync({ alter: false });
        console.log('Database models synced');
    } catch (error) {
        console.error(' Database connection failed:', error.message);
        console.error('Full error:', error);
        throw error;
    }
};

module.exports = { sequelize, connectDB };