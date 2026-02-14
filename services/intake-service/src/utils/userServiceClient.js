const axios = require('axios');

const USER_SERVICE_URL = process.env.USER_SERVICE_URL || 'http://localhost:8081';

const checkAgentClockedIn = async (agentId, token) => {
    try {
        const response = await axios.get(
            `${USER_SERVICE_URL}/api/v1/agents/attendance/is-clocked-in`,
            {
                headers: {
                    Authorization: token  //  token already has "Bearer "
                }
            }
        );
        return response.data.clockedIn === true;
    } catch (error) {
        console.error('Error checking clock-in status:', error.message);
        throw new Error('Failed to verify agent clock-in status');
    }
};

const getAgentDetails = async (agentId, token) => {
    try {
        const response = await axios.get(
            `${USER_SERVICE_URL}/api/v1/agents/${agentId}`,
            {
                headers: {
                    Authorization: token  //  token already has "Bearer "
                }
            }
        );
        return response.data;
    } catch (error) {
        console.error('Error fetching agent details:', error.message);
        return null;
    }
};

const getTraderDetails = async (traderId, token) => {
    try {
        console.log('🔍 Fetching trader details for ID:', traderId);
        console.log('🔑 Using token:', token ? token.substring(0, 20) + '...' : 'NO TOKEN');

        const response = await axios.get(
            `${USER_SERVICE_URL}/api/v1/traders/${traderId}`,
            {
                headers: {
                    Authorization: token  // token already has "Bearer "
                }
            }
        );

        console.log('Trader details fetched successfully');
        return response.data;
    } catch (error) {
        console.error(' Error fetching trader details:', error.response?.status, error.response?.data || error.message);
        return null;
    }
};



async function lookupTraderByPhone(phoneNumber, authToken) {
    try {
        console.log(`🔍 Looking up trader: ${phoneNumber} from User Service`);

        const response = await axios.get(
            `${USER_SERVICE_URL}/api/v1/users/phone/${phoneNumber}`,
            {
                headers: {
                    'Authorization': `Bearer ${authToken}`
                }
            }
        );

        console.log(`✅ Trader found: ${response.data.data.fullName}`);
        return response.data.data;

    } catch (error) {
        console.error(' User Service lookup failed:', error.response?.data || error.message);

        if (error.response?.status === 404) {
            throw new Error(`Trader not found with phone number: ${phoneNumber}`);
        }

        throw new Error('Failed to lookup trader from User Service');
    }
}

module.exports = {
    checkAgentClockedIn,
    getAgentDetails,
    getTraderDetails,
    lookupTraderByPhone
};