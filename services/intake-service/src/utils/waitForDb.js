module.exports = async function waitForDb(sequelize, retries = 10, delay = 5000) {
    for (let i = 1; i <= retries; i++) {
        try {
            await sequelize.authenticate();
            console.log('✅ Database is ready');
            return;
        } catch (err) {
            console.log(`⏳ Waiting for DB (${i}/${retries})...`);
            if (i === retries) throw err;
            await new Promise(r => setTimeout(r, delay));
        }
    }
};
