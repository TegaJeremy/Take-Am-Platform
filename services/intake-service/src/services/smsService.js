const axios = require('axios');

const SMS_API_KEY = process.env.SMS_API_KEY;
const SMS_SENDER_ID = process.env.SMS_SENDER_ID || 'TakeAm';
const SMS_API_URL = process.env.SMS_API_URL || 'https://api.ng.termii.com/api/sms/send';

async function sendSMS(phoneNumber, message) {
    // If no API key, just log and return success
    if (!SMS_API_KEY) {
        console.log('⚠️ SMS_API_KEY not set. SMS not sent (dev mode)');
        console.log(`📱 SMS would be sent to ${phoneNumber}:`);
        console.log(message);
        return { success: true, message: 'SMS skipped (no API key)' };
    }

    try {
        const response = await axios.post(SMS_API_URL, {
            to: phoneNumber,
            from: SMS_SENDER_ID,
            sms: message,
            type: 'plain',
            channel: 'generic',
            api_key: SMS_API_KEY
        });

        console.log('✅ SMS sent successfully:', response.data);
        return { success: true, data: response.data };

    } catch (error) {
        console.error('❌ SMS sending failed:', error.response?.data || error.message);
        // Don't throw error - we don't want SMS failure to break grading
        return { success: false, error: error.message };
    }
}


async function sendGradingSMS(phoneNumber, gradingData) {
    const { totalAmount, gradeA, gradeB, gradeC, gradeD } = gradingData;

    let message = `TakeAm: Your goods have been graded!\nTotal: ₦${totalAmount.toLocaleString()}`;

    if (gradeA.weight > 0) {
        message += `\nGrade A (Fresh): ${gradeA.weight}kg - ₦${gradeA.amount.toLocaleString()}`;
    }
    if (gradeB.weight > 0) {
        message += `\nGrade B (Soft): ${gradeB.weight}kg - ₦${gradeB.amount.toLocaleString()}`;
    }
    if (gradeC.weight > 0) {
        message += `\nGrade C (Feed): ${gradeC.weight}kg - ₦${gradeC.amount.toLocaleString()}`;
    }
    if (gradeD.weight > 0) {
        message += `\nGrade D (Unripe): ${gradeD.weight}kg - Deferred`;
    }

    message += `\n\nPayment within 3 days. Thank you!`;

    return await sendSMS(phoneNumber, message);
}


async function sendPaymentConfirmationSMS(phoneNumber, paymentData) {
    const { amount, bankAccount, bankName } = paymentData;

    const message = `TakeAm: Payment of ₦${parseFloat(amount).toLocaleString()} has been sent to your account ${bankAccount || 'on file'} (${bankName || 'your bank'}). Thank you for using TakeAm!`;

    return await sendSMS(phoneNumber, message);
}


async function sendOTPSMS(phoneNumber, otp) {
    const message = `Your TakeAm verification code is: ${otp}. Valid for 5 minutes. Do not share this code.`;
    return await sendSMS(phoneNumber, message);
}

module.exports = {
    sendSMS,
    sendGradingSMS,
    sendPaymentConfirmationSMS,
    sendOTPSMS
};