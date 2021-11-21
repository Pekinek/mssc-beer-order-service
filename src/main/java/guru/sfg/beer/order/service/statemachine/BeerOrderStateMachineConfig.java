package guru.sfg.beer.order.service.statemachine;

import guru.sfg.beer.order.service.domain.BeerOrderEvent;
import guru.sfg.beer.order.service.domain.BeerOrderStatus;
import guru.sfg.beer.order.service.statemachine.actions.ValidateOrderAction;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

import java.util.EnumSet;
import java.util.UUID;

@Configuration
@EnableStateMachineFactory
@RequiredArgsConstructor
public class BeerOrderStateMachineConfig extends StateMachineConfigurerAdapter<BeerOrderStatus, BeerOrderEvent> {

    private final DatabaseStateMachinePersist databaseStateMachinePersist;
    private final ValidateOrderAction validateOrderAction;


    @Bean
    public StateMachinePersister<BeerOrderStatus, BeerOrderEvent, UUID> persister(DatabaseStateMachinePersist databaseStateMachinePersist) {
        return new DefaultStateMachinePersister<>(databaseStateMachinePersist);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<BeerOrderStatus, BeerOrderEvent> config) throws Exception {
        config.withPersistence().runtimePersister(databaseStateMachinePersist);
    }

    @Override
    public void configure(StateMachineStateConfigurer<BeerOrderStatus, BeerOrderEvent> states) throws Exception {
        states.withStates()
              .initial(BeerOrderStatus.NEW)
              .states(EnumSet.allOf(BeerOrderStatus.class))
              .end(BeerOrderStatus.DELIVERED)
              .end(BeerOrderStatus.PICKED_UP)
              .end(BeerOrderStatus.DELIVERY_EXCEPTION)
              .end(BeerOrderStatus.VALIDATION_EXCEPTION)
              .end(BeerOrderStatus.ALLOCATION_EXCEPTION);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<BeerOrderStatus, BeerOrderEvent> transitions) throws Exception {
        transitions.withExternal()
                   .source(BeerOrderStatus.NEW)
                   .target(BeerOrderStatus.VALIDATION_PENDING)
                   .event(BeerOrderEvent.VALIDATE_ORDER)
                   .action(validateOrderAction)
                   .and()

                   .withExternal()
                   .source(BeerOrderStatus.VALIDATION_PENDING)
                   .target(BeerOrderStatus.VALIDATED)
                   .event(BeerOrderEvent.VALIDATION_PASSED)
                   .and()

                   .withExternal()
                   .source(BeerOrderStatus.VALIDATION_PENDING)
                   .target(BeerOrderStatus.VALIDATION_EXCEPTION)
                   .event(BeerOrderEvent.VALIDATION_FAILED);
    }
}
