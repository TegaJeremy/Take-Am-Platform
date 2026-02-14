const Joi = require('joi');

const createRequestSchema = Joi.object({
    traderAddress: Joi.string().optional()
});

const gradeRequestSchema = Joi.object({
    ripeWeight: Joi.number().min(0).required(),
    unripeWeight: Joi.number().min(0).required(),
    semiSpoiledWeight: Joi.number().min(0).required(),
    spoiledWeight: Joi.number().min(0).required(),
    agentNotes: Joi.string().optional()
});

module.exports = {
    createRequestSchema,
    gradeRequestSchema
};