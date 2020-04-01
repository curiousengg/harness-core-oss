package software.wings.sm.states;

import static io.harness.data.structure.UUIDGenerator.generateUuid;
import static io.harness.rule.OwnerRule.RAGHU;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static software.wings.beans.SettingAttribute.Builder.aSettingAttribute;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

import io.harness.beans.DelegateTask;
import io.harness.category.element.UnitTests;
import io.harness.delegate.beans.TaskData;
import io.harness.rule.Owner;
import io.harness.waiter.WaitNotifyEngine;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.wings.beans.ScalyrConfig;
import software.wings.beans.SettingAttribute;
import software.wings.beans.TaskType;
import software.wings.service.impl.analysis.CustomLogDataCollectionInfo;
import software.wings.service.intfc.AppService;
import software.wings.service.intfc.DelegateService;
import software.wings.service.intfc.MetricDataAnalysisService;
import software.wings.service.intfc.SettingsService;
import software.wings.service.intfc.scalyr.ScalyrService;
import software.wings.service.intfc.security.SecretManager;
import software.wings.verification.VerificationStateAnalysisExecutionData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ScalyrStateTest extends APMStateVerificationTestBase {
  @InjectMocks private ScalyrState scalyrState;
  @Inject protected SecretManager secretManager;
  @Inject private ScalyrService scalyrService;
  @Mock SettingsService settingsService;
  @Mock MetricDataAnalysisService metricAnalysisService;
  @Mock AppService appService;
  @Mock WaitNotifyEngine waitNotifyEngine;
  @Mock DelegateService delegateService;

  @Before
  public void setup() throws Exception {
    setupCommon();
    MockitoAnnotations.initMocks(this);
    setupCommonMocks();
    FieldUtils.writeField(scalyrState, "scalyrService", scalyrService, true);
    FieldUtils.writeField(scalyrState, "secretManager", secretManager, true);

    scalyrState.setHostnameField("${host_name_expression}");
    scalyrState.setMessageField("${message_field_expression}");
    scalyrState.setTimestampField("${timestamp_field_expression}");
    when(executionContext.renderExpression("${host_name_expression}")).thenReturn("resolved_host_name");
    when(executionContext.renderExpression("${message_field_expression}")).thenReturn("resolved_message_field");
    when(executionContext.renderExpression("${timestamp_field_expression}")).thenReturn("resolved_timestamp_field");
  }

  @Test
  @Owner(developers = RAGHU)
  @Category(UnitTests.class)
  public void testRenderExpression() {
    VerificationStateAnalysisExecutionData executionData = mock(VerificationStateAnalysisExecutionData.class);
    Map<String, String> hosts = new HashMap<>();
    hosts.put("host", "default");

    final ScalyrConfig scalyrConfig =
        ScalyrConfig.builder().url("scalyr_url").apiToken("api_token".toCharArray()).build();
    when(settingsService.get(any())).thenReturn(aSettingAttribute().withValue(scalyrConfig).build());
    when(appService.get(anyString())).thenReturn(application);

    scalyrState.triggerAnalysisDataCollection(executionContext, executionData, hosts.keySet());
    ArgumentCaptor<DelegateTask> argument = ArgumentCaptor.forClass(DelegateTask.class);
    verify(delegateService).queueTask(argument.capture());
    TaskData taskData = argument.getValue().getData();
    Object parameters[] = taskData.getParameters();
    assertThat(1).isEqualTo(parameters.length);
    assertThat(TaskType.CUSTOM_LOG_COLLECTION_TASK.name()).isEqualTo(taskData.getTaskType());
    CustomLogDataCollectionInfo customLogDataCollectionInfo = (CustomLogDataCollectionInfo) parameters[0];
    assertThat(customLogDataCollectionInfo.getBaseUrl()).isEqualTo("scalyr_url");
    assertThat(customLogDataCollectionInfo.getValidationUrl()).isEqualTo(ScalyrConfig.VALIDATION_URL);
    assertThat(customLogDataCollectionInfo.getDataUrl()).isEqualTo(ScalyrConfig.QUERY_URL);
    assertThat(customLogDataCollectionInfo.getHeaders())
        .isEqualTo(Collections.singletonMap("Accept", "application/json"));
    assertThat(customLogDataCollectionInfo.getBody()).isEqualTo(scalyrConfig.fetchLogBodyMap(false));
    assertThat(customLogDataCollectionInfo.getHosts()).isEqualTo(Sets.newHashSet("host"));

    final Map<String, Map<String, CustomLogVerificationState.ResponseMapper>> logCollectionMapping =
        customLogDataCollectionInfo.getLogResponseDefinition();

    assertThat(logCollectionMapping.size()).isEqualTo(1);
    final Map<String, CustomLogVerificationState.ResponseMapper> responseMap =
        logCollectionMapping.get(ScalyrConfig.QUERY_URL);
    assertThat(responseMap.get("host"))
        .isEqualTo(CustomLogVerificationState.ResponseMapper.builder()
                       .fieldName("host")
                       .jsonPath(Collections.singletonList("resolved_host_name"))
                       .build());
    assertThat(responseMap.get("timestamp"))
        .isEqualTo(CustomLogVerificationState.ResponseMapper.builder()
                       .fieldName("timestamp")
                       .jsonPath(Collections.singletonList("resolved_timestamp_field"))
                       .build());
    assertThat(responseMap.get("logMessage"))
        .isEqualTo(CustomLogVerificationState.ResponseMapper.builder()
                       .fieldName("logMessage")
                       .jsonPath(Collections.singletonList("resolved_message_field"))
                       .build());
  }

  @Test
  @Owner(developers = RAGHU)
  @Category(UnitTests.class)
  public void testTriggerAnalysisDataCollection_renderConnectorId() {
    VerificationStateAnalysisExecutionData executionData = mock(VerificationStateAnalysisExecutionData.class);
    Map<String, String> hosts = new HashMap<>();
    hosts.put("host1", "default");
    hosts.put("host2", "default");
    hosts.put("host3", "default");
    String resolvedAnalysisServerConfigId = generateUuid();

    ScalyrConfig scalyrConfig = ScalyrConfig.builder().url(generateUuid()).build();
    SettingAttribute settingAttribute =
        aSettingAttribute().withUuid(resolvedAnalysisServerConfigId).withValue(scalyrConfig).withName("scalyr").build();
    wingsPersistence.save(settingAttribute);

    when(settingsService.get(eq(resolvedAnalysisServerConfigId))).thenReturn(settingAttribute);
    when(appService.get(anyString())).thenReturn(application);
    String analysisServerConfigId = "${workflow.variables.connectorName}";
    scalyrState.setAnalysisServerConfigId(analysisServerConfigId);

    ScalyrState spyState = spy(scalyrState);
    when(spyState.getResolvedConnectorId(any(), eq("analysisServerConfigId"), eq(analysisServerConfigId)))
        .thenReturn(resolvedAnalysisServerConfigId);

    spyState.triggerAnalysisDataCollection(executionContext, executionData, hosts.keySet());
    ArgumentCaptor<DelegateTask> argument = ArgumentCaptor.forClass(DelegateTask.class);
    verify(delegateService).queueTask(argument.capture());
    TaskData taskData = argument.getValue().getData();
    Object parameters[] = taskData.getParameters();
    assertThat(1).isEqualTo(parameters.length);
    assertThat(TaskType.CUSTOM_LOG_COLLECTION_TASK.name()).isEqualTo(taskData.getTaskType());
    CustomLogDataCollectionInfo customLogDataCollectionInfo = (CustomLogDataCollectionInfo) parameters[0];
    assertThat(customLogDataCollectionInfo.getHosts()).isEqualTo(hosts.keySet());

    final Map<String, Map<String, CustomLogVerificationState.ResponseMapper>> logCollectionMapping =
        customLogDataCollectionInfo.getLogResponseDefinition();

    assertThat(logCollectionMapping.size()).isEqualTo(1);
    final Map<String, CustomLogVerificationState.ResponseMapper> responseMap =
        logCollectionMapping.get(ScalyrConfig.QUERY_URL);
    assertThat(responseMap.get("host"))
        .isEqualTo(CustomLogVerificationState.ResponseMapper.builder()
                       .fieldName("host")
                       .jsonPath(Collections.singletonList("resolved_host_name"))
                       .build());
    assertThat(responseMap.get("timestamp"))
        .isEqualTo(CustomLogVerificationState.ResponseMapper.builder()
                       .fieldName("timestamp")
                       .jsonPath(Collections.singletonList("resolved_timestamp_field"))
                       .build());
    assertThat(responseMap.get("logMessage"))
        .isEqualTo(CustomLogVerificationState.ResponseMapper.builder()
                       .fieldName("logMessage")
                       .jsonPath(Collections.singletonList("resolved_message_field"))
                       .build());
  }
}
