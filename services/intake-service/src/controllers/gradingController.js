const Grading = require('../models/Grading');
const pricingService = require('../services/pricingService');
const { lookupTraderByPhone } = require('../utils/userServiceClient');
const smsService = require('../services/smsService');


exports.submitGrading = async (req, res, next) => {
    try {
        const agentId = req.user.id;
        const agentPhone = req.user.phoneNumber;
        const agentName = req.user.fullName;

        const {
            traderPhone,
            gradeA,
            gradeB,
            gradeC,
            gradeD,
            agentNotes
        } = req.body;

        // Validation: trader phone required
        if (!traderPhone) {
            return res.status(400).json({
                success: false,
                message: 'Trader phone number is required'
            });
        }

        // Validation: at least one grade must have weight
        const hasGrades = gradeA > 0 || gradeB > 0 || gradeC > 0 || gradeD > 0;
        if (!hasGrades) {
            return res.status(400).json({
                success: false,
                message: 'At least one grade (A, B, C, or D) must have a weight'
            });
        }

        console.log(`🔍 Agent ${agentName} submitting grading for trader: ${traderPhone}`);

        // Step 1: Look up trader in User Service
        const authToken = req.headers.authorization?.split(' ')[1];
        let traderData;

        try {
            traderData = await lookupTraderByPhone(traderPhone, authToken);
        } catch (error) {
            console.error('❌ Trader lookup failed:', error.message);
            return res.status(404).json({
                success: false,
                message: error.message || 'Trader not found. Please verify the phone number.',
                phone: traderPhone
            });
        }

        console.log(`✅ Trader found: ${traderData.fullName}`);

        // Step 2: Calculate payment using pricing service
        const paymentCalculation = pricingService.calculatePayment({
            gradeA: parseFloat(gradeA || 0),
            gradeB: parseFloat(gradeB || 0),
            gradeC: parseFloat(gradeC || 0),
            gradeD: parseFloat(gradeD || 0)
        });

        console.log('💰 Payment calculated:', {
            totalWeight: paymentCalculation.totalWeight,
            totalAmount: paymentCalculation.totalAmount
        });

        // Step 3: Create grading record (permanently ties agent to trader)
        const grading = await Grading.create({
            // Trader info
            traderId: traderData.id,
            traderPhone: traderData.phoneNumber,
            traderName: traderData.fullName,
            traderBankAccount: traderData.bankAccountNumber,
            traderBankName: traderData.bankName,

            // Agent info (permanent tie)
            agentId,
            agentName,
            agentPhone,

            // Grades
            gradeAWeight: paymentCalculation.gradeA.weight,
            gradeBWeight: paymentCalculation.gradeB.weight,
            gradeCWeight: paymentCalculation.gradeC.weight,
            gradeDWeight: paymentCalculation.gradeD.weight,

            // Amounts
            gradeAAmount: paymentCalculation.gradeA.amount,
            gradeBAmount: paymentCalculation.gradeB.amount,
            gradeCAmount: paymentCalculation.gradeC.amount,
            gradeDAmount: paymentCalculation.gradeD.amount,

            // Totals
            totalWeight: paymentCalculation.totalWeight,
            totalAmount: paymentCalculation.totalAmount,
            baseReferencePrice: paymentCalculation.baseReferencePrice,

            // Notes
            agentNotes: agentNotes || null,

            // Status
            paymentStatus: 'PENDING'
        });

        console.log(`✅ Grading saved with ID: ${grading.id}`);

        // Step 4: Send SMS to trader
        await smsService.sendGradingSMS(traderData.phoneNumber, {
            totalAmount: paymentCalculation.totalAmount,
            gradeA: paymentCalculation.gradeA,
            gradeB: paymentCalculation.gradeB,
            gradeC: paymentCalculation.gradeC,
            gradeD: paymentCalculation.gradeD
        });

        // Step 5: Return success response
        return res.status(201).json({
            success: true,
            message: 'Grading completed successfully. Trader notified via SMS.',
            data: {
                gradingId: grading.id,
                trader: {
                    name: traderData.fullName,
                    phone: traderData.phoneNumber,
                    bankAccount: traderData.bankAccountNumber,
                    bankName: traderData.bankName
                },
                agent: {
                    name: agentName,
                    phone: agentPhone
                },
                grades: {
                    gradeA: {
                        weight: paymentCalculation.gradeA.weight,
                        amount: paymentCalculation.gradeA.amount
                    },
                    gradeB: {
                        weight: paymentCalculation.gradeB.weight,
                        amount: paymentCalculation.gradeB.amount
                    },
                    gradeC: {
                        weight: paymentCalculation.gradeC.weight,
                        amount: paymentCalculation.gradeC.amount
                    },
                    gradeD: {
                        weight: paymentCalculation.gradeD.weight,
                        amount: paymentCalculation.gradeD.amount,
                        note: 'Deferred payment'
                    }
                },
                totalWeight: paymentCalculation.totalWeight,
                totalAmount: paymentCalculation.totalAmount,
                paymentStatus: 'PENDING',
                gradedAt: grading.gradedAt,
                smsSent: true
            }
        });

    } catch (error) {
        console.error('❌ Grading submission failed:', error);
        next(error);
    }
};


exports.getAgentGradings = async (req, res, next) => {
    try {
        const agentId = req.user.id;
        const { status, limit = 50, offset = 0 } = req.query;

        console.log(`🔍 Fetching gradings for agent: ${agentId}`);

        const where = { agentId };
        if (status) {
            where.paymentStatus = status;
        }

        const gradings = await Grading.findAndCountAll({
            where,
            order: [['gradedAt', 'DESC']],
            limit: parseInt(limit),
            offset: parseInt(offset)
        });

        return res.status(200).json({
            success: true,
            message: 'Agent gradings retrieved successfully',
            data: {
                total: gradings.count,
                gradings: gradings.rows,
                pagination: {
                    limit: parseInt(limit),
                    offset: parseInt(offset),
                    hasMore: gradings.count > (parseInt(offset) + parseInt(limit))
                }
            }
        });

    } catch (error) {
        console.error('❌ Get agent gradings failed:', error);
        next(error);
    }
};

/**
 * Get grading by ID
 * GET /api/v1/gradings/:id
 */
exports.getGradingById = async (req, res, next) => {
    try {
        const { id } = req.params;

        console.log(`🔍 Fetching grading: ${id}`);

        const grading = await Grading.findByPk(id);

        if (!grading) {
            return res.status(404).json({
                success: false,
                message: 'Grading not found'
            });
        }

        return res.status(200).json({
            success: true,
            data: grading
        });

    } catch (error) {
        console.error('❌ Get grading by ID failed:', error);
        next(error);
    }
};

/**
 * Get all pending payments (admin dashboard)
 * GET /api/v1/gradings/admin/pending-payments
 */
exports.getPendingPayments = async (req, res, next) => {
    try {
        console.log('🔍 Fetching all pending payments for admin');

        const pendingGradings = await Grading.findAll({
            where: { paymentStatus: 'PENDING' },
            order: [['gradedAt', 'ASC']], // Oldest first
            attributes: [
                'id',
                'traderId',
                'traderName',
                'traderPhone',
                'traderBankAccount',
                'traderBankName',
                'agentId',
                'agentName',
                'agentPhone',
                'totalWeight',
                'totalAmount',
                'gradedAt',
                'paymentStatus'
            ]
        });

        // Calculate total pending amount
        const totalPendingAmount = pendingGradings.reduce(
            (sum, grading) => sum + parseFloat(grading.totalAmount),
            0
        );

        return res.status(200).json({
            success: true,
            message: 'Pending payments retrieved successfully',
            data: {
                totalPendingAmount,
                totalPendingCount: pendingGradings.length,
                pendingPayments: pendingGradings
            }
        });

    } catch (error) {
        console.error('❌ Get pending payments failed:', error);
        next(error);
    }
};

/**
 * Get all gradings with filters (admin)
 * GET /api/v1/gradings/admin/all?status=PENDING&agentId=xxx&traderId=xxx
 */
exports.getAllGradings = async (req, res, next) => {
    try {
        const { status, agentId, traderId, limit = 100, offset = 0 } = req.query;

        console.log('🔍 Fetching all gradings with filters:', { status, agentId, traderId });

        const where = {};
        if (status) where.paymentStatus = status;
        if (agentId) where.agentId = agentId;
        if (traderId) where.traderId = traderId;

        const gradings = await Grading.findAndCountAll({
            where,
            order: [['gradedAt', 'DESC']],
            limit: parseInt(limit),
            offset: parseInt(offset)
        });

        return res.status(200).json({
            success: true,
            message: 'All gradings retrieved successfully',
            data: {
                total: gradings.count,
                gradings: gradings.rows,
                pagination: {
                    limit: parseInt(limit),
                    offset: parseInt(offset),
                    hasMore: gradings.count > (parseInt(offset) + parseInt(limit))
                }
            }
        });

    } catch (error) {
        console.error('❌ Get all gradings failed:', error);
        next(error);
    }
};

/**
 * Mark payment as paid (admin only)
 * PUT /api/v1/gradings/:id/mark-paid
 */
exports.markAsPaid = async (req, res, next) => {
    try {
        const { id } = req.params;

        console.log(`💰 Marking grading ${id} as PAID`);

        const grading = await Grading.findByPk(id);

        if (!grading) {
            return res.status(404).json({
                success: false,
                message: 'Grading not found'
            });
        }

        if (grading.paymentStatus === 'PAID') {
            return res.status(400).json({
                success: false,
                message: 'Payment already marked as paid'
            });
        }

        // Update payment status
        grading.paymentStatus = 'PAID';
        grading.paidAt = new Date();
        await grading.save();

        console.log(`✅ Grading ${id} marked as PAID`);

        // Send SMS confirmation to trader
        await smsService.sendPaymentConfirmationSMS(grading.traderPhone, {
            amount: grading.totalAmount,
            bankAccount: grading.traderBankAccount,
            bankName: grading.traderBankName
        });

        return res.status(200).json({
            success: true,
            message: 'Payment marked as paid successfully',
            data: {
                id: grading.id,
                traderName: grading.traderName,
                totalAmount: grading.totalAmount,
                paymentStatus: grading.paymentStatus,
                paidAt: grading.paidAt
            }
        });

    } catch (error) {
        console.error('❌ Mark as paid failed:', error);
        next(error);
    }
};
