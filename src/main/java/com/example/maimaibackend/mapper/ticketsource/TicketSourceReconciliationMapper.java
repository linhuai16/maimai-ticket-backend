package com.example.maimaibackend.mapper.ticketsource;

import com.example.maimaibackend.ticketsource.reconcile.model.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TicketSourceReconciliationMapper {
    int insertBatch(TicketSourceReconciliationBatch batch);
    List<TicketSourceReconciliationCandidate> selectCandidates(@Param("providerId") Long providerId,
                                                                @Param("orderIds") List<Long> orderIds);
    int insertDetail(TicketSourceReconciliationDetail detail);
    int finishBatch(@Param("batchId") Long batchId,
                    @Param("batchStatus") String batchStatus,
                    @Param("totalCount") Integer totalCount,
                    @Param("matchedCount") Integer matchedCount,
                    @Param("differenceCount") Integer differenceCount,
                    @Param("errorCount") Integer errorCount,
                    @Param("remark") String remark);
    TicketSourceReconciliationBatch selectBatch(@Param("batchId") Long batchId);
    List<TicketSourceReconciliationDetail> selectDetails(@Param("batchId") Long batchId);
}
