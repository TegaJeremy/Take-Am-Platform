package com.takeam.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {

    // User counts
    private Long totalUsers;
    private Long totalTraders;
    private Long totalAgents;
    private Long totalBuyers;
    private Long totalAdmins;

    // Agent stats
    private Long pendingAgents;
    private Long approvedAgents;
    private Long rejectedAgents;

    // User status
    private Long activeUsers;
    private Long suspendedUsers;
    private Long bannedUsers;

    // Time-based stats
    private Long todayRegistrations;
    private Long thisWeekRegistrations;
    private Long thisMonthRegistrations;
}