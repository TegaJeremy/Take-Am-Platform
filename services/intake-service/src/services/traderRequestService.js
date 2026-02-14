const TraderRequest = require('../models/TraderRequest');
const GradingDetails = require('../models/GradingDetails');
const { getTraderDetails } = require('../utils/userServiceClient');

class TraderRequestService {
    async createRequest(traderId, traderPhone, traderName, token) {
        // Fetch trader details from user service (this includes the trader profile)
        const traderDetails = await getTraderDetails(traderId, token);

        // Use the trader profile ID if available
        const actualTraderId = traderDetails?.id || traderId;

        const request = await TraderRequest.create({
            traderId: actualTraderId,
            traderPhone,
            traderName,
            traderAddress: null,
            status: 'PENDING'
        });

        return {
            ...request.toJSON(),
            traderDetails: traderDetails || null
        };
    }

    async getTraderRequests(traderId, token) {
        const requests = await TraderRequest.findAll({
            where: { traderId },
            order: [['createdAt', 'DESC']],
            include: [{
                model: GradingDetails,
                as: 'grading',
                required: false
            }]
        });

        // Fetch trader details for display
        const traderDetails = await getTraderDetails(traderId, token);

        return {
            requests,
            traderDetails: traderDetails || null
        };
    }

    async getRequestById(requestId, token) {
        const request = await TraderRequest.findByPk(requestId, {
            include: [{
                model: GradingDetails,
                as: 'grading',
                required: false
            }]
        });

        if (!request) return null;

        // Fetch trader details
        const traderDetails = await getTraderDetails(request.traderId, token);

        return {
            ...request.toJSON(),
            traderDetails: traderDetails || null
        };
    }
}

module.exports = new TraderRequestService();