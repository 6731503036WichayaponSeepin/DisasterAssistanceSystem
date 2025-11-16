// th/mfu/repository/caserepo/AssistanceCaseRepository.java
package th.mfu.repository.caserepo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import th.mfu.model.caseentity.AssistanceCase;
import th.mfu.model.caseentity.CaseStatus;

public interface AssistanceCaseRepository extends JpaRepository<AssistanceCase, Long> {
      boolean existsByReporterUserIdAndStatusIn(Long reporterUserId, List<CaseStatus> statuses);

    Optional<AssistanceCase> findFirstByReporterUserIdAndStatusInOrderByCreatedAtDesc(
            Long reporterUserId,
            List<CaseStatus> statuses
    );
    List<AssistanceCase> findByStatus(CaseStatus status);
    List<AssistanceCase> findByReporterUserId(Long userId);

    List<AssistanceCase> findByAssignedRescueTeamId(Long teamId);
    List<AssistanceCase> findByAssignedRescueTeamIdAndStatus(Long teamId, CaseStatus status);

    List<AssistanceCase> findByAssignedRescueTeamIdIsNull();
    List<AssistanceCase> findByAssignedRescueTeamIdIsNullAndStatus(CaseStatus status);
    
}
