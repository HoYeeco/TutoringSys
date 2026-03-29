package com.tutoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedisMonitorVO {

    private String version;
    private String mode;
    private Long uptimeInSeconds;
    private Long connectedClients;
    private Long totalConnectionsReceived;
    private Long totalCommandsProcessed;
    private Long keyspaceHits;
    private Long keyspaceMisses;
    private Double hitRate;
    private String usedMemory;
    private String usedMemoryPeak;
    private String usedMemoryHuman;
    private Long dbSize;
}
