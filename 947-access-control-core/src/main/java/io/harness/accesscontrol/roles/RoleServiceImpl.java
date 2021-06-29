package io.harness.accesscontrol.roles;

import static io.harness.accesscontrol.common.filter.ManagedFilter.NO_FILTER;
import static io.harness.accesscontrol.common.filter.ManagedFilter.ONLY_CUSTOM;
import static io.harness.accesscontrol.common.filter.ManagedFilter.ONLY_MANAGED;
import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.accesscontrol.common.filter.ManagedFilter;
import io.harness.accesscontrol.permissions.Permission;
import io.harness.accesscontrol.permissions.PermissionFilter;
import io.harness.accesscontrol.permissions.PermissionFilter.IncludedInAllRolesFilter;
import io.harness.accesscontrol.permissions.PermissionService;
import io.harness.accesscontrol.permissions.PermissionStatus;
import io.harness.accesscontrol.roleassignments.RoleAssignment;
import io.harness.accesscontrol.roleassignments.RoleAssignmentFilter;
import io.harness.accesscontrol.roleassignments.RoleAssignmentService;
import io.harness.accesscontrol.roles.filter.RoleFilter;
import io.harness.accesscontrol.roles.persistence.RoleDao;
import io.harness.accesscontrol.scopes.core.ScopeService;
import io.harness.annotations.dev.OwnedBy;
import io.harness.exception.InvalidArgumentsException;
import io.harness.exception.InvalidRequestException;
import io.harness.exception.UnexpectedException;
import io.harness.ng.beans.PageRequest;
import io.harness.ng.beans.PageResponse;
import io.harness.utils.RetryUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.executable.ValidateOnExecution;
import lombok.extern.slf4j.Slf4j;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

@OwnedBy(PL)
@Slf4j
@Singleton
@ValidateOnExecution
public class RoleServiceImpl implements RoleService {
  private final RoleDao roleDao;
  private final PermissionService permissionService;
  private final ScopeService scopeService;
  private final RoleAssignmentService roleAssignmentService;
  private final TransactionTemplate transactionTemplate;

  private static final Set<PermissionStatus> ALLOWED_PERMISSION_STATUS =
      Sets.newHashSet(PermissionStatus.EXPERIMENTAL, PermissionStatus.ACTIVE, PermissionStatus.DEPRECATED);

  private static final RetryPolicy<Object> removeRoleTransactionPolicy = RetryUtils.getRetryPolicy(
      "[Retrying]: Failed to remove role assignments for the role and remove the role; attempt: {}",
      "[Failed]: Failed to remove role assignments for the role and remove the role; attempt: {}",
      ImmutableList.of(TransactionException.class), Duration.ofSeconds(5), 3, log);

  @Inject
  public RoleServiceImpl(RoleDao roleDao, PermissionService permissionService, ScopeService scopeService,
      RoleAssignmentService roleAssignmentService, TransactionTemplate transactionTemplate) {
    this.roleDao = roleDao;
    this.permissionService = permissionService;
    this.scopeService = scopeService;
    this.roleAssignmentService = roleAssignmentService;
    this.transactionTemplate = transactionTemplate;
  }

  @Override
  public Role create(Role role) {
    validateScopes(role);
    validatePermissions(role);
    addCompulsoryPermissions(role);
    return roleDao.create(role);
  }

  @Override
  public PageResponse<Role> list(PageRequest pageRequest, RoleFilter roleFilter) {
    return roleDao.list(pageRequest, roleFilter);
  }

  @Override
  public Optional<Role> get(String identifier, String scopeIdentifier, ManagedFilter managedFilter) {
    return roleDao.get(identifier, scopeIdentifier, managedFilter);
  }

  @Override
  public RoleUpdateResult update(Role roleUpdate) {
    ManagedFilter managedFilter = roleUpdate.isManaged() ? ONLY_MANAGED : ONLY_CUSTOM;
    Optional<Role> currentRoleOptional =
        get(roleUpdate.getIdentifier(), roleUpdate.getScopeIdentifier(), managedFilter);
    if (!currentRoleOptional.isPresent()) {
      throw new InvalidRequestException(
          String.format("Could not find the role in the scope %s", roleUpdate.getScopeIdentifier()));
    }
    Role currentRole = currentRoleOptional.get();
    if (!currentRole.getAllowedScopeLevels().equals(roleUpdate.getAllowedScopeLevels())) {
      throw new InvalidRequestException("Cannot change the the scopes at which this role can be used.");
    }
    validatePermissions(roleUpdate);
    addCompulsoryPermissions(roleUpdate);
    roleUpdate.setVersion(currentRole.getVersion());
    roleUpdate.setCreatedAt(currentRole.getCreatedAt());
    roleUpdate.setLastModifiedAt(currentRole.getLastModifiedAt());
    Role updatedRole = roleDao.update(roleUpdate);
    return RoleUpdateResult.builder().originalRole(currentRole).updatedRole(updatedRole).build();
  }

  @Override
  public boolean removePermissionFromRoles(String permissionIdentifier) {
    return roleDao.removePermissionFromRoles(permissionIdentifier);
  }

  @Override
  public boolean addPermissionToRoles(String permissionIdentifier, RoleFilter roleFilter) {
    return roleDao.addPermissionToRoles(permissionIdentifier, roleFilter);
  }

  @Override
  public Role delete(String identifier, String scopeIdentifier) {
    Optional<Role> roleOpt = get(identifier, scopeIdentifier, NO_FILTER);
    if (!roleOpt.isPresent()) {
      throw new InvalidRequestException(String.format("Could not find the role in the scope %s", scopeIdentifier));
    }
    Role role = roleOpt.get();
    if (role.isManaged()) {
      return deleteManagedRole(identifier);
    } else {
      return deleteCustomRole(identifier, scopeIdentifier);
    }
  }

  @Override
  public long deleteMulti(RoleFilter roleFilter) {
    if (!roleFilter.getManagedFilter().equals(ONLY_CUSTOM)) {
      throw new InvalidRequestException("Can only delete custom roles");
    }
    return roleDao.deleteMulti(roleFilter);
  }

  private Role deleteManagedRole(String roleIdentifier) {
    return Failsafe.with(removeRoleTransactionPolicy).get(() -> transactionTemplate.execute(status -> {
      roleAssignmentService.deleteMulti(RoleAssignmentFilter.builder()
                                            .scopeFilter("/")
                                            .includeChildScopes(true)
                                            .roleFilter(Sets.newHashSet(roleIdentifier))
                                            .build());
      return roleDao.delete(roleIdentifier, null)
          .orElseThrow(
              () -> new UnexpectedException(String.format("Failed to delete the managed role %s", roleIdentifier)));
    }));
  }

  private Role deleteCustomRole(String identifier, String scopeIdentifier) {
    PageResponse<RoleAssignment> pageResponse = roleAssignmentService.list(PageRequest.builder().pageSize(1).build(),
        RoleAssignmentFilter.builder().scopeFilter(scopeIdentifier).roleFilter(Sets.newHashSet(identifier)).build());
    if (pageResponse.getTotalItems() > 0) {
      throw new InvalidRequestException(String.format(
          "Cannot delete role because %s role assignments exists using the role", pageResponse.getTotalItems()));
    }
    return roleDao.delete(identifier, scopeIdentifier)
        .orElseThrow(()
                         -> new UnexpectedException(String.format(
                             "Failed to delete the role %s in the scope %s", identifier, scopeIdentifier)));
  }

  private void validatePermissions(Role role) {
    PermissionFilter permissionFilter = PermissionFilter.builder()
                                            .identifierFilter(role.getPermissions())
                                            .statusFilter(ALLOWED_PERMISSION_STATUS)
                                            .allowedScopeLevelsFilter(role.getAllowedScopeLevels())
                                            .build();
    List<Permission> permissionList = permissionService.list(permissionFilter);
    permissionList = permissionList == null ? new ArrayList<>() : permissionList;
    Set<String> validPermissions = permissionList.stream().map(Permission::getIdentifier).collect(Collectors.toSet());
    Set<String> invalidPermissions = Sets.difference(role.getPermissions(), validPermissions);
    if (!invalidPermissions.isEmpty()) {
      log.error("Invalid permissions while creating role {} in scope {} : [ {} ]", role.getIdentifier(),
          role.getScopeIdentifier(), String.join(",", invalidPermissions));
      throw new InvalidArgumentsException(
          "Some of the specified permissions in the role are invalid or cannot be given at this scope. Please check the permissions again");
    }
  }

  private void validateScopes(Role role) {
    if (role.isManaged() && !scopeService.areScopeLevelsValid(role.getAllowedScopeLevels())) {
      throw new InvalidArgumentsException(
          String.format("The provided scopes are not registered in the service. Please select scopes out of [ %s ]",
              String.join(",", scopeService.getAllScopeLevels())));
    }
    if (!role.isManaged()) {
      String scopeLevel = scopeService.buildScopeFromScopeIdentifier(role.getScopeIdentifier()).getLevel().toString();
      if (role.getAllowedScopeLevels().size() > 1 || !role.getAllowedScopeLevels().contains(scopeLevel)) {
        throw new InvalidArgumentsException(String.format(
            "This custom role can be only used at '%s' level. Please set the allowedScopeLevels to contain only the %s level.",
            scopeLevel, scopeLevel));
      }
    }
  }

  private void addCompulsoryPermissions(Role role) {
    PermissionFilter permissionFilter =
        PermissionFilter.builder()
            .allowedScopeLevelsFilter(role.getAllowedScopeLevels())
            .statusFilter(ALLOWED_PERMISSION_STATUS)
            .includedInAllRolesFilter(IncludedInAllRolesFilter.PERMISSIONS_INCLUDED_IN_ALL_ROLES)
            .build();
    List<Permission> permissionList = permissionService.list(permissionFilter);
    permissionList = permissionList == null ? new ArrayList<>() : permissionList;
    Set<String> compulsoryPermissions =
        permissionList.stream().map(Permission::getIdentifier).collect(Collectors.toSet());
    role.getPermissions().addAll(compulsoryPermissions);
  }
}
