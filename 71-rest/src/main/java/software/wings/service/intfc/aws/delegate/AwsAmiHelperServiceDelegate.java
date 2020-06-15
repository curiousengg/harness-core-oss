package software.wings.service.intfc.aws.delegate;

import software.wings.beans.command.ExecutionLogCallback;
import software.wings.service.impl.aws.model.AwsAmiServiceDeployRequest;
import software.wings.service.impl.aws.model.AwsAmiServiceDeployResponse;
import software.wings.service.impl.aws.model.AwsAmiServiceSetupRequest;
import software.wings.service.impl.aws.model.AwsAmiServiceSetupResponse;
import software.wings.service.impl.aws.model.AwsAmiServiceTrafficShiftAlbDeployRequest;
import software.wings.service.impl.aws.model.AwsAmiServiceTrafficShiftAlbSetupRequest;
import software.wings.service.impl.aws.model.AwsAmiServiceTrafficShiftAlbSetupResponse;
import software.wings.service.impl.aws.model.AwsAmiSwitchRoutesRequest;
import software.wings.service.impl.aws.model.AwsAmiSwitchRoutesResponse;

public interface AwsAmiHelperServiceDelegate {
  AwsAmiServiceSetupResponse setUpAmiService(AwsAmiServiceSetupRequest request, ExecutionLogCallback logCallback);
  AwsAmiServiceDeployResponse deployAmiService(AwsAmiServiceDeployRequest request, ExecutionLogCallback logCallback);
  AwsAmiSwitchRoutesResponse switchAmiRoutes(AwsAmiSwitchRoutesRequest request, ExecutionLogCallback logCallback);
  AwsAmiSwitchRoutesResponse rollbackSwitchAmiRoutes(
      AwsAmiSwitchRoutesRequest request, ExecutionLogCallback logCallback);

  AwsAmiServiceTrafficShiftAlbSetupResponse setUpAmiServiceTrafficShift(
      AwsAmiServiceTrafficShiftAlbSetupRequest request, ExecutionLogCallback logCallback);
  AwsAmiServiceDeployResponse deployAmiServiceTrafficShift(
      AwsAmiServiceTrafficShiftAlbDeployRequest request, ExecutionLogCallback logCallback);
  AwsAmiServiceTrafficShiftAlbSetupResponse switchAmiRoutesTrafficShift(
      AwsAmiServiceTrafficShiftAlbSetupRequest request, ExecutionLogCallback logCallback);
  AwsAmiServiceTrafficShiftAlbSetupResponse rollbackSwitchAmiRoutesTrafficShift(
      AwsAmiServiceTrafficShiftAlbSetupRequest request, ExecutionLogCallback logCallback);
}