const TraderRequest = require('../models/TraderRequest');
const GradingDetails = require('../models/GradingDetails');
const { sendSMS } = require('./smsService');
const { checkAgentClockedIn } = require('../utils/userServiceClient');

class AgentRequestService {
    async getPendingRequests() {
        const requests = await TraderRequest.findAll({
            where: { status: 'PENDING' },
            order: [['createdAt', 'ASC']]
        });
        return requests;
    }

    async getAgentCurrentRequest(agentId) {
        const request = await TraderRequest.findOne({
            where: {
                agentId,
                status: 'ACCEPTED'
            },
            include: [{
                model: GradingDetails,
                as: 'grading',
                required: false
            }]
        });
        return request;
    }

    async acceptRequest(requestId, agentId, token) {
        // Check if agent is clocked in
        const isClockedIn = await checkAgentClockedIn(agentId, token);
        if (!isClockedIn) {
            throw new Error('You must clock in before accepting requests');
        }

        // Check if agent already has an active request
        const activeRequest = await this.getAgentCurrentRequest(agentId);
        if (activeRequest) {
            throw new Error('You already have an active request. Please close it first');
        }

        // Check if request exists and is pending
        const request = await TraderRequest.findByPk(requestId);
        if (!request) {
            throw new Error('Request not found');
        }
        if (request.status !== 'PENDING') {
            throw new Error('Request is not available');
        }

        // Accept the request
        request.agentId = agentId;
        request.status = 'ACCEPTED';
        request.acceptedAt = new Date();
        await request.save();

        return request;
    }

    async gradeRequest(requestId, agentId, gradingData) {
        // Verify request belongs to this agent
        const request = await TraderRequest.findOne({
            where: {
                id: requestId,
                agentId,
                status: 'ACCEPTED'
            }
        });

        if (!request) {
            throw new Error('Request not found or not assigned to you');
        }

        // Calculate totals
        const totalWeight =
            parseFloat(gradingData.ripeWeight || 0) +
            parseFloat(gradingData.unripeWeight || 0) +
            parseFloat(gradingData.semiSpoiledWeight || 0) +
            parseFloat(gradingData.spoiledWeight || 0);

        const totalAmount =
            (parseFloat(gradingData.ripeWeight || 0) * parseFloat(gradingData.ripePricePerKg || 1500)) +
            (parseFloat(gradingData.unripeWeight || 0) * parseFloat(gradingData.unripePricePerKg || 1000)) +
            (parseFloat(gradingData.semiSpoiledWeight || 0) * parseFloat(gradingData.semiSpoiledPricePerKg || 500));

        // Create or update grading details
        const [grading, created] = await GradingDetails.findOrCreate({
            where: { requestId },
            defaults: {
                ...gradingData,
                totalWeight,
                totalAmount
            }
        });

        if (!created) {
            grading.ripeWeight = gradingData.ripeWeight || 0;
            grading.unripeWeight = gradingData.unripeWeight || 0;
            grading.semiSpoiledWeight = gradingData.semiSpoiledWeight || 0;
            grading.spoiledWeight = gradingData.spoiledWeight || 0;
            grading.ripePricePerKg = gradingData.ripePricePerKg || 1500;
            grading.unripePricePerKg = gradingData.unripePricePerKg || 1000;
            grading.semiSpoiledPricePerKg = gradingData.semiSpoiledPricePerKg || 500;
            grading.totalWeight = totalWeight;
            grading.totalAmount = totalAmount;
            grading.agentNotes = gradingData.agentNotes;
            await grading.save();
        }

        return grading;
    }

    async closeRequest(requestId, agentId) {
        // Verify request belongs to this agent
        const request = await TraderRequest.findOne({
            where: {
                id: requestId,
                agentId,
                status: 'ACCEPTED'
            },
            include: [{
                model: GradingDetails,
                as: 'grading',
                required: true
            }]
        });

        if (!request) {
            throw new Error('Request not found or not assigned to you');
        }

        if (!request.grading) {
            throw new Error('Please complete grading before closing the request');
        }

        // Close the request
        request.status = 'COMPLETED';
        request.completedAt = new Date();
        await request.save();

        // Send SMS to trader
        const message = `TakeAm: Your goods have been inspected. Total weight: ${request.grading.totalWeight}kg. Amount to receive: ₦${request.grading.totalAmount}. Thank you!`;
        await sendSMS(request.traderPhone, message);

        return request;
    }

    async getAllRequests(filters = {}) {
        const where = {};

        if (filters.status) {
            where.status = filters.status;
        }

        if (filters.agentId) {
            where.agentId = filters.agentId;
        }

        const requests = await TraderRequest.findAll({
            where,
            order: [['createdAt', 'DESC']],
            include: [{
                model: GradingDetails,
                as: 'grading',
                required: false
            }]
        });

        return requests;
    }

    async getStatistics() {
        const totalRequests = await TraderRequest.count();
        const pendingRequests = await TraderRequest.count({ where: { status: 'PENDING' } });
        const acceptedRequests = await TraderRequest.count({ where: { status: 'ACCEPTED' } });
        const completedRequests = await TraderRequest.count({ where: { status: 'COMPLETED' } });

        return {
            total: totalRequests,
            pending: pendingRequests,
            accepted: acceptedRequests,
            completed: completedRequests
        };
    }
}

module.exports = new AgentRequestService();