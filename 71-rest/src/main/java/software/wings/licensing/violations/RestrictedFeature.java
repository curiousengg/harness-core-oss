package software.wings.licensing.violations;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import software.wings.licensing.violations.checkers.UsersViolationChecker;

@Getter
@ToString
public enum RestrictedFeature {
  USERS(UsersViolationChecker.class);

  private final Class<? extends FeatureViolationChecker> violationsCheckerClass;

  RestrictedFeature(@NonNull Class<? extends FeatureViolationChecker> violationsCheckerClass) {
    this.violationsCheckerClass = violationsCheckerClass;
  }
}
