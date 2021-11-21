package guru.sfg.beer.order.service.statemachine;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.persist.AbstractPersistingStateMachineInterceptor;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseStateMachinePersist extends AbstractPersistingStateMachineInterceptor<BeerOrderStatusEnum,
        BeerOrderEventEnum, UUID> implements StateMachineRuntimePersister<BeerOrderStatusEnum,
                BeerOrderEventEnum, UUID>, StateMachinePersist<BeerOrderStatusEnum, BeerOrderEventEnum, UUID> {

    private BeerOrderRepository beerOrderRepository;

    @Transactional
    @Override
    public void write(StateMachineContext<BeerOrderStatusEnum, BeerOrderEventEnum> context,
                      UUID contextObj) {
        BeerOrder beerOrder = beerOrderRepository.getById(contextObj);
        beerOrder.setOrderStatus(context.getState());
        log.debug("Saving state machine to database: " + context.getState());
        beerOrderRepository.saveAndFlush(beerOrder);
    }

    @Transactional
    @Override
    public StateMachineContext<BeerOrderStatusEnum, BeerOrderEventEnum> read(UUID contextObj) {
        BeerOrder beerOrder = beerOrderRepository.getById(contextObj);

        return new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null, null, null);
    }

    @Override
    public StateMachineInterceptor<BeerOrderStatusEnum, BeerOrderEventEnum> getInterceptor() {
        return this;
    }
}
