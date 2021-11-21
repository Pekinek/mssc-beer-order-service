package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEvent;
import guru.sfg.beer.order.service.domain.BeerOrderStatus;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class BeerOrderManagerImpl implements BeerOrderManager {

    private final StateMachineFactory<BeerOrderStatus, BeerOrderEvent> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    private final StateMachinePersister<BeerOrderStatus, BeerOrderEvent, UUID> persister;


    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatus.NEW);

        return beerOrderRepository.save(beerOrder);
    }

    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEvent eventEnum) throws Exception {
        StateMachine<BeerOrderStatus, BeerOrderEvent> sm = stateMachineFactory.getStateMachine(
                beerOrder.getId());
        persister.restore(sm, beerOrder.getId());
        Message<BeerOrderEvent> msg = MessageBuilder.withPayload(eventEnum).setHeader("BeerOrder", beerOrder).build();
        sm.sendEvent(Mono.just(msg));
    }
}
