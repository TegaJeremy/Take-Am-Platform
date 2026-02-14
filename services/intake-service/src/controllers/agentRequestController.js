const agentRequestService = require('../services/agentRequestService');
const { gradeRequestSchema } = require('../utils/validators');

class AgentRequestController {
    async getPendingRequests(req, res, next) {
        try {
            const token = req.headers.authorization;
            console.log('🎫 Controller received token:', token ? 'YES' : 'NO');

            const requests = await agentRequestService.getPendingRequests(token);

            res.json({
                success: true,
                data: requests
            });
        } catch (error) {
            next(error);
        }
    }

    async getMyCurrentRequest(req, res, next) {
        try {
            const { id: agentId } = req.user;
            const token = req.headers.authorization;
            const request = await agentRequestService.getAgentCurrentRequest(agentId, token);

            res.json({
                success: true,
                data: request
            });
        } catch (error) {
            next(error);
        }
    }

    async acceptRequest(req, res, next) {
        try {
            const { id } = req.params;
            const { id: agentId } = req.user;
            const token = req.headers.authorization;

            const request = await agentRequestService.acceptRequest(id, agentId, token);

            res.json({
                success: true,
                message: 'Request accepted successfully',
                data: request
            });
        } catch (error) {
            next(error);
        }
    }

    async gradeRequest(req, res, next) {
        try {
            const { error } = gradeRequestSchema.validate(req.body);
            if (error) {
                return res.status(400).json({
                    success: false,
                    message: error.details[0].message
                });
            }

            const { id } = req.params;
            const { id: agentId } = req.user;

            const grading = await agentRequestService.gradeRequest(id, agentId, req.body);

            res.json({
                success: true,
                message: 'Grading details saved successfully',
                data: grading
            });
        } catch (error) {
            next(error);
        }
    }

    async closeRequest(req, res, next) {
        try {
            const { id } = req.params;
            const { id: agentId } = req.user;

            const request = await agentRequestService.closeRequest(id, agentId);

            res.json({
                success: true,
                message: 'Request completed successfully. SMS sent to trader.',
                data: request
            });
        } catch (error) {
            next(error);
        }
    }

    async getAllRequests(req, res, next) {
        try {
            const { status, agentId } = req.query;
            const token = req.headers.authorization;
            const requests = await agentRequestService.getAllRequests({ status, agentId }, token);

            res.json({
                success: true,
                data: requests
            });
        } catch (error) {
            next(error);
        }
    }

    async getStatistics(req, res, next) {
        try {
            const stats = await agentRequestService.getStatistics();

            res.json({
                success: true,
                data: stats
            });
        } catch (error) {
            next(error);
        }
    }
}

module.exports = new AgentRequestController();