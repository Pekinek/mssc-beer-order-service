package guru.sfg.beer.order.service.statemachine;

import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
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
public class BeerOrderStateMachineConfig extends StateMachineConfigurerAdapter<BeerOrderStatusEnum,
        BeerOrderEventEnum> {

    DatabaseStateMachinePersist databaseStateMachinePersist;

    @Bean
    public StateMachinePersister<BeerOrderStatusEnum, BeerOrderEventEnum, UUID> persister(DatabaseStateMachinePersist databaseStateMachinePersist){
        return new DefaultStateMachinePersister<>(databaseStateMachinePersist);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<BeerOrderStatusEnum, BeerOrderEventEnum> config) throws Exception {
        config.withPersistence().runtimePersister(databaseStateMachinePersist);
    }

    @Override
    public void configure(StateMachineStateConfigurer<BeerOrderStatusEnum, BeerOrderEventEnum> states) throws Exception {
        states.withStates()
              .initial(BeerOrderStatusEnum.NEW)
              .states(EnumSet.allOf(BeerOrderStatusEnum.class))
              .end(BeerOrderStatusEnum.DELIVERED)
              .end(BeerOrderStatusEnum.PICKED_UP)
              .end(BeerOrderStatusEnum.DELIVERY_EXCEPTION)
              .end(BeerOrderStatusEnum.VALIDATION_EXCEPTION)
              .end(BeerOrderStatusEnum.ALLOCATION_EXCEPTION);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<BeerOrderStatusEnum, BeerOrderEventEnum> transitions) throws Exception {
        transitions.withExternal()
                   .source(BeerOrderStatusEnum.NEW)
                   .target(BeerOrderStatusEnum.VALIDATION_PENDING)
                   .event(BeerOrderEventEnum.VALIDATE_ORDER)
                   .and()

                   .withExternal()
                   .source(BeerOrderStatusEnum.VALIDATION_PENDING)
                   .target(BeerOrderStatusEnum.VALIDATED)
                   .event(BeerOrderEventEnum.VALIDATION_PASSED)
                   .and()

                   .withExternal()
                   .source(BeerOrderStatusEnum.VALIDATION_PENDING)
                   .target(BeerOrderStatusEnum.VALIDATION_EXCEPTION)
                   .event(BeerOrderEventEnum.VALIDATION_FAILED);
    }
}
