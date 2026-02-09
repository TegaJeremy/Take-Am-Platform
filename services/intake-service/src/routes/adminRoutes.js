const express = require('express');
const router = express.Router();
const agentRequestController = require('../controllers/agentRequestController');
const { authenticate, authorizeRoles } = require('../middlewares/authMiddleware');

// All routes require authentication and ADMIN role
router.use(authenticate);
router.use(authorizeRoles('ADMIN'));

// Get all requests (with optional filters)
router.get(
    '/requests',
    (req, res, next) => agentRequestController.getAllRequests(req, res, next)
);

// Get statistics
router.get(
    '/stats',
    (req, res, next) => agentRequestController.getStatistics(req, res, next)
);

module.exports = router;