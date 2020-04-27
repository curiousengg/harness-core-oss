package io.harness.redesign.states.wait;

import io.harness.annotations.ProducesState;
import io.harness.registries.state.StateProducer;
import io.harness.state.State;
import io.harness.state.StateType;

@ProducesState
public class WaitStateProducer implements StateProducer {
  @Override
  public State produce() {
    return new WaitState();
  }

  @Override
  public StateType getType() {
    return StateType.builder().type(WaitState.STATE_TYPE).build();
  }
}
