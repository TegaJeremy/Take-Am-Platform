/**
 * Pricing Service - Calculates payment based on grades
 *
 * Grade System:
 * - Grade A: Fresh (1.0× multiplier) - Full value
 * - Grade B: Soft (0.6× multiplier) - Processing value
 * - Grade C: Feed/Compost (0.2× multiplier) - Feed value
 * - Grade D: Unripe (0.0× multiplier) - Not paid immediately
 */

class PricingService {
    constructor() {
        // Base Reference Price - set by admin (we'll hardcode for now)
        this.baseReferencePrice = 100; // ₦100/kg

        // Grade multipliers (locked system)
        this.gradeMultipliers = {
            A: 1.0,  // Fresh - Full value
            B: 0.6,  // Soft - 60% value
            C: 0.2,  // Feed - 20% value
            D: 0.0   // Unripe - 0% (deferred payment)
        };
    }

    /**
     * Calculate payment for graded goods
     * @param {Object} grades - { gradeA: 50, gradeB: 30, gradeC: 10, gradeD: 20 }
     * @returns {Object} - Payment breakdown
     */
    calculatePayment(grades) {
        const { gradeA = 0, gradeB = 0, gradeC = 0, gradeD = 0 } = grades;

        // Validate: at least one grade must have weight
        const totalWeight = gradeA + gradeB + gradeC + gradeD;
        if (totalWeight === 0) {
            throw new Error('At least one grade must have a weight');
        }

        // Calculate amounts per grade
        const gradeAAmount = gradeA * this.baseReferencePrice * this.gradeMultipliers.A;
        const gradeBAmount = gradeB * this.baseReferencePrice * this.gradeMultipliers.B;
        const gradeCAmount = gradeC * this.baseReferencePrice * this.gradeMultipliers.C;
        const gradeDAmount = gradeD * this.baseReferencePrice * this.gradeMultipliers.D;

        // Total paid (excludes Grade D)
        const totalAmount = gradeAAmount + gradeBAmount + gradeCAmount;

        return {
            gradeA: {
                weight: gradeA,
                amount: gradeAAmount,
                multiplier: this.gradeMultipliers.A
            },
            gradeB: {
                weight: gradeB,
                amount: gradeBAmount,
                multiplier: this.gradeMultipliers.B
            },
            gradeC: {
                weight: gradeC,
                amount: gradeCAmount,
                multiplier: this.gradeMultipliers.C
            },
            gradeD: {
                weight: gradeD,
                amount: gradeDAmount,
                multiplier: this.gradeMultipliers.D,
                note: 'Deferred payment - Unripe goods'
            },
            totalWeight,
            totalAmount,
            baseReferencePrice: this.baseReferencePrice,
            calculatedAt: new Date().toISOString()
        };
    }

    /**
     * Get current BRP (for admin dashboard)
     */
    getBaseReferencePrice() {
        return this.baseReferencePrice;
    }

    /**
     * Update BRP (admin only - we'll implement later)
     */
    updateBaseReferencePrice(newPrice) {
        if (newPrice <= 0) {
            throw new Error('Base Reference Price must be positive');
        }
        this.baseReferencePrice = newPrice;
        return this.baseReferencePrice;
    }

    /**
     * Get grade multipliers (for transparency)
     */
    getGradeMultipliers() {
        return this.gradeMultipliers;
    }
}

module.exports = new PricingService();