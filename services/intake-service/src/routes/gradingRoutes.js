const express = require('express');
const router = express.Router();
const gradingController = require('../controllers/gradingController');
const { authenticate } = require('../middlewares/authMiddleware');


router.use(authenticate);


router.post('/', gradingController.submitGrading);


router.get('/agent/my-gradings', gradingController.getAgentGradings);


router.get('/:id', gradingController.getGradingById);


router.get('/admin/pending-payments', gradingController.getPendingPayments);


router.get('/admin/all', gradingController.getAllGradings);


router.put('/:id/mark-paid', gradingController.markAsPaid);

module.exports = router;