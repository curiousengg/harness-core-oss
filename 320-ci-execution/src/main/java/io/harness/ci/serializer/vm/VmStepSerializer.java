package io.harness.ci.serializer.vm;

import io.harness.beans.steps.CIStepInfo;
import io.harness.beans.steps.stepinfo.RunStepInfo;
import io.harness.delegate.beans.ci.vm.steps.VmStepInfo;
import io.harness.pms.yaml.ParameterField;
import io.harness.yaml.core.timeout.Timeout;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class VmStepSerializer {
  @Inject private VmRunStepSerializer vmRunStepSerializer;
  public VmStepInfo serialize(
      CIStepInfo stepInfo, String identifier, ParameterField<Timeout> parameterFieldTimeout) {
      String stepName = stepInfo.getNonYamlInfo().getStepInfoType().getDisplayName();
    switch (stepInfo.getNonYamlInfo().getStepInfoType()) {
      case RUN:
        return vmRunStepSerializer.serialize((RunStepInfo) stepInfo, identifier, parameterFieldTimeout, stepName);
      case CLEANUP:
      case TEST:
      case BUILD:
      case SETUP_ENV:
      case GIT_CLONE:
      case INITIALIZE_TASK:
      default:
        //                log.info("serialisation is not implemented");
        return null;
    }
  }
}
