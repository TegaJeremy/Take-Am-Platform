const express = require('express');
const router = express.Router();
const traderRequestController = require('../controllers/traderRequestController');
const { authenticate, authorizeRoles } = require('../middlewares/authMiddleware');


router.use(authenticate);


router.post(
    '/',
    authorizeRoles('TRADER'),
    (req, res, next) => traderRequestController.createRequest(req, res, next)
);

// Get my requests
router.get(
    '/my',
    authorizeRoles('TRADER'),
    (req, res, next) => traderRequestController.getMyRequests(req, res, next)
);

// Get specific request details
router.get(
    '/:id',
    authorizeRoles('TRADER'),
    (req, res, next) => traderRequestController.getRequestById(req, res, next)
);

module.exports = router;