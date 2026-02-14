const traderRequestService = require('../services/traderRequestService');

class TraderRequestController {
    async createRequest(req, res, next) {
        try {
            const { id: traderId, phoneNumber, role, name } = req.user;
            const token = req.headers.authorization;

            // Verify user is a trader
            if (role !== 'TRADER') {
                return res.status(403).json({
                    success: false,
                    message: 'Only traders can create requests'
                });
            }

            const request = await traderRequestService.createRequest(
                traderId,
                phoneNumber,
                name || 'Trader',
                token
            );

            res.status(201).json({
                success: true,
                message: 'Request created successfully. An agent will contact you soon.',
                data: request
            });
        } catch (error) {
            next(error);
        }
    }

    async getMyRequests(req, res, next) {
        try {
            const { id: traderId } = req.user;
            const token = req.headers.authorization;

            const result = await traderRequestService.getTraderRequests(traderId, token);

            res.json({
                success: true,
                data: result
            });
        } catch (error) {
            next(error);
        }
    }

    async getRequestById(req, res, next) {
        try {
            const { id } = req.params;
            const token = req.headers.authorization;

            const request = await traderRequestService.getRequestById(id, token);

            if (!request) {
                return res.status(404).json({
                    success: false,
                    message: 'Request not found'
                });
            }

            // Verify request belongs to this trader
            if (request.traderId !== req.user.id) {
                return res.status(403).json({
                    success: false,
                    message: 'Access denied'
                });
            }

            res.json({
                success: true,
                data: request
            });
        } catch (error) {
            next(error);
        }
    }
}

// Export an instance, not the class
module.exports = new TraderRequestController();