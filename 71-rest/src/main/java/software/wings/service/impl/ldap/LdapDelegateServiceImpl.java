package software.wings.service.impl.ldap;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.harness.exception.WingsException;
import org.apache.commons.lang3.StringUtils;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.wings.beans.sso.LdapGroupResponse;
import software.wings.beans.sso.LdapSettings;
import software.wings.beans.sso.LdapTestResponse;
import software.wings.beans.sso.LdapTestResponse.Status;
import software.wings.beans.sso.LdapUserResponse;
import software.wings.helpers.ext.ldap.LdapConstants;
import software.wings.helpers.ext.ldap.LdapGroupConfig;
import software.wings.helpers.ext.ldap.LdapResponse;
import software.wings.helpers.ext.ldap.LdapUserConfig;
import software.wings.security.encryption.EncryptedDataDetail;
import software.wings.service.intfc.ldap.LdapDelegateService;
import software.wings.service.intfc.security.EncryptionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Impl for Ldap Delegate Service  {@link LdapDelegateService}.
 * Created by Pranjal on 08/21/2018
 */
@Singleton
public class LdapDelegateServiceImpl implements LdapDelegateService {
  private static final Logger logger = LoggerFactory.getLogger(LdapDelegateServiceImpl.class);
  @Inject private EncryptionService encryptionService;

  @Override
  public LdapTestResponse validateLdapConnectionSettings(
      LdapSettings settings, EncryptedDataDetail encryptedDataDetail) {
    logger.info("Initiating validateLdapConnectionSettings with ldap settings : ", settings);
    settings.decryptFields(encryptedDataDetail, encryptionService);
    LdapHelper helper = new LdapHelper(settings.getConnectionSettings());
    LdapResponse response = helper.validateConnectionConfig();
    if (response.getStatus() == LdapResponse.Status.SUCCESS) {
      return LdapTestResponse.builder().status(Status.SUCCESS).message(response.getMessage()).build();
    }
    return LdapTestResponse.builder().status(Status.FAILURE).message(response.getMessage()).build();
  }

  @Override
  public LdapTestResponse validateLdapUserSettings(LdapSettings settings, EncryptedDataDetail encryptedDataDetail) {
    logger.info("Initiating validateLdapUserSettings with ldap settings : ", settings);
    settings.decryptFields(encryptedDataDetail, encryptionService);
    LdapHelper helper = new LdapHelper(settings.getConnectionSettings());
    LdapResponse response = helper.validateUserConfig(settings.getUserSettingsList());
    if (response.getStatus() == LdapResponse.Status.SUCCESS) {
      return LdapTestResponse.builder().status(Status.SUCCESS).message(response.getMessage()).build();
    }
    return LdapTestResponse.builder().status(Status.FAILURE).message(response.getMessage()).build();
  }

  @Override
  public LdapTestResponse validateLdapGroupSettings(LdapSettings settings, EncryptedDataDetail encryptedDataDetail) {
    logger.info("Initiating validateLdapGroupSettings with ldap settings : ", settings);
    settings.decryptFields(encryptedDataDetail, encryptionService);
    LdapHelper helper = new LdapHelper(settings.getConnectionSettings());
    LdapResponse response = helper.validateGroupConfig(settings.getGroupSettingsList().get(0));
    if (response.getStatus() == LdapResponse.Status.SUCCESS) {
      return LdapTestResponse.builder().status(Status.SUCCESS).message(response.getMessage()).build();
    }
    return LdapTestResponse.builder().status(Status.FAILURE).message(response.getMessage()).build();
  }

  @Override
  public LdapResponse authenticate(LdapSettings settings, EncryptedDataDetail settingsEncryptedDataDetail,
      String username, EncryptedDataDetail passwordEncryptedDataDetail) {
    settings.decryptFields(settingsEncryptedDataDetail, encryptionService);
    String password = null;
    try {
      password = new String(encryptionService.getDecryptedValue(passwordEncryptedDataDetail));
    } catch (IOException e) {
      throw new WingsException("Failed to decrypt the password.");
    }
    LdapHelper helper = new LdapHelper(settings.getConnectionSettings());
    return helper.authenticate(settings.getUserSettingsList(), username, password);
  }

  private LdapGroupResponse buildLdapGroupResponse(LdapEntry group, LdapGroupConfig settings) {
    Set<String> availableAttrs = Sets.newHashSet(group.getAttributeNames());
    String name = group.getAttribute(settings.getNameAttr()).getStringValue();
    String description = StringUtils.EMPTY;
    String descriptionAttr = settings.getDescriptionAttr();

    if (availableAttrs.contains(descriptionAttr)) {
      description = group.getAttribute(settings.getDescriptionAttr()).getStringValue();
    }

    int totalMembers = Integer.parseInt(group.getAttribute(LdapConstants.GROUP_SIZE_ATTR).getStringValue());
    boolean selectable = true;
    String message = "";

    if (LdapConstants.MAX_GROUP_MEMBERS_LIMIT < totalMembers) {
      selectable = false;
      message = LdapConstants.GROUP_MEMBERS_EXCEEDED;
    }

    return LdapGroupResponse.builder()
        .dn(group.getDn())
        .name(name)
        .description(description)
        .totalMembers(totalMembers)
        .selectable(selectable)
        .message(message)
        .build();
  }

  private LdapUserResponse buildLdapUserResponse(LdapEntry user, LdapUserConfig userConfig) {
    String name, email = null;
    LdapAttribute ldapEmailAttribute = user.getAttribute(userConfig.getEmailAttr());
    if (ldapEmailAttribute != null) {
      email = ldapEmailAttribute.getStringValue();
    } else {
      logger.warn("UserConfig email attribute = {} is missing in LdapEntry user object", userConfig.getEmailAttr());
    }

    if (Arrays.asList(user.getAttributeNames()).contains(userConfig.getDisplayNameAttr())) {
      name = user.getAttribute(userConfig.getDisplayNameAttr()).getStringValue();
    } else {
      name = email;
    }

    return LdapUserResponse.builder().dn(user.getDn()).name(name).email(email).build();
  }

  @Override
  public Collection<LdapGroupResponse> searchGroupsByName(
      LdapSettings settings, EncryptedDataDetail encryptedDataDetail, String nameQuery) {
    settings.decryptFields(encryptedDataDetail, encryptionService);
    LdapHelper helper = new LdapHelper(settings.getConnectionSettings());
    try {
      List<LdapListGroupsResponse> ldapListGroupsResponses =
          helper.searchGroupsByName(settings.getGroupSettingsList(), nameQuery);
      return createLdapGroupResponse(helper, ldapListGroupsResponses, settings);
    } catch (LdapException e) {
      throw new WingsException(e.getResultCode().toString(), e);
    }
  }

  private Collection<LdapGroupResponse> createLdapGroupResponse(LdapHelper helper,
      List<LdapListGroupsResponse> ldapListGroupsResponses, LdapSettings ldapSettings) throws LdapException {
    Collection<LdapGroupResponse> ldapGroupResponse = new ArrayList<>();
    for (LdapListGroupsResponse ldapListGroupsResponse : ldapListGroupsResponses) {
      if (LdapResponse.Status.SUCCESS.equals(ldapListGroupsResponse.getLdapResponse().getStatus())) {
        helper.populateGroupSize(ldapListGroupsResponse.getSearchResult(), ldapSettings.getUserSettingsList());
        Collection<LdapEntry> entries = ldapListGroupsResponse.getSearchResult().getEntries();
        for (LdapEntry entry : entries) {
          ldapGroupResponse.add(buildLdapGroupResponse(entry, ldapListGroupsResponse.getLdapGroupConfig()));
        }
      }
    }
    return ldapGroupResponse;
  }

  @Override
  public LdapGroupResponse fetchGroupByDn(LdapSettings settings, EncryptedDataDetail encryptedDataDetail, String dn) {
    settings.decryptFields(encryptedDataDetail, encryptionService);
    LdapHelper helper = new LdapHelper(settings.getConnectionSettings());
    LdapGroupResponse groupResponse = null;
    try {
      LdapListGroupsResponse listGroupsResponse = helper.getGroupByDn(settings.getGroupSettingsList(), dn);

      // Is the call to fetch the group failed.
      if (LdapResponse.Status.SUCCESS != listGroupsResponse.getLdapResponse().getStatus()) {
        return groupResponse;
      }

      SearchResult groups = listGroupsResponse.getSearchResult();
      helper.populateGroupSize(groups, settings.getUserSettingsList());

      // If there are no entries in the group.
      LdapEntry group = groups.getEntries().isEmpty() ? null : groups.getEntries().iterator().next();
      if (null == group) {
        return null;
      }

      groupResponse = buildLdapGroupResponse(group, listGroupsResponse.getLdapGroupConfig());
      if (!groupResponse.isSelectable()) {
        return groupResponse;
      }

      Collection<LdapUserResponse> userResponses = null;

      List<LdapGetUsersResponse> ldapGetUsersResponses = helper.listGroupUsers(settings.getUserSettingsList(), dn);

      userResponses = ldapGetUsersResponses.stream()
                          .map(ldapGetUsersResponse
                              -> ldapGetUsersResponse.getSearchResult()
                                     .getEntries()
                                     .stream()
                                     .map(user -> buildLdapUserResponse(user, ldapGetUsersResponse.getLdapUserConfig()))
                                     .collect(Collectors.toList()))
                          .flatMap(x -> x.stream())
                          .collect(Collectors.toList());

      groupResponse.setUsers(userResponses);
      return groupResponse;
    } catch (LdapException e) {
      if (e.getResultCode().equals(ResultCode.NO_SUCH_OBJECT)) {
        logger.error(String.format("Ldap [%s] received while fetching group by dn: [%s] for Ldap Name: [%s]",
            e.getResultCode().toString(), dn, settings.getPublicSSOSettings().getDisplayName()));
        return null;
      }
      throw new WingsException(e.getResultCode().toString(), e);
    }
  }
}
