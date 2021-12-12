package io.harness.gitsync.gitsyncerror.impl;

import static io.harness.annotations.dev.HarnessTeam.PL;
import static io.harness.data.structure.CollectionUtils.emptyIfNull;
import static io.harness.ng.core.utils.NGUtils.validate;
import static io.harness.utils.PageUtils.getNGPageResponse;
import static io.harness.utils.PageUtils.getPageRequest;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.ROOT;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.skip;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.query.Update.update;

import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.Scope;
import io.harness.delegate.beans.git.YamlGitConfigDTO;
import io.harness.gitsync.common.service.YamlGitConfigService;
import io.harness.gitsync.gitsyncerror.GitSyncErrorStatus;
import io.harness.gitsync.gitsyncerror.beans.GitSyncError;
import io.harness.gitsync.gitsyncerror.beans.GitSyncError.GitSyncErrorKeys;
import io.harness.gitsync.gitsyncerror.beans.GitSyncErrorAggregateByCommit;
import io.harness.gitsync.gitsyncerror.beans.GitSyncErrorAggregateByCommit.GitSyncErrorAggregateByCommitKeys;
import io.harness.gitsync.gitsyncerror.beans.GitSyncErrorType;
import io.harness.gitsync.gitsyncerror.beans.GitToHarnessErrorDetails;
import io.harness.gitsync.gitsyncerror.dtos.GitSyncErrorAggregateByCommitDTO;
import io.harness.gitsync.gitsyncerror.dtos.GitSyncErrorCountDTO;
import io.harness.gitsync.gitsyncerror.dtos.GitSyncErrorDTO;
import io.harness.gitsync.gitsyncerror.remote.GitSyncErrorMapper;
import io.harness.gitsync.gitsyncerror.service.GitSyncErrorService;
import io.harness.ng.beans.PageRequest;
import io.harness.ng.beans.PageResponse;
import io.harness.repositories.gitSyncError.GitSyncErrorRepository;
import io.harness.utils.PageUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;

@Singleton
@Slf4j
@OwnedBy(PL)
public class GitSyncErrorServiceImpl implements GitSyncErrorService {
  private static final String IS_ACTIVE_ERROR = "isActiveError";
  private final YamlGitConfigService yamlGitConfigService;
  private final GitSyncErrorRepository gitSyncErrorRepository;
  private final ScheduledExecutorService executorService;
  public static final String ERROR_DOCUMENT = "errorDocument";
  public static final Long DEFAULT_COMMIT_TIME = 0L;

  @Inject
  public GitSyncErrorServiceImpl(
      YamlGitConfigService yamlGitConfigService, GitSyncErrorRepository gitSyncErrorRepository) {
    this.yamlGitConfigService = yamlGitConfigService;
    this.gitSyncErrorRepository = gitSyncErrorRepository;
    this.executorService = Executors.newScheduledThreadPool(1);
    executorService.scheduleWithFixedDelay(this::markExpiredErrors, 1, 24, TimeUnit.HOURS);
  }

  @Override
  public PageResponse<GitSyncErrorAggregateByCommitDTO> listGitToHarnessErrorsGroupedByCommits(PageRequest pageRequest,
      String accountIdentifier, String orgIdentifier, String projectIdentifier, String searchTerm, String repoId,
      String branch, Integer numberOfErrorsInSummary) {
    Pageable pageable = getPageRequest(pageRequest);
    Criteria criteria = createGitToHarnessErrorFilterCriteria(
        accountIdentifier, orgIdentifier, projectIdentifier, searchTerm, repoId, branch);
    criteria.and(GitSyncErrorKeys.status).in(GitSyncErrorStatus.ACTIVE, GitSyncErrorStatus.RESOLVED);
    SortOperation sortOperation = sort(Sort.Direction.DESC, GitSyncErrorAggregateByCommitKeys.createdAt);

    Aggregation aggregation = newAggregation(match(criteria), getProjectionOperationForProjectingActiveError(),
        getGroupOperationForGroupingErrorsWithCommitId(),
        getProjectionOperationForProjectingGitSyncErrorAggregateByCommitKeys(numberOfErrorsInSummary), sortOperation,
        skip(pageable.getOffset()), limit(pageable.getPageSize()));
    List<GitSyncErrorAggregateByCommit> gitSyncErrorAggregateByCommitList =
        gitSyncErrorRepository.aggregate(aggregation, GitSyncErrorAggregateByCommit.class).getMappedResults();
    long totalCount = gitSyncErrorRepository.count(criteria);
    List<GitSyncErrorAggregateByCommitDTO> gitSyncErrorAggregateByCommitDTOList =
        emptyIfNull(gitSyncErrorAggregateByCommitList)
            .stream()
            .map(GitSyncErrorMapper::toGitSyncErrorAggregateByCommitDTO)
            .collect(toList());
    Set<String> repoUrls = emptyIfNull(gitSyncErrorAggregateByCommitDTOList)
                               .stream()
                               .map(gitSyncErrorAggregateByCommitDTO
                                   -> gitSyncErrorAggregateByCommitDTO.getErrorsForSummaryView().get(0).getRepoUrl())
                               .collect(Collectors.toSet());
    Map<String, String> repoIds = getRepoIds(repoUrls, accountIdentifier, orgIdentifier, projectIdentifier);
    gitSyncErrorAggregateByCommitDTOList.forEach(gitSyncErrorAggregateByCommitDTO -> {
      String repoUrl = gitSyncErrorAggregateByCommitDTO.getErrorsForSummaryView().get(0).getRepoUrl();
      gitSyncErrorAggregateByCommitDTO.setRepoId(repoIds.get(repoUrl));
    });
    Page<GitSyncErrorAggregateByCommitDTO> page =
        new PageImpl<>(gitSyncErrorAggregateByCommitDTOList, pageable, totalCount);
    return getNGPageResponse(page);
  }

  private ProjectionOperation getProjectionOperationForProjectingActiveError() {
    Criteria activeErrorCriteria = Criteria.where(GitSyncErrorKeys.status).is(GitSyncErrorStatus.ACTIVE);
    return Aggregation.project()
        .and(ConditionalOperators.Cond.when(activeErrorCriteria).then(1).otherwise(0))
        .as(IS_ACTIVE_ERROR)
        .andExpression(ROOT)
        .as(ERROR_DOCUMENT)
        .andExpression(GitSyncErrorKeys.gitCommitId)
        .as(GitSyncErrorKeys.gitCommitId)
        .andExpression(GitSyncErrorKeys.commitMessage)
        .as(GitSyncErrorKeys.commitMessage)
        .andInclude(GitSyncErrorKeys.branchName)
        .andInclude(GitSyncErrorKeys.createdAt);
  }

  private GroupOperation getGroupOperationForGroupingErrorsWithCommitId() {
    return group(GitSyncErrorKeys.gitCommitId)
        .sum(IS_ACTIVE_ERROR)
        .as(GitSyncErrorAggregateByCommitKeys.failedCount)
        .first(GitSyncErrorKeys.gitCommitId)
        .as(GitSyncErrorAggregateByCommitKeys.gitCommitId)
        .first(GitSyncErrorKeys.commitMessage)
        .as(GitSyncErrorAggregateByCommitKeys.commitMessage)
        .first(GitSyncErrorKeys.branchName)
        .as(GitSyncErrorAggregateByCommitKeys.branchName)
        .first(GitSyncErrorKeys.createdAt)
        .as(GitSyncErrorAggregateByCommitKeys.createdAt)
        .push(ERROR_DOCUMENT)
        .as(GitSyncErrorAggregateByCommitKeys.errorsForSummaryView);
  }

  private ProjectionOperation getProjectionOperationForProjectingGitSyncErrorAggregateByCommitKeys(
      Integer numberOfErrorsInSummary) {
    return project()
        .andInclude(GitSyncErrorAggregateByCommitKeys.gitCommitId)
        .andInclude(GitSyncErrorAggregateByCommitKeys.createdAt)
        .andInclude(GitSyncErrorAggregateByCommitKeys.commitMessage)
        .andInclude(GitSyncErrorAggregateByCommitKeys.branchName)
        .andInclude(GitSyncErrorAggregateByCommitKeys.failedCount)
        .andExpression(GitSyncErrorAggregateByCommitKeys.errorsForSummaryView)
        .slice(numberOfErrorsInSummary)
        .as(GitSyncErrorAggregateByCommitKeys.errorsForSummaryView);
    //
  }

  private Map<String, String> getRepoIds(Set<String> repoUrls, String accountId, String orgId, String projectId) {
    return repoUrls.stream().collect(Collectors.toMap(repoUrl
        -> repoUrl,
        repoUrl -> yamlGitConfigService.getByProjectIdAndRepo(accountId, orgId, projectId, repoUrl).getIdentifier()));
  }

  @Override
  public PageResponse<GitSyncErrorDTO> listAllGitToHarnessErrors(PageRequest pageRequest, String accountId,
      String orgIdentifier, String projectIdentifier, String searchTerm, String repoId, String branch) {
    Criteria criteria =
        createGitToHarnessErrorFilterCriteria(accountId, orgIdentifier, projectIdentifier, searchTerm, repoId, branch);
    criteria.and(GitSyncErrorKeys.status).is(GitSyncErrorStatus.ACTIVE);
    Page<GitSyncErrorDTO> gitSyncErrorPage =
        gitSyncErrorRepository.findAll(criteria, PageUtils.getPageRequest(pageRequest))
            .map(GitSyncErrorMapper::toGitSyncErrorDTO);

    Set<String> repoUrls = new HashSet<>();
    gitSyncErrorPage.forEach(gitSyncErrorDTO -> { repoUrls.add(gitSyncErrorDTO.getRepoUrl()); });
    Map<String, String> repoIds = getRepoIds(repoUrls, accountId, orgIdentifier, projectIdentifier);
    gitSyncErrorPage.forEach(
        gitSyncErrorDTO -> { gitSyncErrorDTO.setRepoId(repoIds.get(gitSyncErrorDTO.getRepoUrl())); });
    return getNGPageResponse(gitSyncErrorPage);
  }

  private Criteria createGitToHarnessErrorFilterCriteria(String accountIdentifier, String orgIdentifier,
      String projectIdentifier, String searchTerm, String repoId, String branch) {
    // when no filter is chosen - take all repos and their default branches
    Criteria criteria = Criteria.where(GitSyncErrorKeys.accountIdentifier)
                            .is(accountIdentifier)
                            .and(GitSyncErrorKeys.scopes)
                            .is(Scope.of(accountIdentifier, orgIdentifier, projectIdentifier))
                            .and(GitSyncErrorKeys.errorType)
                            .is(GitSyncErrorType.GIT_TO_HARNESS);
    Criteria repoBranchCriteria =
        getRepoBranchCriteria(accountIdentifier, orgIdentifier, projectIdentifier, repoId, branch);
    criteria.andOperator(repoBranchCriteria)
        .and(GitSyncErrorKeys.createdAt)
        .gt(OffsetDateTime.now().minusDays(30).toInstant().toEpochMilli());
    if (isNotBlank(searchTerm)) {
      criteria.orOperator(Criteria.where(GitSyncErrorKeys.gitCommitId).regex(searchTerm, "i"),
          Criteria.where(GitSyncErrorKeys.completeFilePath).regex(searchTerm, "i"),
          Criteria.where(GitSyncErrorKeys.entityType).regex(searchTerm, "i"));
    }
    return criteria;
  }

  private Criteria getRepoBranchCriteria(
      String accountIdentifier, String orgIdentifier, String projectIdentifier, String repoIdentifier, String branch) {
    if (StringUtils.isNotEmpty(repoIdentifier)) {
      YamlGitConfigDTO yamlGitConfigDTO =
          yamlGitConfigService.get(projectIdentifier, orgIdentifier, accountIdentifier, repoIdentifier);
      branch = StringUtils.isEmpty(branch) ? yamlGitConfigDTO.getBranch() : branch;
      return Criteria.where(GitSyncErrorKeys.repoUrl)
          .is(yamlGitConfigDTO.getRepo())
          .and(GitSyncErrorKeys.branchName)
          .is(branch);
    }
    return new Criteria();
  }

  @Override
  public PageResponse<GitSyncErrorDTO> listGitToHarnessErrorsForCommit(PageRequest pageRequest, String commitId,
      String accountIdentifier, String orgIdentifier, String projectIdentifier, String repoId, String branch) {
    String repoUrl = yamlGitConfigService.get(projectIdentifier, orgIdentifier, accountIdentifier, repoId).getRepo();
    Criteria criteria = Criteria.where(GitSyncErrorKeys.accountIdentifier)
                            .is(accountIdentifier)
                            .and(GitSyncErrorKeys.scopes)
                            .is(Scope.of(accountIdentifier, orgIdentifier, projectIdentifier))
                            .and(GitSyncErrorKeys.errorType)
                            .is(GitSyncErrorType.GIT_TO_HARNESS)
                            .and(GitSyncErrorKeys.repoUrl)
                            .is(repoUrl)
                            .and(GitSyncErrorKeys.branchName)
                            .is(branch)
                            .and(GitSyncErrorKeys.gitCommitId)
                            .is(commitId)
                            .and(GitSyncErrorKeys.status)
                            .in(GitSyncErrorStatus.ACTIVE, GitSyncErrorStatus.RESOLVED);
    Page<GitSyncError> gitSyncErrorPage =
        gitSyncErrorRepository.findAll(criteria, PageUtils.getPageRequest(pageRequest));
    return getNGPageResponse(gitSyncErrorPage.map(GitSyncErrorMapper::toGitSyncErrorDTO));
  }

  @Override
  public GitSyncErrorDTO save(GitSyncErrorDTO gitSyncErrorDTO) {
    return save(GitSyncErrorMapper.toGitSyncError(gitSyncErrorDTO, gitSyncErrorDTO.getAccountIdentifier()));
  }

  private GitSyncErrorDTO save(GitSyncError gitSyncError) {
    try {
      validate(gitSyncError);
      GitSyncError savedError = gitSyncErrorRepository.save(gitSyncError);
      return GitSyncErrorMapper.toGitSyncErrorDTO(savedError);
    } catch (DuplicateKeyException ex) {
      log.info("A git sync error for this commitId and File already exists.", ex);
      GitToHarnessErrorDetails additionalErrorDetails =
          (GitToHarnessErrorDetails) gitSyncError.getAdditionalErrorDetails();
      return getGitToHarnessError(gitSyncError.getAccountIdentifier(), additionalErrorDetails.getGitCommitId(),
          gitSyncError.getRepoUrl(), gitSyncError.getBranchName(), gitSyncError.getCompleteFilePath())
          .get();
    }
  }

  @Override
  public List<GitSyncErrorDTO> saveAll(List<GitSyncErrorDTO> gitSyncErrorDTOList) {
    List<GitSyncError> gitSyncErrors =
        gitSyncErrorDTOList.stream()
            .map(gitSyncErrorDTO
                -> GitSyncErrorMapper.toGitSyncError(gitSyncErrorDTO, gitSyncErrorDTO.getAccountIdentifier()))
            .collect(toList());
    List<GitSyncError> gitSyncErrorsSaved = new ArrayList<>();
    try {
      gitSyncErrorRepository.saveAll(gitSyncErrors).iterator().forEachRemaining(gitSyncErrorsSaved::add);
      return gitSyncErrorsSaved.stream().map(GitSyncErrorMapper::toGitSyncErrorDTO).collect(toList());
    } catch (DuplicateKeyException ex) {
      log.info("Git sync error already exist", ex);
      return null;
    }
  }

  @Override
  public void overrideGitToHarnessErrors(String accountId, String repoUrl, String branchName, Set<String> filePaths) {
    Criteria criteria = createActiveErrorsFilterCriteria(
        accountId, GitSyncErrorType.GIT_TO_HARNESS, repoUrl, branchName, new ArrayList<>(filePaths));
    Update update = update(GitSyncErrorKeys.status, GitSyncErrorStatus.OVERRIDDEN);
    gitSyncErrorRepository.updateError(criteria, update);
  }

  @Override
  public void resolveGitToHarnessErrors(
      String accountId, String repoUrl, String branchName, Set<String> filePaths, String commitId) {
    Criteria criteria = createActiveErrorsFilterCriteria(
        accountId, GitSyncErrorType.GIT_TO_HARNESS, repoUrl, branchName, new ArrayList<>(filePaths));
    Update update =
        update(GitSyncErrorKeys.status, GitSyncErrorStatus.RESOLVED).set(GitSyncErrorKeys.resolvedByCommitId, commitId);
    gitSyncErrorRepository.updateError(criteria, update);
  }

  private Criteria createActiveErrorsFilterCriteria(
      String accountId, GitSyncErrorType errorType, String repoUrl, String branchName, List<String> filePaths) {
    Criteria criteria = Criteria.where(GitSyncErrorKeys.accountIdentifier)
                            .is(accountId)
                            .and(GitSyncErrorKeys.errorType)
                            .is(errorType)
                            .and(GitSyncErrorKeys.repoUrl)
                            .is(repoUrl)
                            .and(GitSyncErrorKeys.branchName)
                            .is(branchName);
    if (errorType.equals(GitSyncErrorType.GIT_TO_HARNESS)) {
      criteria.and(GitSyncErrorKeys.completeFilePath).in(filePaths);
    }
    return criteria.and(GitSyncErrorKeys.status).is(GitSyncErrorStatus.ACTIVE);
  }

  @Override
  public Optional<GitSyncErrorDTO> getGitToHarnessError(
      String accountId, String commitId, String repoUrl, String branchName, String filePath) {
    Criteria criteria = Criteria.where(GitSyncErrorKeys.accountIdentifier)
                            .is(accountId)
                            .and(GitSyncErrorKeys.gitCommitId)
                            .is(commitId)
                            .and(GitSyncErrorKeys.repoUrl)
                            .is(repoUrl)
                            .and(GitSyncErrorKeys.branchName)
                            .is(branchName)
                            .and(GitSyncErrorKeys.completeFilePath)
                            .is(filePath);
    GitSyncError error = gitSyncErrorRepository.find(criteria);
    return Optional.ofNullable(GitSyncErrorMapper.toGitSyncErrorDTO(error));
  }

  private void markExpiredErrors() {
    Criteria criteria =
        Criteria.where(GitSyncErrorKeys.createdAt).lte(OffsetDateTime.now().minusDays(30).toInstant().toEpochMilli());
    Update update = update(GitSyncErrorKeys.status, GitSyncErrorStatus.EXPIRED);
    gitSyncErrorRepository.updateError(criteria, update);
  }

  @Override
  public boolean deleteGitSyncErrors(List<String> errorIds, String accountId) {
    return gitSyncErrorRepository.deleteByIds(errorIds).wasAcknowledged();
  }

  @Override
  public void recordConnectivityError(
      String accountIdentifier, List<Scope> scopes, String repoUrl, String branch, String errorMessage) {
    scopes.forEach(scope -> recordConnectivityErrorInternal(accountIdentifier, scope, repoUrl, branch, errorMessage));
  }

  private void recordConnectivityErrorInternal(
      String accountIdentifier, Scope scope, String repoUrl, String branch, String errorMessage) {
    Criteria criteria = Criteria.where(GitSyncErrorKeys.accountIdentifier)
                            .is(accountIdentifier)
                            .and(GitSyncErrorKeys.scopes)
                            .is(scope)
                            .and(GitSyncErrorKeys.errorType)
                            .is(GitSyncErrorType.CONNECTIVITY_ISSUE)
                            .and(GitSyncErrorKeys.repoUrl)
                            .is(repoUrl)
                            .and(GitSyncErrorKeys.branchName)
                            .is(branch);
    GitSyncError gitSyncError = gitSyncErrorRepository.find(criteria);
    if (gitSyncError == null) {
      GitSyncError error = GitSyncError.builder()
                               .accountIdentifier(accountIdentifier)
                               .errorType(GitSyncErrorType.CONNECTIVITY_ISSUE)
                               .repoUrl(repoUrl)
                               .branchName(branch)
                               .failureReason(errorMessage)
                               .status(GitSyncErrorStatus.ACTIVE)
                               .scopes(Collections.singletonList(scope))
                               .createdAt(System.currentTimeMillis())
                               .build();
      save(error);
    } else {
      Update update = update(GitSyncErrorKeys.failureReason, errorMessage)
                          .set(GitSyncErrorKeys.status, GitSyncErrorStatus.ACTIVE)
                          .set(GitSyncErrorKeys.createdAt, System.currentTimeMillis())
                          .set(GitSyncErrorKeys.lastUpdatedAt, System.currentTimeMillis());
      gitSyncErrorRepository.upsert(criteria, update);
    }
  }

  @Override
  public PageResponse<GitSyncErrorDTO> listConnectivityErrors(String accountIdentifier, String orgIdentifier,
      String projectIdentifier, String repoIdentifier, String branch, PageRequest pageRequest) {
    Criteria criteria = createConnectivityErrorFilterCriteria(
        accountIdentifier, orgIdentifier, projectIdentifier, repoIdentifier, branch);

    Page<GitSyncError> gitSyncErrors = gitSyncErrorRepository.findAll(criteria, PageUtils.getPageRequest(pageRequest));
    Page<GitSyncErrorDTO> dtos = gitSyncErrors.map(GitSyncErrorMapper::toGitSyncErrorDTO);
    return getNGPageResponse(dtos);
  }

  private Criteria createConnectivityErrorFilterCriteria(
      String accountIdentifier, String orgIdentifier, String projectIdentifier, String repoIdentifier, String branch) {
    Criteria criteria = Criteria.where(GitSyncErrorKeys.accountIdentifier)
                            .is(accountIdentifier)
                            .and(GitSyncErrorKeys.errorType)
                            .in(GitSyncErrorType.FULL_SYNC, GitSyncErrorType.CONNECTIVITY_ISSUE)
                            .and(GitSyncErrorKeys.scopes)
                            .is(Scope.of(accountIdentifier, orgIdentifier, projectIdentifier));
    Criteria repoBranchCriteria =
        getRepoBranchCriteria(accountIdentifier, orgIdentifier, projectIdentifier, repoIdentifier, branch);

    criteria.andOperator(repoBranchCriteria);
    criteria.and(GitSyncErrorKeys.status)
        .is(GitSyncErrorStatus.ACTIVE)
        .and(GitSyncErrorKeys.createdAt)
        .gt(OffsetDateTime.now().minusDays(30).toInstant().toEpochMilli());
    return criteria;
  }

  @Override
  public GitSyncErrorCountDTO getErrorCount(String accountIdentifier, String orgIdentifier, String projectIdentifier,
      String searchTerm, String repoId, String branch) {
    return GitSyncErrorCountDTO.builder()
        .gitToHarnessErrorCount(
            getGitToHarnessErrorCount(accountIdentifier, orgIdentifier, projectIdentifier, searchTerm, repoId, branch))
        .connectivityErrorCount(
            getConnectivityErrorCount(accountIdentifier, orgIdentifier, projectIdentifier, repoId, branch))
        .build();
  }

  private long getGitToHarnessErrorCount(String accountIdentifier, String orgIdentifier, String projectIdentifier,
      String searchTerm, String repoId, String branch) {
    Criteria criteria = createGitToHarnessErrorFilterCriteria(
        accountIdentifier, orgIdentifier, projectIdentifier, searchTerm, repoId, branch);
    criteria.and(GitSyncErrorKeys.status).is(GitSyncErrorStatus.ACTIVE);
    return gitSyncErrorRepository.count(criteria);
  }

  private long getConnectivityErrorCount(
      String accountIdentifier, String orgIdentifier, String projectIdentifier, String repoId, String branch) {
    Criteria criteria =
        createConnectivityErrorFilterCriteria(accountIdentifier, orgIdentifier, projectIdentifier, repoId, branch);
    return gitSyncErrorRepository.count(criteria);
  }

  @Override
  public void resolveConnectivityErrors(String accountIdentifier, String repoUrl, String branchName) {
    Criteria criteria = createActiveErrorsFilterCriteria(
        accountIdentifier, GitSyncErrorType.CONNECTIVITY_ISSUE, repoUrl, branchName, Collections.EMPTY_LIST);
    Update update = update(GitSyncErrorKeys.status, GitSyncErrorStatus.RESOLVED);
    gitSyncErrorRepository.updateError(criteria, update);
  }
}
