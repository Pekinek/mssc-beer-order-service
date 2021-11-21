package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEvent;
import guru.sfg.beer.order.service.domain.BeerOrderStatus;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class BeerOrderManagerImpl implements BeerOrderManager {

    public static final String BEER_ORDER = "BEER_ORDER";

    private final StateMachineFactory<BeerOrderStatus, BeerOrderEvent> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    private final StateMachinePersister<BeerOrderStatus, BeerOrderEvent, UUID> persister;



    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatus.NEW);

        BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);
        sendBeerOrderEvent(savedBeerOrder, BeerOrderEvent.VALIDATE_ORDER);
        return savedBeerOrder;
    }


    @Override
    public void processValidationResult(UUID beerOrderId, Boolean isValid) {

        BeerOrder beerOrder = beerOrderRepository.findOneById(beerOrderId);
        if (isValid) {
            sendBeerOrderEvent(beerOrder, BeerOrderEvent.VALIDATION_PASSED);
            BeerOrder validatedOrder = beerOrderRepository.findOneById(beerOrderId);
            sendBeerOrderEvent(validatedOrder, BeerOrderEvent.ALLOCATE_ORDER);
        } else {
            sendBeerOrderEvent(beerOrder, BeerOrderEvent.VALIDATION_FAILED);
        }
    }

    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEvent eventEnum) {
        StateMachine<BeerOrderStatus, BeerOrderEvent> sm = build(beerOrder);
        Message<BeerOrderEvent> msg = MessageBuilder.withPayload(eventEnum).setHeader(BEER_ORDER, beerOrder).build();
        sm.sendEvent(Mono.just(msg));
    }

    private StateMachine<BeerOrderStatus, BeerOrderEvent> build(BeerOrder beerOrder) {
        StateMachine<BeerOrderStatus, BeerOrderEvent> sm = stateMachineFactory.getStateMachine(beerOrder.getId());
        sm.stopReactively().block();
        StateMachineContext<BeerOrderStatus, BeerOrderEvent> context = new DefaultStateMachineContext<>(
                beerOrder.getOrderStatus(), null, null, null);
        sm.getStateMachineAccessor()
          .doWithAllRegions(function -> function.resetStateMachineReactively(context).block());
        sm.startReactively().block();
        return sm;
    }
}
