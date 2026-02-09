const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/database');

const TraderRequest = sequelize.define('TraderRequest', {
    id: {
        type: DataTypes.UUID,
        defaultValue: DataTypes.UUIDV4,
        primaryKey: true
    },
    traderId: {
        type: DataTypes.UUID,
        allowNull: false,
        field: 'trader_id'
    },
    traderPhone: {
        type: DataTypes.STRING(20),
        allowNull: false,
        field: 'trader_phone'
    },
    traderName: {
        type: DataTypes.STRING(255),
        allowNull: false,
        field: 'trader_name'
    },
    traderAddress: {
        type: DataTypes.TEXT,
        field: 'trader_address'
    },
    status: {
        type: DataTypes.ENUM('PENDING', 'ACCEPTED', 'COMPLETED', 'CANCELLED'),
        allowNull: false,
        defaultValue: 'PENDING'
    },
    agentId: {
        type: DataTypes.UUID,
        allowNull: true,
        field: 'agent_id'
    },
    acceptedAt: {
        type: DataTypes.DATE,
        field: 'accepted_at'
    },
    completedAt: {
        type: DataTypes.DATE,
        field: 'completed_at'
    },
    notes: {
        type: DataTypes.TEXT
    }
}, {
    tableName: 'trader_requests',
    underscored: true,
    timestamps: true
});

module.exports = TraderRequest;