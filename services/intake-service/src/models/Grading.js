const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/database');

const Grading = sequelize.define('Grading', {
    id: {
        type: DataTypes.UUID,
        defaultValue: DataTypes.UUIDV4,
        primaryKey: true
    },

    // Trader information (from User Service)
    traderId: {
        type: DataTypes.UUID,
        allowNull: false,
        comment: 'Trader UUID from User Service'
    },
    traderPhone: {
        type: DataTypes.STRING,
        allowNull: false,
        comment: 'Trader phone number for lookup'
    },
    traderName: {
        type: DataTypes.STRING,
        allowNull: false,
        comment: 'Trader full name'
    },
    traderBankAccount: {
        type: DataTypes.STRING,
        allowNull: true,
        comment: 'Trader bank account for payment'
    },
    traderBankName: {
        type: DataTypes.STRING,
        allowNull: true,
        comment: 'Trader bank name'
    },

    // Agent information (permanently tied)
    agentId: {
        type: DataTypes.UUID,
        allowNull: false,
        comment: 'Agent UUID - permanently tied to this transaction'
    },
    agentName: {
        type: DataTypes.STRING,
        allowNull: false,
        comment: 'Agent full name'
    },
    agentPhone: {
        type: DataTypes.STRING,
        allowNull: false,
        comment: 'Agent phone number'
    },

    // Grade weights (at least one required)
    gradeAWeight: {
        type: DataTypes.DECIMAL(10, 2),
        allowNull: true,
        defaultValue: 0,
        comment: 'Grade A - Fresh (kg)'
    },
    gradeBWeight: {
        type: DataTypes.DECIMAL(10, 2),
        allowNull: true,
        defaultValue: 0,
        comment: 'Grade B - Soft (kg)'
    },
    gradeCWeight: {
        type: DataTypes.DECIMAL(10, 2),
        allowNull: true,
        defaultValue: 0,
        comment: 'Grade C - Feed/Compost (kg)'
    },
    gradeDWeight: {
        type: DataTypes.DECIMAL(10, 2),
        allowNull: true,
        defaultValue: 0,
        comment: 'Grade D - Unripe (kg)'
    },

    // Calculated amounts
    gradeAAmount: {
        type: DataTypes.DECIMAL(10, 2),
        allowNull: true,
        defaultValue: 0,
        comment: 'Amount for Grade A'
    },
    gradeBAmount: {
        type: DataTypes.DECIMAL(10, 2),
        allowNull: true,
        defaultValue: 0,
        comment: 'Amount for Grade B'
    },
    gradeCAmount: {
        type: DataTypes.DECIMAL(10, 2),
        allowNull: true,
        defaultValue: 0,
        comment: 'Amount for Grade C'
    },
    gradeDAmount: {
        type: DataTypes.DECIMAL(10, 2),
        allowNull: true,
        defaultValue: 0,
        comment: 'Amount for Grade D (deferred)'
    },

    // Totals
    totalWeight: {
        type: DataTypes.DECIMAL(10, 2),
        allowNull: false,
        comment: 'Total weight across all grades'
    },
    totalAmount: {
        type: DataTypes.DECIMAL(10, 2),
        allowNull: false,
        comment: 'Total amount to be paid (excludes Grade D)'
    },

    // Pricing info (for audit trail)
    baseReferencePrice: {
        type: DataTypes.DECIMAL(10, 2),
        allowNull: false,
        comment: 'BRP used for this calculation'
    },

    // Agent notes
    agentNotes: {
        type: DataTypes.TEXT,
        allowNull: true,
        comment: 'Agent observations about the goods'
    },

    // Payment status
    paymentStatus: {
        type: DataTypes.ENUM('PENDING', 'PAID', 'FAILED'),
        defaultValue: 'PENDING',
        comment: 'Payment status for admin tracking'
    },

    // Timestamps
    gradedAt: {
        type: DataTypes.DATE,
        defaultValue: DataTypes.NOW,
        comment: 'When grading was completed'
    },
    paidAt: {
        type: DataTypes.DATE,
        allowNull: true,
        comment: 'When payment was made'
    }
}, {
    tableName: 'gradings',
    timestamps: true,
    indexes: [
        { fields: ['traderId'] },
        { fields: ['agentId'] },
        { fields: ['traderPhone'] },
        { fields: ['paymentStatus'] },
        { fields: ['gradedAt'] }
    ]
});

module.exports = Grading;