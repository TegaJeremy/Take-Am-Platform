const express = require('express');
const router = express.Router();
const agentRequestController = require('../controllers/agentRequestController');
const { authenticate, authorizeRoles } = require('../middlewares/authMiddleware');

// All routes require authentication
router.use(authenticate);

// Agent views all pending requests
router.get(
    '/pending',
    authorizeRoles('AGENT', 'ADMIN'),
    (req, res, next) => agentRequestController.getPendingRequests(req, res, next)
);

// Agent gets their current active request
router.get(
    '/my-current',
    authorizeRoles('AGENT'),
    (req, res, next) => agentRequestController.getMyCurrentRequest(req, res, next)
);

// Agent accepts a request
router.post(
    '/:id/accept',
    authorizeRoles('AGENT'),
    (req, res, next) => agentRequestController.acceptRequest(req, res, next)
);


router.post(
    '/:id/grade',
    authorizeRoles('AGENT'),
    (req, res, next) => agentRequestController.gradeRequest(req, res, next)
);

// Agent closes the request (sends SMS)
router.post(
    '/:id/close',
    authorizeRoles('AGENT'),
    (req, res, next) => agentRequestController.closeRequest(req, res, next)
);

module.exports = router;